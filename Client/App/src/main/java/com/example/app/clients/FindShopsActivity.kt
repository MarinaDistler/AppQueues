package com.example.app.clients

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.example.app.BaseActivity
import com.example.app.R
import org.json.JSONArray
import org.json.JSONObject

class FindShopsActivity : BaseActivity() {
    val path = "find-shops"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_shops)
        checkUserInQueue()
    }

    fun scanQr(view: View) {
        //??
    }
    fun findShop(view: View) {
        val name = findViewById<EditText>(R.id.editTextWorkerLogin).text.toString()
        if (name.isEmpty()) {
            showSnackBar("Name can not be empty")
        } else {
            val answer = network.doHttpGet(path, listOf("name" to name))
            if (network.checkForError(answer, arrayOf("shop_names"), this)) {
                return
            }
            if ((answer.get("shop_names") as JSONArray).length() == 0) {
                showSnackBar("Nothing found")
            } else {
                val shops = answer.get("shop_names") as JSONArray
                val layout = findViewById<LinearLayout>(R.id.layoutInfo)
                layout.visibility = View.VISIBLE
                layout.removeAllViews()
                for (i in 0.. shops.length() - 1) {
                    createButton(this, shops.getString(i), ::joinShop, layout)
                }
            }
        }
    }
    fun joinShop(view: View) {
        val shop = (view as Button).text
        val intent = Intent(this, SelectQueueActivity::class.java)
        intent.putExtra("shop", shop)
        startActivity(intent)
    }
}