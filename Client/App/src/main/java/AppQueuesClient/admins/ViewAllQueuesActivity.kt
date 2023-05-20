package AppQueuesClient.admins

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.*
import AppQueuesClient.BaseActivity
import android.widget.EditText
import android.widget.TextView
import com.example.app.R
import org.json.JSONObject

class ViewAllQueuesActivity : BaseActivity() {
    val path = "view-queues"
    val path_delete = "delete-queue"
    val path_profile = "profile"
    var shop_name: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        network.initSharedPreferences(this)
        setContentView(R.layout.activity_view_all_queues)
        checkRegistered()
        var answer = network.doHttpGet(path_profile)
        if (network.checkForError(answer, arrayOf("shop_name", "login"), this)) {
            return
        }
        shop_name = answer.getString("shop_name")
        if (shop_name == "") {
            showSnackBar("You do not have shop name!")
            showDialogEditText("Create shop name", null,false,
                "Save", {dialog, mview ->
                    val str = mview.findViewById<EditText>(R.id.dialogEditText).text.toString()
                    val text_error = mview.findViewById<TextView>(R.id.dialogTextError)
                    if (str == "") {
                        text_error.visibility = View.VISIBLE
                        text_error.text = "The shop name must not be empty!"
                    } else {
                        val answer = network.doHttpPost(path_profile, JSONObject().put("shop_name", str))
                        network.checkForError(answer, arrayOf(), this)
                        restartActivity()
                        dialog.cancel()
                    } },
                null, {dialog, _ -> dialog.cancel()},
                "Shop name: ", shop_name
            )
        }
        findViewById<TextView>(R.id.textShopNameViewAllQueues).text = shop_name
        answer = network.doHttpGet(path, listOf())
        if (network.checkForError(answer, arrayOf("queues"), this)) {
            return
        }
        val queues = answer.getJSONArray("queues")
        if (queues.length() == 0) {
            findViewById<TextView>(R.id.textNoQueues).visibility = View.VISIBLE
        } else {
            val layout = findViewById<LinearLayout>(R.id.layout_queues)
            layout.postDelayed({
                for (i in 0 until queues.length()) {
                    createButtonWithDelete(
                        this, queues.getString(i), ::toEditQueue,
                        ::deleteQueue, layout
                    )
                }
            }, 1)
        }
    }

    fun editShopName(view: View?) {
        showDialogEditText("Edit shop name", null,true,
            "Save", {dialog, mview ->
                val str = mview.findViewById<EditText>(R.id.dialogEditText).text.toString()
                val text_error = mview.findViewById<TextView>(R.id.dialogTextError)
                val answer = network.doHttpPost(path_profile, JSONObject().put("shop_name", str))
                network.checkForError(answer, arrayOf(), this)
                if (answer.has("notification")) {
                    text_error.visibility = View.VISIBLE
                    text_error.text = answer.getString("notification")
                } else {
                    restartActivity()
                    dialog.cancel()
                } },
            "Cancel", {dialog, _ -> dialog.cancel()},
            "Shop name: ", shop_name
        )
    }

    fun toEditQueue(view: View) {
        checkRegistered()
        val queue_name = (view as Button).text
        val intent = Intent(this, ViewQueueActivity::class.java)
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
        val intent = Intent(this, EditQueueActivity::class.java)
        intent.putExtra("is_new", true)
        startActivity(intent)
    }
}