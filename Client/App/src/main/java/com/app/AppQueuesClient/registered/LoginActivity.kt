package com.app.AppQueuesClient.registered

import com.app.AppQueuesClient.BaseActivity
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.activity.result.contract.ActivityResultContracts
import com.app.R
import org.json.JSONObject

class LoginActivity : BaseActivity() {
    val path = "login"
    var resultLauncherToRegister = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val name = intent!!.getStringExtra("name")
            if (name == "register") {
                doFinish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        if (isRegistered()) {
            doFinish()
        }
    }

    fun doFinish() {
        val resultIntent = Intent()
        resultIntent.putExtra("name", "sign_in")
        setResult(Activity.RESULT_OK, resultIntent)
        is_registred = true
        finish()
    }

    fun goRegister(view: View) {
        val intent = Intent(this, RegisterActivity::class.java)
        resultLauncherToRegister.launch(intent)
    }

    fun doLogin(view: View) {
        val login = findViewById<EditText>(R.id.editTextLogin).text.toString()
        val pass = findViewById<EditText>(R.id.editTextPassword).text.toString()
        if (login.isEmpty() and pass.isEmpty()) {
            showSnackBar("Both login and password must not be empty!")
        } else if (login.isEmpty()) {
            showSnackBar("Login and password must not be empty!")
        } else if (pass.isEmpty()) {
            showSnackBar("Password must not be empty!")
        } else {
            val answer = network.doHttpPost(path, JSONObject()
                .put("login", login)
                .put("password", pass))
            if (network.checkForError(answer, arrayOf(), this)) {
                return
            }
            if (answer.has("notification")) {
                showSnackBar(answer.getString("notification"))
            } else {
                doFinish()
            }
        }
    }
}
