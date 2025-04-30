const jsonServer = require('json-server')
const server = jsonServer.create()
const router = jsonServer.router('db.json')
const middlewares = jsonServer.defaults()

server.use(middlewares)

server.use(jsonServer.rewriter({
  '/api/getMonthlyOccupancyByExperienceIdsAndDates': '/monthlyOccupancy',
  '/api/getPurchaseById': '/purchasesById',
  '/api/getExperiencesByIdsAndDate': '/experiencesByDate',
  '/api/getExperiencesByIds': '/experiencesByIds',
  '/api/escaperadar/getOccupancyBetweenDates': '/occupancyER'
}))

server.use(router)

server.listen(3000, () => {
  console.log('JSON Server running with custom /api routes on port 3000')
})
