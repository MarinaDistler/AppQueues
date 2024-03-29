package com.app.AppQueuesClient

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import com.app.AppQueuesClient.admins.ViewAllQueuesActivity
import com.app.AppQueuesClient.clients.FindShopsActivity
import com.app.AppQueuesClient.registered.MainRegisteredActivity
import com.app.R

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (isRegistered()) {
            val intent = Intent(this, MainRegisteredActivity::class.java)
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
