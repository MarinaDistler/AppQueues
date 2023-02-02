package com.example.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.app.admins.ViewAllQueuesActivity
import com.example.app.clients.FindShopsActivity

class MainActivity : BaseActivity() {
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        println("!!!")
        if (result.resultCode == Activity.RESULT_OK) {
            println("!!@")
            if (isRegistered()) {
                println("!!")
                val button = findViewById<Button>(R.id.button_login)
                button.text = "Your queues"
                button.setOnClickListener(::goToViewAllQueues)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (isRegistered()) {
            val button = findViewById<Button>(R.id.button_login)
            button.text = "Your queues"
            button.setOnClickListener(::goToViewAllQueues)
        }
    }
    fun goToActivity2(view: View) {
        val intent = Intent(this, MainActivity2::class.java)
        resultLauncher.launch(intent)
    }

    fun goToViewAllQueues(view: View) {
        val intent = Intent(this, ViewAllQueuesActivity::class.java)
        startActivity(intent)
    }
    fun goToFindQueueActivity(view: View) {
        val intent = Intent(this, FindShopsActivity::class.java)
        startActivity(intent)
    }
}
