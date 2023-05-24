package com.app.AppQueuesClient.clients

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.view.View
import android.widget.TextView
import com.app.AppQueuesClient.BaseActivity
import com.app.AppQueuesClient.MainActivity
import com.app.R
import org.json.JSONObject

class InfoQueueActivity : BaseActivity() {
    private val path = "info-queue"
    private var handlerThread: HandlerThread? = null
    var queue: String? = null
    var shop: String? = null
    var limit_mins: Int = 5

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info_queue)
        queue = intent.getStringExtra("queue")
        findViewById<TextView>(R.id.textQueueNameInfo).text = queue
        shop = intent.getStringExtra("shop")
        findViewById<TextView>(R.id.textShopNameInfo).text = shop
        val is_in_queue = intent.getBooleanExtra("is_in_queue", false)
        if (!is_in_queue) {
            val queue_id = intent.getIntExtra("queue_id", -1)
            val answer = network.doHttpPost(
                path, JSONObject()
                    .put("queue_id", queue_id).put("queue", queue),
                listOf("add_user" to true.toString())
            )
            network.checkForError(answer, arrayOf(), this)
        }
        if (isRegistered()) {
            val answer = network.doHttpGet(path, listOf("get_limit" to true.toString()))
            network.checkForError(answer, arrayOf("limit_mins"), this)
            limit_mins = answer.getInt("limit_mins")
        }
        handlerThread = HandlerThread("updateInfoThread");
        handlerThread!!.start();
        Handler(handlerThread!!.looper).post(::updateInfo);
    }

    fun updateInfo(ntfc_id: Int? = null) {
        val answer = network.doHttpGet(path, listOf("check_status" to false.toString()))
        if (answer.has("error") && answer.getString("error") ==
                    "[record_id, queue_id] should be in session attributes" && isUserInQueue() == null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            handlerThread!!.quitSafely();
            return
        }
        if (network.checkForError(answer, arrayOf("number", "time", "num_workers", "status"), this)) {
            Handler(handlerThread!!.looper).postDelayed( {updateInfo(ntfc_id)}, 3000)
            return
        }
        val number = answer.getInt("number")
        Handler(Looper.getMainLooper()).post {
            findViewById<TextView>(R.id.textNumber).text = getString(R.string.text_number_queue) +
                    " " + number.toString()
        }
        var ntfc_id_ = ntfc_id
        val text_time = findViewById<TextView>(R.id.textTime)
        var text_time_value: String? = null
        if (answer.getInt("time") == -1) {
            Handler(Looper.getMainLooper()).post { text_time.visibility = View.GONE }
        } else {
            val time = answer.getInt("time")
            val mins: Int = (time % 3600) / 60
            val hours: Int = time / 3600
            var text = getString(R.string.text_predicted_time) + " "
            text_time_value = ""
            if (hours == 1) {
                text_time_value += "$hours hour "
            } else if (hours > 1) {
                text_time_value += "$hours hours "
            }
            if (mins == 1) {
                text_time_value += "$mins minute"
            } else if (mins > 1) {
                text_time_value += "$mins minutes"
            }
            Handler(Looper.getMainLooper()).post {
                text_time.text = text + text_time_value
                text_time.visibility = View.VISIBLE
            }
        }
        Handler(Looper.getMainLooper()).post {
            findViewById<TextView>(R.id.textNumWorkers).text =
                getString(R.string.text_number_workers) + " " +
                        answer.getInt("num_workers").toString()
        }
        val status = answer.getString("status")
        if (status == "WAIT") {
            if (number in 1..3 || answer.getInt("time") / 60 < limit_mins) {
                val title = "Attention!"
                var text = "You are the $number in the queue!"
                if (text_time_value != null) {
                    text += "Your waiting time is $text_time_value! "
                }
                val intent = Intent(this, InfoQueueActivity::class.java)
                intent.putExtra("queue", queue)
                intent.putExtra("is_in_queue", true)
                intent.putExtra("shop", shop)
                ntfc_id_ = showNotification(title, text, ntfc_id, intent)
            }
            Handler(handlerThread!!.looper).postDelayed( {updateInfo(ntfc_id_)}, 3000)
        } else if (status == "WORK") {
            val title = "Your turn!"
            val text = "Your window is " + answer.getString("window_name")
            Handler(Looper.getMainLooper()).post { showDialog(title, text, cancelable = false) }
            val intent = Intent(this, InfoQueueActivity::class.java)
            intent.putExtra("queue", queue)
            intent.putExtra("is_in_queue", true)
            intent.putExtra("shop", shop)
            ntfc_id_ = showNotification(title, text, ntfc_id_, intent)
            Handler(handlerThread!!.looper).post { updateInfoEnd(ntfc_id_) }
        } else if (status == "COMPLETED") {
            Handler(handlerThread!!.looper).post { updateInfoEnd(ntfc_id_) }
        } else if (status == "CANCELED") {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            handlerThread!!.quitSafely()
        } else {
            showSnackBar("error: your status=$status")
            Handler(handlerThread!!.looper).postDelayed( {updateInfo(ntfc_id_)}, 3000)
        }
    }
    fun updateInfoEnd(ntfc_id: Int?) {
        val answer = network.doHttpGet(path, listOf("check_status" to true.toString()))
        if (network.checkForError(answer, arrayOf("status"), this)) {
            return
        }
        val status = answer.getString("status")
        if (status == "COMPLETED") {
            val intent = Intent(this, RateQueueActivity::class.java)
            intent.putExtra("queue", queue)
            intent.putExtra("shop", shop)
            intent.putExtra("ntfc_id", ntfc_id)
            val ntfc_id_ = showNotification("Thank you for using our app", "Please rate the service of queue", ntfc_id, intent)
            intent.putExtra("ntfc_id", ntfc_id_)
            startActivity(intent)
            handlerThread!!.quitSafely();
        } else if (status == "WORK"){
            Handler(handlerThread!!.looper).postDelayed( { updateInfoEnd(ntfc_id) }, 3000)
        } else if (status == "WAIT") {
            Handler(handlerThread!!.looper).postDelayed(::updateInfo, 3000)
        }
    }

    fun exitQueue(view: View) {
        val answer = network.doHttpPost(path, JSONObject(), listOf("exit" to true.toString()))
        network.checkForError(answer, arrayOf(), this)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun skipPlace(view: View) {
        val answer = network.doHttpPost(path, JSONObject(), listOf("skip" to true.toString()))
        network.checkForError(answer, arrayOf(), this)
        if (answer.has("notification")) {
            showSnackBar(answer.getString("notification"))
        }
    }
}