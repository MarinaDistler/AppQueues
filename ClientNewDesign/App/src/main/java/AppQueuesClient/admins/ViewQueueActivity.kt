package AppQueuesClient.admins

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import AppQueuesClient.BaseActivity
import com.example.app.R

class ViewQueueActivity : BaseActivity() {
    val path = "edit-queue"
    var queue_name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        network.initSharedPreferences(this)
        setContentView(R.layout.activity_view_queue)
        checkRegistered()
        queue_name = intent.getStringExtra("queue_name")
        val answer = network.doHttpGet(path, listOf("queue_name" to queue_name!!))
        if (network.checkForError(answer, arrayOf("workers"), this)) {
            return
        }
        findViewById<TextView>(R.id.textNameQueueValue).text = queue_name
        val workers = answer.getJSONArray("workers")
        val layout = findViewById<LinearLayout>(R.id.layout_workers)
        for (i in 0..workers.length() - 1) {
            createTextView(this, workers.getString(i), layout)
        }
    }

    fun editQueue(view: View) {
        checkRegistered()
        val intent = Intent(this, EditQueueActivity::class.java)
        intent.putExtra("is_new", false)
        intent.putExtra("queue_name", queue_name)
        startActivity(intent)
    }
}