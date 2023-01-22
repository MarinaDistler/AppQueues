package com.example.app.clients

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import com.example.app.BaseActivity
import com.example.app.MainActivity
import com.example.app.R
import org.json.JSONObject

class RateQueueActivity : BaseActivity() {
    private val path = "rate-queue"
    var queue: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        network.initSharedPreferences(this)
        setContentView(R.layout.activity_rate_queue)
        queue = intent.getStringExtra("queue")
        findViewById<TextView>(R.id.textQueueName).text = queue
    }

    fun rateQueue(view: View) {
        val rating = findViewById<RatingBar>(R.id.ratingBar).rating
        network.doHttpPost(path, JSONObject().put("rating", rating))
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
