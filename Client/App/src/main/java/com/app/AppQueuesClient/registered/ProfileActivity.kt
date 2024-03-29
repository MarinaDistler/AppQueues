package com.app.AppQueuesClient.registered

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.app.AppQueuesClient.BaseActivity
import android.widget.EditText
import com.app.R
import org.json.JSONObject

class ProfileActivity : BaseActivity() {
    val path = "profile"
    var login: String? = null
    var shop_name: String? = null
    var alert_time: Int? = null
    var alert_time_text: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        network.initSharedPreferences(this)
        setContentView(R.layout.activity_profile)
        checkRegistered()
        val answer = network.doHttpGet(path)
        if (network.checkForError(answer, arrayOf("shop_name", "login", "alert_time"), this)) {
            return
        }
        login = answer.getString("login")
        shop_name = answer.getString("shop_name")
        alert_time = answer.getInt("alert_time")
        alert_time_text = alert_time.toString() + " "
        if (alert_time == 1) {
            alert_time_text += "minute"
        } else {
            alert_time_text += "minutes"
        }
        findViewById<TextView>(R.id.textLoginValue).text = login
        findViewById<TextView>(R.id.textShopNameValue).text = shop_name
        findViewById<TextView>(R.id.textAlertTimeValue).text = alert_time_text
    }

    fun changePassword(view: View) {
        checkRegistered()
        showDialogTwoEditText("Change password", null,true,
            "Save", {dialog, mview ->
                val edit_text1 = mview.findViewById<EditText>(R.id.dialogEditText1)
                val edit_text2 = mview.findViewById<EditText>(R.id.dialogEditText2)
                val text_error = mview.findViewById<TextView>(R.id.dialogTextError)
                val str1 = edit_text1.text.toString()
                val str2 = edit_text2.text.toString()
                if (str1 != str2) {
                    text_error.visibility = View.VISIBLE
                    text_error.text = "Passwords must be the same!"
                } else if (str1.isEmpty()) {
                    text_error.visibility = View.VISIBLE
                    text_error.text = "The password must not be empty!"
                } else {
                    val answer = network.doHttpPost(path, JSONObject().put("password", str1))
                    network.checkForError(answer, arrayOf(), this)
                    dialog.cancel()
                } },
            "Cancel", {dialog, _ -> dialog.cancel()},
            "Password: ", "",
            "Repeat the password:", ""
        )
    }

    fun editLogin(view: View) {
        showDialogEditText("Edit login", null,true,
            "Save", {dialog, mview ->
                val str = mview.findViewById<EditText>(R.id.dialogEditText).text.toString()
                val text_error = mview.findViewById<TextView>(R.id.dialogTextError)
                if (str == "") {
                    text_error.visibility = View.VISIBLE
                    text_error.text = "The login must not be empty!"
                } else {
                    val answer = network.doHttpPost(path, JSONObject().put("login", str))
                    network.checkForError(answer, arrayOf(), this)
                    if (answer.has("notification")) {
                        text_error.visibility = View.VISIBLE
                        text_error.text = answer.getString("notification")
                    } else {
                        restartActivity()
                        dialog.cancel()
                    }
                } },
            "Cancel", {dialog, _ -> dialog.cancel()},
            "Login: ", login
        )
    }

    fun editShopName(view: View) {
        showDialogEditText("Edit shop name", null,true,
            "Save", {dialog, mview ->
                val str = mview.findViewById<EditText>(R.id.dialogEditText).text.toString()
                val text_error = mview.findViewById<TextView>(R.id.dialogTextError)
                if (str == "") {
                    text_error.visibility = View.VISIBLE
                    text_error.text = "The shop name must not be empty!"
                } else {
                    val answer = network.doHttpPost(path, JSONObject().put("shop_name", str))
                    network.checkForError(answer, arrayOf(), this)
                    if (answer.has("notification")) {
                        text_error.visibility = View.VISIBLE
                        text_error.text = answer.getString("notification")
                    } else {
                        restartActivity()
                        dialog.cancel()
                    }
                } },
            "Cancel", {dialog, _ -> dialog.cancel()},
            "Shop name: ", shop_name
        )
    }

    fun editAlertTime(view: View) {
        showDialogEditText("Edit alert time", null,true,
            "Save", {dialog, mview ->
                val str = mview.findViewById<EditText>(R.id.dialogEditText).text.toString()
                val text_error = mview.findViewById<TextView>(R.id.dialogTextError)
                if (str == "") {
                    text_error.visibility = View.VISIBLE
                    text_error.text = "The alert time must not be empty!"
                } else if (str.toIntOrNull() == null) {
                    text_error.visibility = View.VISIBLE
                    text_error.text = "The alert time must be a number!"
                } else {
                    val answer = network.doHttpPost(path, JSONObject().put("alert_time", str.toInt()))
                    network.checkForError(answer, arrayOf(), this)
                    restartActivity()
                    dialog.cancel()
                } },
            "Cancel", {dialog, _ -> dialog.cancel()},
            "Alert time: ", alert_time_text
        )
    }
}