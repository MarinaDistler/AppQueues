package com.example.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.app.admins.CreateQueueActivity
import com.example.app.admins.ViewAllQueuesActivity

class MainActivity3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
    }
    fun goToActivity4Client(view: View) {
        val intent = Intent(this, MainActivity4Client::class.java)
        startActivity(intent)
    }
    fun goToActivity4Consultant(view: View) {
        val intent = Intent(this, MainActivity4Consultant::class.java)
        startActivity(intent)
    }
    fun goToActivity4Admin(view: View) {
        val intent = Intent(this, ViewAllQueuesActivity::class.java)
        startActivity(intent)
    }
}
