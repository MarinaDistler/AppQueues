package com.example.network

import android.app.Activity
import android.widget.Toast
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.json.responseJson
import org.json.JSONObject

class Network {
    val URL = "http://10.0.2.2:8080/"

    fun checkForError(answer: JSONObject, activity: Activity) : Boolean {
        if (answer.has("error")) {
            Toast.makeText(activity, "server error: " + answer.get("error"), Toast.LENGTH_SHORT)
                .show()
            return true
        }
        return false
    }

    fun doHttpGet(path: String, params: List<Pair<String, String>> = listOf()): JSONObject {
        val response = Fuel.get(URL + path, params)
            .responseJson{_, _, _, -> }
            .join()
        println("GET")
        if (!response.body().isEmpty()) {
            val body = JSONObject(response.body().toByteArray().decodeToString())
            println(body)
            return body
        }
        return JSONObject()
    }

    fun doHttpPost(path: String, json: JSONObject = JSONObject(),
                   params: List<Pair<String, String>> = listOf()): JSONObject {
        val response = Fuel.post(URL + path, params)
            .jsonBody(json.toString())
            .responseJson{_, _, _, -> }
            .join()
        println("POST")
        if (!response.body().isEmpty()) {
            val body = JSONObject(response.body().toByteArray().decodeToString())
            println(body)
            return body
        }
        return JSONObject()
    }
}