package com.example.app

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.fragment.app.DialogFragment
import com.example.app.clients.InfoQueueActivity
import com.example.network.Network
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import java.security.AccessController.getContext


class BaseDialogFragment(val title: String, val text: String) : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(title)
                .setMessage(text)
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}

open class BaseActivity : AppCompatActivity() {
    val network = Network()
    var notification_id = 1
    var channel_id: String? = null
    var notificationManager: NotificationManager? = null


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

    fun showDialog(title: String, text: String) {
        if (isBackground()) {
            return
        }
        val myDialogFragment = BaseDialogFragment(title, text)
        val manager = supportFragmentManager
        myDialogFragment.show(manager, "myDialog")
    }

    fun showNotification(title: String, text: String, ntfc_id: Int? = null,
                         intent: Intent = Intent(this, MainActivity::class.java)
    ) : Int? {
        if (!isBackground()) {
            return null
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
        button.backgroundTintList = ColorStateList.valueOf(getColor(R.color.teal_700))
        button.setTextColor(Color.WHITE)
        if (width != null) {
            button.width = width
        }
        layout.addView(button)
        return button
    }

    fun createTextView(activity: Activity, text: String, layout: LinearLayout, width: Int?) : TextView {
        val textView = TextView(activity)
        textView.text = text
        textView.isAllCaps = false
        textView.setTextColor(Color.WHITE)
        if (width != null) {
            textView.width = width
        }
        textView.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        layout.addView(textView)
        return textView
    }

    fun createTextWithDelete(activity: Activity, text: String, function: (view: View) -> Unit, layout: LinearLayout) {
        val lin_layout = LinearLayout(activity)
        lin_layout.orientation = LinearLayout.HORIZONTAL
        createTextView(activity, text, lin_layout, width=layout.width - dpToPx(50))
        createButton(activity, "X", function, lin_layout, width=dpToPx(50))
        layout.addView(lin_layout)
    }

    fun checkUserInQueue() {
        val path = "check-user-in-queue"
        val answer = network.doHttpGet(path)
        if (network.checkForError(answer, arrayOf(), this)) {
            return
        }
        if (!answer.has("queue")) {
            return
        }
        val intent = Intent(this, InfoQueueActivity::class.java)
        intent.putExtra("queue", answer.getString("queue"))
        intent.putExtra("is_in_queue", true)
        startActivity(intent)
    }

    fun isRegistered() : Boolean {
        val path = "check-registered"
        val answer = network.doHttpGet(path)
        if (network.checkForError(answer, arrayOf("is_registered"), this)) {
            return false
        }
        return answer.getBoolean("is_registered")
    }

    fun checkRegistered() {
        if (!isRegistered()) {
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
        }
    }
}

