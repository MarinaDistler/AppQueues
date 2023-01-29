package com.example.app.admins

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.children
import androidx.core.view.get
import com.example.app.BaseActivity
import com.example.app.R
import com.example.app.clients.SelectQueueActivity
import org.json.JSONArray
import org.json.JSONObject

class CreateQueueActivity : BaseActivity() {
    val path = "edit-queue"
    var is_new: Boolean = true
    var queue_name: String? = null
    var workers = mutableListOf<String>()
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
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
        setContentView(R.layout.activity_create_queue)
        checkRegistered()
        is_new = intent.getBooleanExtra("is_new", true)
        if (!is_new) {
            queue_name = intent.getStringExtra("queue_name")
            val answer = network.doHttpGet(path, listOf("queue_name" to queue_name!!))
            if (network.checkForError(answer, arrayOf("workers"), this)) {
                return
            }
            findViewById<EditText>(R.id.editTextQueueName).setText(queue_name!!)
            workers = answer.getJSONArray("workers") as MutableList<String>
            val layout = findViewById<LinearLayout>(R.id.layout_workers)
            for (worker in workers) {
                createTextWithDelete(this, worker, ::deleteWorker, layout)
            }
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
            .put("name", name).put("workers", JSONArray(workers)).put("new", true))
        if (!network.checkForError(answer, arrayOf(), this)) {
            showSnackBar("Creation succeeded!")
            // finish()
            val intent = Intent(this, EditQueueActivity::class.java)
            intent.putExtra("queue_name", name)
            startActivity(intent)
        }
    }
}