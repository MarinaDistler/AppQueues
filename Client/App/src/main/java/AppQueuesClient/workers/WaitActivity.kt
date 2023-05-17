package AppQueuesClient.workers

import AppQueuesClient.BaseActivity
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.app.R
import org.json.JSONObject

class WaitActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        network.initSharedPreferences(this)
        setContentView(R.layout.activity_login)
        checkRegistered()
    }
    fun onClick(view: View) {
        val name = findViewById<EditText>(R.id.editTextLogin).text.toString()
        val pass = findViewById<EditText>(R.id.editTextPassword).text.toString()
        if (name.isEmpty() or pass.isEmpty()) {
            Toast.makeText(this, "both name and pass are required", Toast.LENGTH_SHORT).show()
        } else {
            val answer = network.doHttpPost("hello-servlet", JSONObject()
                .put("login", name)
                .put("password", pass))
            if (answer.has("error")) { // добавить другие ошибки
                Toast.makeText(this, "Login already exists", Toast.LENGTH_SHORT).show()
            }
            else {
                val resultIntent = Intent()
                resultIntent.putExtra("name", "sign_in")
                setResult(Activity.RESULT_OK, resultIntent)
                is_registred = true
                finish()
            }
        }
    }
}
