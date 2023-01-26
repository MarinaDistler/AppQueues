package com.example.app.admins

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.example.app.BaseActivity
import com.example.app.R
import org.json.JSONObject

class FindWorkerActivity : BaseActivity() {
    val path = "find-worker"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_worker)
        checkRegistered()
    }

    fun findWorker(view: View) {
        val login = findViewById<EditText>(R.id.editTextWorkerLogin).text.toString()
        if (login.isEmpty()) {
            showSnackBar("Login can not be empty")
        } else {
            val answer = network.doHttpGet(path, listOf("login" to login))
            if (network.checkForError(answer, arrayOf("logins"), this)) {
                return
            }
            if ((answer.get("logins") as JSONObject).length() == 0) {
                showSnackBar("Nothing found")
            } else {
                val logins = answer.get("logins") as JSONObject
                val layout = findViewById<LinearLayout>(R.id.layoutInfo)
                layout.visibility = View.VISIBLE
                layout.removeAllViews()
                for (login in logins!!.keys()) {
                    createButton(this, login, ::selectWorker, layout)
                }
            }
        }
    }
    fun selectWorker(view: View) {
        val login = (view as Button).text
        val intent = Intent().putExtra("worker_login", login)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}