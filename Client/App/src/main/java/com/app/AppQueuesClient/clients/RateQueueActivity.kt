package com.app.AppQueuesClient.clients

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RatingBar
import android.widget.TextView
import com.app.AppQueuesClient.BaseActivity
import com.app.AppQueuesClient.MainActivity
import com.app.R
import org.json.JSONObject

class RateQueueActivity : BaseActivity() {
    private val path = "rate-queue"
    var queue: String? = null
    var shop: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rate_queue)
        queue = intent.getStringExtra("queue")
        findViewById<TextView>(R.id.textQueueName).text = queue
        shop = intent.getStringExtra("shop")
        findViewById<TextView>(R.id.textShopNameRate).text = shop
    }

    fun rateQueue(view: View) {
        val rating = findViewById<RatingBar>(R.id.ratingBar).rating
        val answer = network.doHttpPost(path, JSONObject().put("rating", rating))
        network.checkForError(answer, arrayOf(), this)
        closeNotification(intent.getIntExtra("ntfc_id", -1))
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
