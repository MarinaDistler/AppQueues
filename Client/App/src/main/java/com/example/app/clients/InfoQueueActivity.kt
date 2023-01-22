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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        network.initSharedPreferences(this)
        setContentView(R.layout.activity_info_queue)
        queue = intent.getStringExtra("queue")
        findViewById<TextView>(R.id.textShopName).text = queue
        val queue_id = intent.getIntExtra("queue_id", -1)
        val answer = network.doHttpPost(path, JSONObject().put("queue_id", queue_id))
        network.checkForError(answer, arrayOf(), this)
        handlerThread = HandlerThread("updateInfoThread");
        handlerThread!!.start();
        Handler(handlerThread!!.looper).post(::updateInfo);
    }

    fun updateInfo() {
        val answer = network.doHttpGet(path, listOf("check_status" to false.toString()))
        if (network.checkForError(answer, arrayOf("number", "time", "num_workers"), this)) {
            Handler(handlerThread!!.looper).postDelayed( ::updateInfo, 3000)
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
                Handler(handlerThread!!.looper).postDelayed( ::updateInfo, 3000)
                return
            }
            val status = answer.getString("status")
            if (status == "WORK") {
                val title = "Your turn!"
                val text = "Your window is " + answer.getString("window_name")
                showDialog(title, text)
                val ntfc_id = showNotification(title, text, intent=Intent(this, InfoQueueActivity::class.java))
                Handler(handlerThread!!.looper).post { updateInfoEnd(ntfc_id) }
            } else {
                showSnackBar("error: your status=$status")
                Handler(handlerThread!!.looper).postDelayed( ::updateInfo, 3000)
            }
            return
        }
        Handler(handlerThread!!.looper).postDelayed( ::updateInfo, 3000)
    }
    fun updateInfoEnd(ntfc_id: Int?) {
        val answer = network.doHttpGet(path, listOf("check_status" to true.toString()))
        if (network.checkForError(answer, arrayOf("status"), this)) {
            return
        }
        val status = answer.getString("status")
        if (status == "COMPLITED") {
            handlerThread!!.quitSafely();
            val intent = Intent(this, RateQueueActivity::class.java)
            showNotification("Thank you for using our app", "Please rate the service", ntfc_id, intent)
            intent.putExtra("queue", queue)
            startActivity(intent)
        } else {
            Handler(handlerThread!!.looper).postDelayed( { updateInfoEnd(ntfc_id) }, 3000)
        }
    }
}