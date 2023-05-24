package com.app.AppQueuesClient.admins

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.get
import com.app.AppQueuesClient.BaseActivity
import com.app.R
import org.json.JSONArray
import org.json.JSONObject

class EditQueueActivity : BaseActivity() {
    val path = "edit-queue"
    var is_new: Boolean = true
    var queue_name: String? = null
    var workers = mutableListOf<String>()
    val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val result_intent: Intent? = result.data
            val worker = result_intent!!.getStringExtra("worker_login")
            if (workers.contains(worker)) {
                showSnackBar("You already have this worker in this queue!")
            } else {
                workers.add(worker!!)
                createTextWithDelete(
                    this, worker!!, ::deleteWorker,
                    findViewById<LinearLayout>(R.id.layout_workers)
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        network.initSharedPreferences(this)
        setContentView(R.layout.activity_edit_queue)
        checkRegistered()
        is_new = intent.getBooleanExtra("is_new", true)
        if (!is_new) {
            queue_name = intent.getStringExtra("queue_name")
            findViewById<TextView>(R.id.textCreateQueue).text = "Edit the queue"
            val answer = network.doHttpGet(path, listOf("queue_name" to queue_name!!))
            if (network.checkForError(answer, arrayOf("workers"), this)) {
                return
            }
            findViewById<EditText>(R.id.editTextQueueName).setText(queue_name!!)
            val workers_json = answer.getJSONArray("workers")
            workers = mutableListOf<String>()
            for (i in 0 until workers_json.length()) {
                workers.add(workers_json.getString(i))
            }
            val layout = findViewById<LinearLayout>(R.id.layout_workers)
            layout.postDelayed({
                for (worker in workers) {
                    createTextWithDelete(this, worker, ::deleteWorker, layout)
                } }, 1)
        }
    }

    fun addWorker(view: View) {
        val intent = Intent(this, FindWorkerActivity::class.java)
        resultLauncher.launch(intent)
    }

    fun deleteWorker(view: View) {
        val parent = view.parent as LinearLayout
        val worker = (parent[0] as TextView).text
        workers.remove(worker)
        findViewById<LinearLayout>(R.id.layout_workers).removeView(parent)
    }

    fun saveQueue(view: View) {
        checkRegistered()
        val name = findViewById<EditText>(R.id.editTextQueueName).text.toString()
        if (name.isEmpty()) {
            showSnackBar("Name can not be empty")
            return
        }
        val answer = network.doHttpPost(path, JSONObject()
            .put("name", name).put("workers", JSONArray(workers))
            .put("new", is_new).put("old_name", queue_name))
        if (!network.checkForError(answer, arrayOf(), this)) {
            showSnackBar("Saving succeeded!")
            val intent = Intent(this, ViewAllQueuesActivity::class.java)
            startActivity(intent)
        }
    }
}