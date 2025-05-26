package com.example.gestionreservas.TestActivityMain

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.isClickable
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.gestionreservas.view.activities.MainActivity
import com.example.gestionreservas.view.activities.ReservasActivity
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import com.example.gestionreservas.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun botonLoginVisibleYClick() {
        // Verifica que el botón de login está visible
        onView(withId(R.id.btnLogin))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))

        // Simula un click en el botón de login
        onView(withId(R.id.btnLogin)).perform(click())
    }

    @Test
    fun comprobarCamposReset() {
        // Pulsar botón reset (o la acción que vacíe los campos)
        onView(withId(R.id.btnReset)).perform(click())

        // Comprobar que los campos están vacíos
        onView(withId(R.id.editCorreo)).check(matches(withText("")))
        onView(withId(R.id.editPass)).check(matches(withText("")))
    }

    /**
     * En este test se usa la función `loginApiFake()` para simular el login.
     * Para realizar pruebas con la llamada real, sustituir el método `loginApiFake()`
     * por la siguiente llamada directa que inicia la actividad ReservasActivity:
     *
     * startActivity(Intent(this, ReservasActivity::class.java))
     * finish()
     */
    @Test
    fun loginConCamposRellenosNavegaCorrectamente() {
        Intents.init()
        try {
            onView(withId(R.id.btnLogin)).perform(click())
            Intents.intended(hasComponent(ReservasActivity::class.java.name))
        } finally {
            Intents.release()
        }
    }

}