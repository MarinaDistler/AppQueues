package com.example.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import org.json.JSONObject

class MainActivity2 : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        network.initSharedPreferences(this)
        setContentView(R.layout.activity_main2)
        if (isRegistered()) {
            val intent = Intent(this, MainActivity3::class.java)
            startActivity(intent)
        }
    }
    fun onClick(view: View) {
        val name = findViewById<EditText>(R.id.editTextWorkerLogin).text.toString()
        val pass = findViewById<EditText>(R.id.editTextPassword).text.toString()
        if (name.isEmpty() or pass.isEmpty()) {
            Toast.makeText(this, "both name and pass are required", Toast.LENGTH_SHORT).show()
        } else {
            val answer = network.doHttpPost("hello-servlet", JSONObject()
                .put("login", name)
                .put("password", pass))
            println(answer)
            if (answer.has("error")) { // добавить другие ошибки
                Toast.makeText(this, "Login already exists", Toast.LENGTH_SHORT).show()
            }
            else {
                setResult(Activity.RESULT_OK, Intent())
                finish()
            }
        }
    }
}
