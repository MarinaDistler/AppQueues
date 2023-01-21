package com.example.app

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.network.Network

open class BaseActivity : AppCompatActivity() {
    val network = Network()

    fun sendToast(text: String) {
        val toast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
        toast.show()
    }

    fun createButton(activity: Activity, text: String, function: (view: View) -> Unit, layout: LinearLayout) {
        val button = Button(activity)
        button.text = text
        button.setOnClickListener { view_button ->
            function(view_button)
        }
        button.isAllCaps = false
        button.backgroundTintList = ColorStateList.valueOf(getColor(R.color.teal_700))
        button.setTextColor(Color.WHITE)
        layout.addView(button)
    }
}