package AppQueuesClient.registered

import AppQueuesClient.BaseActivity
import AppQueuesClient.MainActivity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import AppQueuesClient.admins.ViewAllQueuesActivity
import AppQueuesClient.clients.FindShopsActivity
import com.example.app.R

class MainRegisteredActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_registred)
        if (!isRegistered()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val res = super.onCreateOptionsMenu(menu)
        menu.findItem(R.id.item_home).isVisible = false
        return res
    }

    fun goToViewAllQueues(view: View) {
        val intent = Intent(this, ViewAllQueuesActivity::class.java)
        startActivity(intent)
    }
    fun goToFindQueueActivity(view: View) {
        val intent = Intent(this, FindShopsActivity::class.java)
        startActivity(intent)
    }
}
