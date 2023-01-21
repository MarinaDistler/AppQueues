package com.example.app.clients

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.View
import android.widget.TextView
import com.example.app.BaseActivity
import com.example.app.R
import org.json.JSONObject

class InfoQueueActivity : BaseActivity() {
    private val path = "info-queue"
    private var handlerThread: HandlerThread? = null
    var queue: String? = null
    var queue_id: Int? = null
    var record_id: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_queue)
        queue = intent.getStringExtra("queue")
        findViewById<TextView>(R.id.textQueueName).text = queue
        queue_id = intent.getIntExtra("queue_id", -1)
        val answer = network.doHttpPost(path, JSONObject().put("queue_id", queue_id)) // надо ли куда-то записать user_id?
        network.checkForError(answer, arrayOf("record_id"), this)
        record_id = answer.getInt("record_id")
        handlerThread = HandlerThread("updateInfoThread");
        handlerThread!!.start();
        Handler(handlerThread!!.looper).post(::updateInfo);
    }

    fun updateInfo() { // работает ли при выключенном приложении?
        val answer = network.doHttpGet(path, listOf("record_id" to record_id.toString(), "queue_id" to queue_id.toString()))
        if (network.checkForError(answer, arrayOf("number", "time", "num_workers"), this)) {
            return
        }
        println(answer)
        val number = answer.getInt("number")
        Handler(Looper.getMainLooper()).post {
            findViewById<TextView>(R.id.textNumber).text = getString(R.string.text_number_queue) +
                    " " + number.toString()
        }
        val text_time = findViewById<TextView>(R.id.textTime)
        if (answer.getInt("time") == -1) {
            Handler(Looper.getMainLooper()).post { text_time.visibility = View.GONE }
        } else {
            Handler(Looper.getMainLooper()).post {
                text_time.text = getString(R.string.text_predicted_time) + " " + answer.getInt("time").toString()
            }
        }
        Handler(Looper.getMainLooper()).post {
            findViewById<TextView>(R.id.textNumWorkers).text =
                getString(R.string.text_number_workers) + " " +
                        answer.getInt("num_workers").toString()
        }
        if (number == 0) {
            if (network.checkForError(answer, arrayOf("window_name", "status"), this)) {
                return
            }
            // послать уведомление и закончить таймер и запустить таймер про окончание
            Handler(handlerThread!!.looper).post(::updateInfoEnd)
        }
        Handler(handlerThread!!.looper).postDelayed( ::updateInfo, 3000)
    }
    fun updateInfoEnd() {
        val answer = network.doHttpGet(path, listOf("is_end" to true.toString(), "record_id" to record_id.toString()))
        if (network.checkForError(answer, arrayOf("is_end"), this)) {
            return
        }
        if (answer.getBoolean("is_end")) {
            handlerThread!!.quitSafely();
            val intent = Intent(this, SelectQueueActivity::class.java)
            intent.putExtra("queue", queue)
            intent.putExtra("queue_id", queue_id)
            intent.putExtra("record_id", record_id)
            startActivity(intent)
        } else {
            Handler(handlerThread!!.looper).postDelayed( ::updateInfoEnd, 3000)
        }
    }
}