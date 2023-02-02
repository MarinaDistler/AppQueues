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
        setContentView(R.layout.activity_info_queue)
        queue = intent.getStringExtra("queue")
        findViewById<TextView>(R.id.textShopName).text = queue
        val is_in_queue = intent.getBooleanExtra("is_in_queue", false)
        if (!is_in_queue) {
            val queue_id = intent.getIntExtra("queue_id", -1)
            val answer = network.doHttpPost(
                path, JSONObject()
                    .put("queue_id", queue_id).put("queue", queue)
            )
            network.checkForError(answer, arrayOf(), this)
        }
        handlerThread = HandlerThread("updateInfoThread");
        handlerThread!!.start();
        Handler(handlerThread!!.looper).post(::updateInfo);
    }

    fun updateInfo() {
        val answer = network.doHttpGet(path, listOf("check_status" to false.toString()))
        if (answer.has("error") && answer.getString("error") ==
                    "[record_id, queue_id] should be in session attributes" && isUserInQueue() == null) {
            handlerThread!!.quitSafely();
            val intent = Intent(this, FindShopsActivity::class.java)
            startActivity(intent)
        }
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
        if (answer.has("window_name") && answer.has("status")) {
            val status = answer.getString("status")
            if (status == "WAIT") {
                Handler(handlerThread!!.looper).postDelayed( ::updateInfo, 3000)
            } else if (status == "WORK") {
                val title = "Your turn!"
                val text = "Your window is " + answer.getString("window_name")
                showDialog(title, text, cancelable = false)
                val ntfc_id = showNotification(title, text, intent=Intent(this, InfoQueueActivity::class.java))
                Handler(handlerThread!!.looper).post { updateInfoEnd(ntfc_id) }
            } else if (status == "COMPLETED") {
                Handler(handlerThread!!.looper).post { updateInfoEnd(null) }
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
        if (status == "COMPLETED") {
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