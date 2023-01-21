package com.example.app.clients

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import com.example.app.BaseActivity
import com.example.app.R

class RateQueueActivity : BaseActivity() {
    private val path = "rate-queue"
    var queue: String? = null
    var queue_id: Int? = null
    var record_id: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rate_queue)
        queue = intent.getStringExtra("queue")
        findViewById<TextView>(R.id.textQueueName).text = queue
        queue_id = intent.getIntExtra("queue_id", -1)
        record_id = intent.getIntExtra("record_id", -1)
    }

    fun rateQueue() {
        val answer = network.doHttpGet(
            path,
            listOf("record_id" to record_id.toString(), "queue_id" to queue_id.toString())
        )
        if (network.checkForError(answer, arrayOf(), this)) {
            return
        }
        val number = answer.getInt("number")
        findViewById<TextView>(R.id.textNumber).text = R.string.text_number_queue.toString() +
                number.toString()
        val text_time = findViewById<TextView>(R.id.textNumber)
        if (answer.getInt("time") == -1) {
            text_time.visibility = View.GONE
        } else {
            text_time.text =
                R.string.text_predicted_time.toString() + answer.getInt("time").toString()
        }
        findViewById<TextView>(R.id.textNumWorkers).text = R.string.text_number_workers.toString() +
                answer.getInt("num_workers").toString()
        if (number == 0) {
            if (network.checkForError(answer, arrayOf("window_name", "status"), this)) {
                return
            }
            // послать уведомление и закончить таймер и запустить таймер про окончание
        }
    }

    fun updateInfoEnd() {
        val answer = network.doHttpGet(
            path,
            listOf("is_end" to true.toString(), "record_id" to record_id.toString())
        )
        if (network.checkForError(answer, arrayOf("is_end"), this)) {
            return
        }
    }
}
