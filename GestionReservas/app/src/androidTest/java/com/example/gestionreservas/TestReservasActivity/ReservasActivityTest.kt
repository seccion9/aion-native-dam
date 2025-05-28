package com.example.gestionreservas.TestReservasActivity


import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestionreservas.R
import com.example.gestionreservas.view.activities.ReservasActivity
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.work.WorkManager


@RunWith(AndroidJUnit4::class)
class ReservasActivityTest {

    @get:Rule
    val activityRule= ActivityScenarioRule(ReservasActivity::class.java)

    @Test
    fun cerrarSesion_eliminarDatosSharedPreferences(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)

        prefs.edit().putString("auth_token", "token123").putBoolean("mantener_sesion", true).apply()

        val activity = ActivityScenario.launch(ReservasActivity::class.java)
        activity.onActivity {
            it.javaClass.getDeclaredMethod("cerrarSesion", Context::class.java).apply {
                isAccessible = true
                invoke(it, context)
            }
        }

        val token = prefs.getString("auth_token", null)
        val mantenerSesion = prefs.contains("mantener_sesion")

        assertNull(token)
        assertFalse(mantenerSesion)

    }

    @Test
    fun comprobarHomeInicio(){
        val texview= R.id.tvPanelPrincipal
        val activity=ActivityScenario.launch(ReservasActivity::class.java)
        onView(withId(R.id.tvPanelPrincipal))
            .check(matches(isDisplayed()))

    }
    @Test
    fun navegacionListado(){
        val texview=R.id.listado
        val activity=ActivityScenario.launch(ReservasActivity::class.java)
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())

        onView(withId(texview)).perform(click())
        onView(withId(R.id.recyclerReservasListado)).check(matches(isDisplayed()))

    }
    @Test
    fun navegationCalendario(){
        val textview=R.id.calendario
        val activity=ActivityScenario.launch(ReservasActivity::class.java)
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())

        onView(withId(textview)).perform(click())
        onView(withId(R.id.recyclerHorasSalas)).check(matches(isDisplayed()))

    }
    @Test
    fun navegationHome(){
        val textview=R.id.home
        val activity=ActivityScenario.launch(ReservasActivity::class.java)
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())

        onView(withId(textview)).perform(click())
        onView(withId(R.id.tvPanelPrincipal)).check(matches(isDisplayed()))

    }

    @Test
    fun navegationMailing(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("gmail_tokens", Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
        val textview=R.id.mailing
        val activity=ActivityScenario.launch(ReservasActivity::class.java)
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())

        onView(withId(textview)).perform(click())
        onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))

    }

    @Test
    fun navegationConfiguracion(){
        val textview=R.id.configuracion
        val activity=ActivityScenario.launch(ReservasActivity::class.java)
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())

        onView(withId(textview)).perform(click())
        onView(withId(R.id.switchModoOscuro)).check(matches(isDisplayed()))

    }
    @Test
    fun worker_lanzado_si_notificaciones_activadas() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("ajustes", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("notificaciones_activadas", true).apply()

        val activity = ActivityScenario.launch(ReservasActivity::class.java)

        // El test no puede verificar notificación directamente, pero sí que WorkManager esté activo
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork("CheckReservasWorker")
            .get()

        assertFalse("El worker debería estar activo", workInfos.isEmpty())
    }



}