package com.example.app.admins

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.*
import com.example.app.BaseActivity
import com.example.app.R
import org.json.JSONObject

class ViewAllQueuesActivity : BaseActivity() {
    val path = "view-queues"
    val path_delete = "delete-queue"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        network.initSharedPreferences(this)
        setContentView(R.layout.activity_view_all_queues)
        checkRegistered()
        val answer = network.doHttpGet(path, listOf())
        if (network.checkForError(answer, arrayOf("queues"), this)) {
            return
        }
        val queues = answer.getJSONArray("queues")
        val layout = findViewById<LinearLayout>(R.id.layout_queues)
        layout.postDelayed({
            for (i in 0 until queues.length()) {
                createButtonWithDelete(this, queues.getString(i), ::toEditQueue,
                    ::deleteQueue, layout)
            } }, 1)
    }

    fun toEditQueue(view: View) {
        checkRegistered()
        val queue_name = (view as Button).text
        val intent = Intent(this, EditQueueActivity::class.java)
        intent.putExtra("queue_name", queue_name)
        startActivity(intent)
    }

    fun deleteQueue(view: View) {
        checkRegistered()
        val parent = view.parent as LinearLayout
        val queue_name = (parent[0] as Button).text
        showDialog("Do you want to delete the queue $queue_name?", positive_text = "Yes",
            positive_action = {dialog, _ ->
                val answer = network.doHttpPost(path_delete, JSONObject()
                    .put("queue_name", queue_name))
                if (!network.checkForError(answer, arrayOf(), this)) {
                    println("!!!")
                    findViewById<LinearLayout>(R.id.layout_queues).removeView(parent)
                }
                dialog.cancel()
            },
            negative_text = "No", negative_action = {dialog, _ -> dialog.cancel()}
        )
    }

    fun toCreateQueue(view: View) {
        checkRegistered()
        val intent = Intent(this, CreateQueueActivity::class.java)
        intent.putExtra("is_new", true)
        startActivity(intent)
    }
}