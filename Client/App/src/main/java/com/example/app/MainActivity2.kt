package com.example.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.network.Network
import org.json.JSONObject

class MainActivity2 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
    }
    fun onClick(view: View) {
        val name = findViewById<EditText>(R.id.editTextTextPersonName).text.toString()
        val pass = findViewById<EditText>(R.id.editTextPassword).text.toString()
        if (name.isEmpty() or pass.isEmpty()) {
            Toast.makeText(this, "both name and pass are required", Toast.LENGTH_SHORT).show()
        } else if (Network().HttpPost("hello-servlet", JSONObject()
                    .put("login", name)
                    .put("password", pass)).has("error")) { // добавить другие ошибки
            Toast.makeText(this, "Login already exists", Toast.LENGTH_SHORT).show()
        }
        else {
            Network().HttpGet("hello-servlet") // проверка для дебага
            goToActivity3(view)
        }
    }
    fun goToActivity3(view: View) {
        val intent = Intent(this, MainActivity3::class.java)
        startActivity(intent)
    }
}