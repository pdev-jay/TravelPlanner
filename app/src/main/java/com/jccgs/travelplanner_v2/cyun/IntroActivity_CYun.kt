package com.jccgs.travelplanner_v2.cyun

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.airbnb.lottie.LottieAnimationView
import com.jccgs.travelplanner_v2.R

class IntroActivity_CYun : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro_cyun)

        var lodingImage = findViewById(R.id.loading_image) as LottieAnimationView

        lodingImage.playAnimation()

        val handler = Handler()
        handler.postDelayed({
            val intent = Intent(this, LogInActivity_CYun::class.java)
            startActivity(intent)
            finish()
        },3000)

    }
}