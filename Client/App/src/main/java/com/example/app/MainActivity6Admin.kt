package com.example.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity6Admin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main6_admin)
    }
    fun goToActivity1(view: View) {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
