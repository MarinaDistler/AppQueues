package com.example.app.clients

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.app.BaseActivity
import com.example.app.R
import com.example.network.Network
import org.json.JSONArray
import org.json.JSONObject

class FindShopsActivity : BaseActivity() {
    val path = "find-shops"
    var shops: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_shops) // добавить проверку, что он уже стоит в очереди
    }

    fun scanQr(view: View) {
        //??
    }
    fun findShop(view: View) {
        val name = findViewById<EditText>(R.id.editTextShopName).text.toString()
        if (name.isEmpty()) {
            sendToast("Name can not be empty")
        } else {
            val answer = network.doHttpGet(path, listOf("name" to name))
            if (network.checkForError(answer, arrayOf("shops"), this)) {
                return
            }
            if ((answer.get("shops") as JSONObject).length() == 0) {
                sendToast("Nothing found")
            } else {
                shops = answer.get("shops") as JSONObject
                val layout = findViewById<LinearLayout>(R.id.layoutQueues)
                layout.visibility = View.VISIBLE
                layout.removeAllViews()
                for (shop in shops!!.keys()) {
                    createButton(this, shop, ::joinShop, layout)
                }
            }
        }
    }
    fun joinShop(view: View) {
        val shop = (view as Button).text
        val intent = Intent(this, SelectQueueActivity::class.java)
        intent.putExtra("shop", shop)
        intent.putExtra("shop_id", shops!![shop as String] as Int)
        startActivity(intent)
    }
}