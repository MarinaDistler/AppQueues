package com.example.app.clients

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.app.BaseActivity
import com.example.app.R
import org.json.JSONObject

class SelectQueueActivity : BaseActivity() {
    private val path = "find-queues"
    var shop: String? = null
    var shop_id: Int? = null
    var queues: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_queues)
        shop = intent.getStringExtra("shop")
        findViewById<TextView>(R.id.textQueueName).text = shop
        shop_id = intent.getIntExtra("shop_id", -1)
        findQueues()
    }

    fun findQueues() {
        val answer = network.doHttpGet(path, listOf("shop_id" to shop_id.toString()))
        if (network.checkForError(answer, arrayOf("queues"), this)) {
            return
        }
        if ((answer.get("queues") as JSONObject).length() == 0) {
            sendToast("server error: Nothing found")
        } else {
            queues = answer.get("queues") as JSONObject
            val layout = findViewById<LinearLayout>(R.id.layoutQueues)
            layout.visibility = View.VISIBLE
            layout.removeAllViews()
            for (queue in queues!!.keys()) {
                createButton(this, queue, ::joinQueue, layout)
            }
        }
    }
    fun joinQueue(view: View) {
        val queue = (view as Button).text
        val intent = Intent(this, InfoQueueActivity::class.java)
        intent.putExtra("queue", queue)
        intent.putExtra("queue_id", queues!![queue as String] as Int)
        startActivity(intent)
    }
}