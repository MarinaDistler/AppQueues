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

class RegisterActivity : BaseActivity() {
    val path = "register"
    var resultLauncherToLogin = registerForActivityResult(ActivityResultContracts.StartActivityForResult())
    { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data
            val name = intent!!.getStringExtra("name")
            if (name == "sign_in") {
                doFinish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        if (isRegistered()) {
            doFinish()
        }
    }

    fun doFinish() {
        val resultIntent = Intent()
        resultIntent.putExtra("name", "register")
        setResult(Activity.RESULT_OK, resultIntent)
        is_registred = true
        finish()
    }

    fun goLogin(view: View) {
        val intent = Intent(this, LoginActivity::class.java)
        resultLauncherToLogin.launch(intent)
    }

    fun doRegister(view: View) {
        val login = findViewById<EditText>(R.id.editTextLogin).text.toString()
        val pass1 = findViewById<EditText>(R.id.editTextPassword1).text.toString()
        val pass2 = findViewById<EditText>(R.id.editTextPassword2).text.toString()
        val shop_name = findViewById<EditText>(R.id.editTextShopName).text.toString()
        val alert_time = findViewById<EditText>(R.id.editAlertTime).text.toString()
        if (login.isEmpty() or pass1.isEmpty() or pass2.isEmpty()) {
            showSnackBar("Fields with * must not be empty!")
        } else if (pass1 != pass2) {
            showSnackBar("Passwords must be the same!")
        } else if (alert_time.isNotEmpty() && alert_time.toIntOrNull() == null) {
            showSnackBar("The alert time must be a number!")
        } else {
            val json = JSONObject().put("login", login).put("password", pass1)
            if (shop_name.isNotEmpty()) {
                json.put("shop_name", shop_name)
            }
            if (alert_time.isNotEmpty()) {
                json.put("alert_time", alert_time.toInt())
            }
            val answer = network.doHttpPost(path, json)
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
