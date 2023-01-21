package com.example.network

import android.app.Activity
import android.widget.Toast
import com.example.app.BaseActivity
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.json.responseJson
import org.json.JSONObject

class Network {
    val URL = "http://10.0.2.2:8080/"

    fun checkForError(answer: JSONObject, names: Array<String>, activity: BaseActivity) : Boolean {
        if (answer.has("error")) {
            activity.sendToast("server error: " + answer.get("error"))
            return true
        }
        for (name in names) {
            if (!answer.has(name)) {
                activity.sendToast("server error: " + name + " should be in answer")
                return true
            }
        }
        return false
    }

    fun doHttpGet(path: String, params: List<Pair<String, String>> = listOf()): JSONObject {
        val response = Fuel.get(URL + path, params)
            .responseJson{_, _, _, -> }
            .join()
        println("GET " + URL + path)
        if (!response.body().isEmpty()) {
            println(response)
            return JSONObject(response.body().toByteArray().decodeToString())
        }
        return JSONObject()
    }

    fun doHttpPost(path: String, json: JSONObject = JSONObject(),
                   params: List<Pair<String, String>> = listOf()): JSONObject {
        val response = Fuel.post(URL + path, params)
            .jsonBody(json.toString())
            .responseJson{_, _, _, -> }
            .join()
        println("POST " + URL + path)
        if (!response.body().isEmpty()) {
            println(response)
            return JSONObject(response.body().toByteArray().decodeToString())
        }
        return JSONObject()
    }
}