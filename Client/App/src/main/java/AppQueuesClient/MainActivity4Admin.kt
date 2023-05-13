package AppQueuesClient

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app.R

class MainActivity4Admin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main4_admin)
    }
    fun onClick(view: View) {
        val text = findViewById<EditText>(R.id.editTextTextMultiLine)
        if (text.text.toString().isEmpty()) {
            Toast.makeText(this, "at least one service is required", Toast.LENGTH_SHORT).show()
        } else {
            goToActivity5Admin(view, text)
        }
    }
    fun goToActivity5Admin(view: View, text: EditText) {
        val intent = Intent(this, MainActivity5Admin::class.java)
        intent.putExtra("services", text.text.toString())
        startActivity(intent)
    }
}
