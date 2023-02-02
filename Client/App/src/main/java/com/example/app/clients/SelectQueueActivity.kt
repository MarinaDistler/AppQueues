package com.example.app.clients

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.app.BaseActivity
import com.example.app.R
import org.json.JSONArray
import org.json.JSONObject

class SelectQueueActivity : BaseActivity() {
    private val path = "find-queues"
    var shop: String? = null
    var queues: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_queues)
        checkUserInQueue()
        shop = intent.getStringExtra("shop")
        findViewById<TextView>(R.id.textShopName).text = shop
        findQueues()
    }

    fun findQueues() {
        val answer = network.doHttpGet(path, listOf("shop" to shop!!))
        if (network.checkForError(answer, arrayOf("queue_names", "queue_ids"), this)) {
            return
        }
        if ((answer.get("queue_names") as JSONArray).length() == 0) {
            showSnackBar("server error: Nothing found")
        } else if ((answer.get("queue_names") as JSONArray).length() != (answer.get("queue_ids") as JSONArray).length()) {
            showSnackBar("server error: length of queue_names and queue_ids should ne same")
        } else {
            val queue_names = answer.get("queue_names") as JSONArray
            val queue_ids = answer.get("queue_ids") as JSONArray
            val layout = findViewById<LinearLayout>(R.id.layout_queues)
            layout.visibility = View.VISIBLE
            layout.removeAllViews()
            queues = JSONObject()
            for (i in 0..queue_names.length() - 1) {
                queues!!.put(queue_names.getString(i), queue_ids.getInt(i))
                createButton(this, queue_names.getString(i), ::joinQueue, layout)
            }
        }
    }
    fun joinQueue(view: View) {
        val queue = (view as Button).text
        val intent = Intent(this, InfoQueueActivity::class.java)
        intent.putExtra("is_in_queue", false)
        intent.putExtra("queue", queue)
        intent.putExtra("queue_id", queues!![queue as String] as Int)
        startActivity(intent)
    }
}