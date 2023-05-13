package AppQueuesClient

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.app.R

class MainActivity5Admin : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main5_admin)
        val listServices = findViewById<TextView>(R.id.textListServices)
        val intent = Intent(intent)
        val services = intent.getStringExtra("services")
        val servicesSet = services?.split("\n")?.toSet()
        val servicesString = servicesSet.toString()
        val servicesText = servicesString.subSequence(1, servicesString.length - 1).replace(", ".toRegex(), "\n")
        listServices.text = servicesText
    }
    fun goToActivity6Admin(view: View) {
        val intent = Intent(this, MainActivity6Admin::class.java)
        startActivity(intent)
    }
}
