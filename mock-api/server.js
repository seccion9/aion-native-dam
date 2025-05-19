/* ────────────────────────────────────────────────────────────────────────── */
/*  server.js – Mock API con:                                                */
/*    ▸ login /api/sanctum/token                                             */
/*    ▸ middleware Bearer para TODO lo que empiece por /api                  */
/*    ▸ rewriter (routes.json) – mantiene tus redirects                      */
/*    ▸ endpoint calculado:                                                  */
/*        /api/getMonthlyOccupancyByExperienceIdsAndDates                    */
/*      (mismo contrato que la API real)                                     */
/*    ▸ Notificaciones Push (Firebase Admin SDK)                             */
/* ────────────────────────────────────────────────────────────────────────── */

const jsonServer = require('json-server');
const server    = jsonServer.create();
const router    = jsonServer.router('db.json');
const middle    = jsonServer.defaults();
const rewriter  = jsonServer.rewriter(require('./routes.json'));

server.use(middle);
server.use(jsonServer.bodyParser);

/* ─────────────── INICIO: Firebase Admin para Push ─────────────── */
const admin = require('firebase-admin');
const serviceAccount = require('./service-account.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});

/**
 * Enviar push notification con FCM (Firebase Admin SDK)
 * @param {string} tokenUsuario registration token del usuario
 * @param {string} titulo título de la notificación
 * @param {string} mensaje cuerpo del mensaje
 */
async function enviarPushFCM(tokenUsuario, titulo, mensaje) {
  const payload = {
    token: tokenUsuario,
    notification: {
      title: titulo,
      body: mensaje,
    }
  };
  try {
    const response = await admin.messaging().send(payload);
    console.log('Push enviada:', response);
  } catch (error) {
    console.error('Error enviando push:', error);
  }
}
/* ─────────────── FIN: Firebase Admin para Push ─────────────── */

/* ─────────────────────────── LOGIN fake ──────────────────────────── */
server.post('/api/sanctum/token', (req, res) => {
  const { email, password } = req.body;
  const user = router.db.get('users').find({ email, password }).value();

  return user
    ? res.status(200).json({ token: user.token })
    : res.status(401).json({ error: 'Credenciales incorrectas' });
});

/* ─────────── Middleware global /api – exige Bearer <token> ────────── */
server.use('/api', (req, res, next) => {
  if (req.path === '/sanctum/token') return next();          // deja pasar login

  const auth = req.headers.authorization || '';
  if (!auth.startsWith('Bearer ')) {
    return res.status(401).json({ error: 'Token no proporcionado' });
  }

  const token = auth.replace('Bearer ', '');
  const user  = router.db.get('users').find({ token }).value();
  if (!user)  return res.status(403).json({ error: 'Token inválido' });

  next();                                                    // token OK
});

/* ───────  /api/getMonthlyOccupancyByExperienceIdsAndDates  ───────── */
server.get('/api/getMonthlyOccupancyByExperienceIdsAndDates', (req, res) => {
  const dateStartStr = req.query.date_start;
  const dateEndStr   = req.query.date_end;

  if (!dateStartStr || !dateEndStr) {
    return res.status(400).json({ error: 'date_start y date_end son requeridos' });
  }

  const parseDMY = s => {
    const [d, m, y] = s.split('/');
    return new Date(`${y}-${m}-${d}T00:00:00`);
  };
  const dStart = parseDMY(dateStartStr);
  const dEnd   = parseDMY(dateEndStr);

  const allData = router.db.get('monthlyOccupancy').value()[0]; // solo 1 objeto en el array
  const result = {};

  for (const [fecha, datos] of Object.entries(allData)) {
    const d = parseDMY(fecha);
    if (d >= dStart && d <= dEnd) {
      result[fecha] = datos;
    }
  }

  res.json(result);
});
server.get('/occupancyER', (req, res) => {
  const start = req.query.date_start;
  const end   = req.query.date_end;

  const data = router.db.get('occupancyER')
    .filter(item => item.date >= start && item.date <= end)
    .value();

  res.json(data);
});
/* ──────────── Rewriter + resto de colecciones (/api/*) ───────────── */
server.use(rewriter);           // mantiene rutas de routes.json (purchases, etc.)

/* ────────── Endpoint filtrado /api/escaperadar/getOccupancyBetweenDates ────────── */
server.get('/api/escaperadar/getOccupancyBetweenDates', (req, res) => {
  const { date_start, date_end } = req.query;

  // valida que lleguen los dos parámetros
  if (!date_start || !date_end) {
    return res.status(400).json({ error: 'date_start y date_end son requeridos' });
  }

  // filtra la colección occupancyER por rango inclusivo
  const datosFiltrados = router.db
    .get('occupancyER')
    .filter(o => o.date >= date_start && o.date <= date_end)
    .value();

  res.json(datosFiltrados);
});
server.patch('/api/purchases/:id', (req, res) => {
  const { id } = req.params;
  const cambios = req.body;

  console.log("ID recibido:", id);

  const todasLasCompras = router.db.get('purchases').value(); 
  console.log("IDs existentes:", todasLasCompras.map(c => c.id));

  const compra = router.db.get('purchases').find({ id }).value();

  if (!compra) {
    return res.status(404).json({ error: 'Compra no encontrada' });
  }

  const compraActualizada = router.db.get('purchases')
    .find({ id })
    .assign(cambios)
    .write();

  res.status(200).json(compraActualizada);
});

/* ────────── Endpoint /api/paymentsCajaChica ────────── */
server.get('/api/paymentsCajaChicaDia/:fecha', (req, res) => {
  const { fecha } = req.params;

  const pagosDelDia = router.db
    .get('paymentsCajaChica')
    .filter({ fecha })
    .value();

  res.status(200).json(pagosDelDia);
});

/* ────────── Endpoint PATCH para guardar el FCM token de un usuario ────────── */
server.patch('/api/users/:id/fcmToken', (req, res) => {
  const { id } = req.params;
  const { fcmToken } = req.body;
  if (!fcmToken) {
    return res.status(400).json({ error: 'Falta el FCM token' });
  }
  const user = router.db.get('users').find({ id }).value();
  if (!user) {
    return res.status(404).json({ error: 'Usuario no encontrado' });
  }
  router.db.get('users').find({ id }).assign({ fcmToken }).write();
  res.status(200).json({ ok: true });
});

/* ────────── Endpoint POST /purchases ────────── */
server.post('/api/purchases', async (req, res) => {
  const nuevaCompra = req.body;

  const auth = req.headers.authorization || '';

  const token = auth.replace('Bearer ', '');
  const user = router.db.get('users').find({ token }).value();

  if (!user) {
    return res.status(403).json({ error: 'Token inválido' });
  }

  if (!nuevaCompra || !nuevaCompra.id) {
    return res.status(400).json({ error: 'Compra inválida. Falta ID u otros campos.' });
  }

  // Añadir ID del usuario autenticado
  nuevaCompra.userId = user.id; // o user.email

  router.db.get('purchases').push(nuevaCompra).write();
  // Si el usuario tiene un fcmToken, se le manda push
  if (user.fcmToken) {
    await enviarPushFCM(
      user.fcmToken,
      'Nueva reserva',
      `Tu reserva ${nuevaCompra.id} ha sido registrada`
    );
  }

  res.status(201).json(nuevaCompra);
});

server.get('/api/purchases', (req, res) => {
  const auth = req.headers.authorization || '';
  const token = auth.replace('Bearer ', '');
  const user = router.db.get('users').find({ token }).value();

  if (!user) {
    return res.status(403).json({ error: 'Token inválido' });
  }

  const comprasUsuario = router.db.get('purchases')
  .filter(compra => String(compra.userId) === String(user.id))
  .value();


  res.status(200).json(comprasUsuario);
});

server.use(router);     // todas las colecciones con prefijo /api
/* ───────────────────────── Lanzar servidor ───────────────────────── */
server.listen(3000, () =>
  console.log('  Mock API corriendo en http://localhost:3000 – listo!')
);
