package com.example.app.clients

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app.BaseActivity
import com.example.app.MainActivity3
import com.example.app.R
import com.example.network.Network
import org.json.JSONArray
import org.json.JSONObject

class SelectQueueActivity : BaseActivity() {
    var shop: String? = null
    var shop_id: Int? = null
    var queues: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_queues)
        shop = intent.getStringExtra("shop")
        findViewById<TextView>(R.id.textShopName).text = shop
        shop_id = intent.getIntExtra("shop_id", -1)
        findQueues()
    }

    fun findQueues() {
        val answer = network.doHttpGet("find-queues", listOf("shop_id" to shop_id.toString()))
        if (network.checkForError(answer, this)) {
            return
        }
        if (!answer.has("queues")) {
            sendToast("server error: queues not found in answer")
        } else if ((answer.get("queues") as JSONObject).length() == 0) {
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
        val intent = Intent(this, SelectQueueActivity::class.java) //?
        intent.putExtra("queue", queue)
        intent.putExtra("queue_id", queues!![queue as String] as Int)
        startActivity(intent)
    }
}