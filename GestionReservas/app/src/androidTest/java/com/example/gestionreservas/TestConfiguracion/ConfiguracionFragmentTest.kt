package com.example.gestionreservas.TestConfiguracion

import android.content.Context
import android.content.SharedPreferences
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerActions
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestionreservas.view.fragment.ConfiguracionFragment
import com.example.gestionreservas.R
import com.example.gestionreservas.view.activities.MainActivity
import com.example.gestionreservas.view.activities.ReservasActivity
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfiguracionFragmentTest {

    private lateinit var prefs: SharedPreferences
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        // Simulamos sesión iniciada
        prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("auth_token", "fake_token_valido")
            .putBoolean("mantener_sesion", true)
            .apply()
    }

    @Test
    fun loginYNavegarAConfiguracionYCerrarSesion() {
        // Paso 1: Lanzar MainActivity
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("my_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        val activity = ActivityScenario.launch(MainActivity::class.java)

        // Paso 2: Rellenar campos y pulsar login
        onView(withId(R.id.editCorreo)).perform(typeText("admin@aether.com"))
        onView(withId(R.id.editPass)).perform(typeText("1234"))
        onView(withId(R.id.btnLogin)).perform(click())

        // Paso 3: Esperar que navegue a ReservasActivity
        onView(withId(R.id.drawer_layout)).check(matches(isDisplayed()))

        // Paso 4: Abrir drawer y entrar a configuración
        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open())
        onView(withId(R.id.configuracion)).perform(click())

        // Paso 5: Pulsar cerrar sesión global
        onView(withId(R.id.tvCerrarSesionGlobal)).check(matches(isDisplayed()))
        onView(withId(R.id.tvCerrarSesionGlobal)).perform(click())

        // Paso 6: Verificar que se borró el token
        val token = prefs.getString("auth_token", null)
        assertNull("El token debería haberse eliminado", token)
    }



}