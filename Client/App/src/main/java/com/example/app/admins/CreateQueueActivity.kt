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
    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val result_intent: Intent? = result.data
            val worker = result_intent!!.getStringExtra("worker_login")
            createTextWithDelete(this, worker!!, ::deleteWorker, findViewById<LinearLayout>(R.id.layout_workers))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        network.initSharedPreferences(this)
        setContentView(R.layout.activity_create_queue)
        checkRegistered()
    }

    fun addWorker(view: View) {
        val intent = Intent(this, FindWorkerActivity::class.java)
        resultLauncher.launch(intent)
    }

    fun deleteWorker(view: View) {
        findViewById<LinearLayout>(R.id.layout_workers).removeView(view.parent as View)
    }

    fun saveQueue(view: View) {
        val name = findViewById<EditText>(R.id.editTextQueueName).text.toString()
        if (name.isEmpty()) {
            showSnackBar("Name can not be empty")
            return
        }
        val workers_group = findViewById<LinearLayout>(R.id.layout_workers).children
        val workers = JSONArray()
        for (i in 0..workers_group.count() - 1) {
            val layout = workers_group.elementAt(i) as LinearLayout
            workers.put((layout.get(0) as TextView).text.toString())
        }
        val answer = network.doHttpPost(path, JSONObject()
            .put("name", name).put("workers", workers).put("new", true))
        if (!network.checkForError(answer, arrayOf(), this)) {
            showSnackBar("Creation succeeded!")
            finish()
        }
    }
}