package com.example.app.admins

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.example.app.BaseActivity
import com.example.app.R
import com.example.app.clients.SelectQueueActivity
import org.json.JSONObject

class EditQueueActivity : BaseActivity() {
    val path = "edit-queue"
    var shops: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        network.initSharedPreferences(this)
        setContentView(R.layout.activity_create_queue)
    }

    fun joinShop(view: View) {
        val shop = (view as Button).text
        val intent = Intent(this, SelectQueueActivity::class.java)
        intent.putExtra("shop", shop)
        intent.putExtra("shop_id", shops!![shop as String] as Int)
        startActivity(intent)
    }
}