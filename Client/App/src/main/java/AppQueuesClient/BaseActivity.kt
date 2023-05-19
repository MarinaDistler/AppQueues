package AppQueuesClient

import AppQueuesClient.clients.InfoQueueActivity
import AppQueuesClient.registered.LoginActivity
import AppQueuesClient.registered.MainRegisteredActivity
import AppQueuesClient.registered.ProfileActivity
import android.app.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.example.app.R
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import network.Network
import org.json.JSONObject


open class BaseActivity : AppCompatActivity() {
    val network = Network()
    var notification_id = 1
    var channel_id: String? = null
    var notificationManager: NotificationManager? = null
    var is_registred: Boolean? = null
    var resultLauncherMenu = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val name = intent!!.getStringExtra("name")
            if (name == "sign_in" || name == "register") {
                restartActivity()
            }
        }
    }

    private val DELETE_WIDTH = 50


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        network.initSharedPreferences(this)
        channel_id = getString(R.string.app_name)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val importance = NotificationManager.IMPORTANCE_MAX
        val channel = NotificationChannel(channel_id, channel_id + "base", importance)
        channel.enableLights(true)
        channel.lightColor = getColor(R.color.teal_700)
        channel.enableVibration(true)
        notificationManager!!.createNotificationChannel(channel)
        val actionBar = supportActionBar
        actionBar?.setIcon(R.drawable.ic_action_bar)
        actionBar?.setDisplayUseLogoEnabled(true)
        actionBar?.setDisplayShowHomeEnabled(true)
        is_registred = isRegistered()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (is_registred!!) {
            menuInflater.inflate(R.menu.main_registred, menu)
        } else {
            menuInflater.inflate(R.menu.main, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (is_registred!!) {
            when (item.itemId) {
                R.id.item_home -> startActivity(Intent(this, MainRegisteredActivity::class.java))
                R.id.item_profile -> startActivity(Intent(this, ProfileActivity::class.java))
                R.id.item_sign_out -> {
                    val path = "is-registered"
                    val answer = network.doHttpPost(path, JSONObject(), listOf("sign_out" to true.toString()))
                    network.checkForError(answer, arrayOf(), this)
                    is_registred = false
                    restartActivity()
                }
            }
        } else {
            when (item.itemId) {
                R.id.item_home -> startActivity(Intent(this, MainActivity::class.java))
                R.id.item_register -> {
                    resultLauncherMenu.launch(Intent(this, LoginActivity::class.java))
                }
                R.id.item_sign_in -> {
                    resultLauncherMenu.launch(Intent(this, LoginActivity::class.java))
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun restartActivity() {
        val cur_intent = intent
        finish()
        startActivity(cur_intent)
    }

    fun isBackground() : Boolean {
        val runningAppProcessInfo = ActivityManager.RunningAppProcessInfo()
        ActivityManager.getMyMemoryState(runningAppProcessInfo)
        return runningAppProcessInfo.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
    }

    fun showSnackBar(text: String) {
        val snackBarView = Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
        val view = snackBarView.view
        val params = view.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        view.layoutParams = params
        snackBarView.animationMode = BaseTransientBottomBar.ANIMATION_MODE_FADE
        snackBarView.show()
        println(text)
    }

    fun showDialog(title: String, text: String? = null, cancelable: Boolean = false, positive_text: String? = null,
                   positive_action: (dialog: DialogInterface, id: Int) -> Unit = {_, _ -> }, negative_text: String? = null,
                   negative_action: (dialog: DialogInterface, id: Int) -> Unit = {_, _ -> }, internal_view: View? = null) :
    AlertDialog? {
        if (isBackground()) {
            return null
        }
        val textViewTitle = TextView(this)
        textViewTitle.text = title
        textViewTitle.textSize = 30.0F
        textViewTitle.setTypeface(null, Typeface.BOLD)
        textViewTitle.gravity = Gravity.CENTER
        val mBuilder = AlertDialog.Builder(this)
            .setCustomTitle(textViewTitle)
            .setMessage(text)
            .setPositiveButton(positive_text, null)
            .setNegativeButton(negative_text, null)
            .setView(internal_view)
            .setCancelable(cancelable)
        val dialog = mBuilder.create()
        dialog.show()
        val mPositiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        mPositiveButton.setOnClickListener {
            positive_action(dialog, 0)
        }
        val mNegativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        mNegativeButton.setOnClickListener {
            negative_action(dialog, 0)
        }
        return dialog
    }

    fun showDialogEditText(title: String, text: String? = null, cancelable: Boolean = false,
                           positive_text: String? = null, positive_action: (dialog: DialogInterface, view: View) -> Unit = {_, _ -> },
                           negative_text: String? = null, negative_action: (dialog: DialogInterface, view: View) -> Unit = {_, _ -> },
                           name_text_view: String, hint_edit_text: String? = null
    ) : AlertDialog? {
        if (isBackground()) {
            return null
        }
        val linearlayout: View = layoutInflater.inflate(R.layout.dialog_edit_text_1, null)
        linearlayout.findViewById<TextView>(R.id.dialogTextView).text = name_text_view
        val edit_text = linearlayout.findViewById<EditText>(R.id.dialogEditText)
        edit_text.hint = hint_edit_text
        return showDialog(title, text, cancelable, positive_text, {dialog, _ ->
            positive_action(dialog, linearlayout)
        },
            negative_text, {dialog, _ ->
                negative_action(dialog, linearlayout)
            }, linearlayout)
    }

    fun showDialogTwoEditText(title: String, text: String? = null, cancelable: Boolean = false,
                           positive_text: String? = null, positive_action: (dialog: DialogInterface, view: View) -> Unit = {_, _ -> },
                           negative_text: String? = null, negative_action: (dialog: DialogInterface, view: View) -> Unit = {_, _ -> },
                           name_text_view1: String, hint_edit_text1: String? = null,
                              name_text_view2: String, hint_edit_text2: String? = null
    ) : AlertDialog? {
        if (isBackground()) {
            return null
        }
        val linearlayout: View = layoutInflater.inflate(R.layout.dialog_edit_text_2, null)
        linearlayout.findViewById<TextView>(R.id.dialogTextView1).text = name_text_view1
        linearlayout.findViewById<TextView>(R.id.dialogTextView2).text = name_text_view2
        val edit_text1 = linearlayout.findViewById<EditText>(R.id.dialogEditText1)
        val edit_text2 = linearlayout.findViewById<EditText>(R.id.dialogEditText2)
        edit_text1.hint = hint_edit_text1
        edit_text2.hint = hint_edit_text2
        return showDialog(title, text, cancelable, positive_text, {dialog, _ ->
                positive_action(dialog, linearlayout)
            },
            negative_text, {dialog, _ ->
                negative_action(dialog, linearlayout)
            }, linearlayout)
    }

    fun showNotification(title: String, text: String, ntfc_id: Int? = null,
                         intent: Intent = Intent(this, MainActivity::class.java)
    ) : Int? {
        if (!isBackground()) {
            return ntfc_id
        }
        intent.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(this,0,
            intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(this, channel_id!!)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setDefaults(Notification.DEFAULT_ALL)
        var id = ntfc_id
        if (id == null) {
            id = notification_id
            notification_id += 1
        }
        notificationManager!!.notify(id, builder.build())
        return id
    }
    fun closeNotification(ntfc_id: Int) {
        if (ntfc_id != -1) {
            notificationManager!!.cancel(ntfc_id);
        }
    }

    fun dpToPx(dp: Int): Int {
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT))
    }

    fun createButton(activity: Activity, text: String, function: (view: View) -> Unit,
                     layout: LinearLayout, width: Int? = null) : Button {
        val button = Button(activity)
        button.text = text
        button.setOnClickListener { view_button ->
            function(view_button)
        }
        button.isAllCaps = false
        button.backgroundTintList = ColorStateList.valueOf(getColor(R.color.pink))
        button.setTextColor(Color.WHITE)
        button.background = getDrawable(R.drawable.btn_rounded_corner)
        if (width != null) {
            button.layoutParams = LayoutParams(width, LayoutParams.WRAP_CONTENT)
        }
        layout.addView(button)
        println(button.layoutParams.width)
        return button
    }

    fun createTextView(activity: Activity, text: String, layout: LinearLayout, width: Int?=null) : TextView {
        val textView = TextView(activity)
        textView.text = text
        textView.isAllCaps = false
        textView.setTextColor(Color.WHITE)
        if (width != null) {
            textView.layoutParams = LayoutParams(width, LayoutParams.WRAP_CONTENT)
        }
        textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        layout.addView(textView)
        return textView
    }

    fun createTextWithDelete(activity: Activity, text: String, function_delete: (view: View) -> Unit, layout: LinearLayout) {
        val lin_layout = LinearLayout(activity)
        lin_layout.orientation = LinearLayout.HORIZONTAL
        createTextView(activity, text, lin_layout, width=layout.width - dpToPx(DELETE_WIDTH))
        createButton(activity, "X", function_delete, lin_layout, width=dpToPx(DELETE_WIDTH))
        layout.addView(lin_layout)
    }

    fun createButtonWithDelete(activity: Activity, text: String, function: (view: View) -> Unit,
                               function_delete: (view: View) -> Unit, layout: LinearLayout) {
        val lin_layout = LinearLayout(activity)
        lin_layout.orientation = LinearLayout.HORIZONTAL
        createButton(activity, text, function, lin_layout, width=layout.width - dpToPx(DELETE_WIDTH))
        createButton(activity, "X", function_delete, lin_layout, width=dpToPx(DELETE_WIDTH))
        layout.addView(lin_layout)
    }

    fun isUserInQueue() : String? {
        val path = "check-user-in-queue"
        val answer = network.doHttpGet(path)
        if (network.checkForError(answer, arrayOf(), this)) {
            return null
        }
        if (!answer.has("queue")) {
            return null
        }
        return answer.getString("queue")
    }

    fun checkUserInQueue() {
        val queue = isUserInQueue()
        if (queue != null) {
            val intent = Intent(this, InfoQueueActivity::class.java)
            intent.putExtra("queue", queue)
            intent.putExtra("is_in_queue", true)
            startActivity(intent)
        }
    }

    fun isRegistered() : Boolean {
        val path = "is-registered"
        val answer = network.doHttpGet(path)
        if (network.checkForError(answer, arrayOf("is_registered"), this)) {
            return false
        }
        return answer.getBoolean("is_registered")
    }

    fun checkRegistered() {
        is_registred = isRegistered()
        if (!is_registred!!) {
            resultLauncherMenu.launch(Intent(this, LoginActivity::class.java))
        }
    }
}

