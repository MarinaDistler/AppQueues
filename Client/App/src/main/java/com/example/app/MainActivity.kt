package com.example.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import com.example.app.admins.ViewAllQueuesActivity
import com.example.app.clients.FindShopsActivity

class MainActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (isRegistered()) {
            val intent = Intent(this, MainRegistredActivity::class.java)
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
