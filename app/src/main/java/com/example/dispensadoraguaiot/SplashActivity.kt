package com.example.dispensadoraguaiot

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Simula la inicialización y decide la ruta lógica (Router)
        Handler(Looper.getMainLooper()).postDelayed({
            val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            val onboardingCompletado = prefs.getBoolean("onboarding_complete", false)

            if (!onboardingCompletado) {
                // Primera vez -> Va a Onboarding
                startActivity(Intent(this, OnboardingActivity::class.java))
            } else {
                // Ya completó la inducción -> Va a Login
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 1500)
    }
}