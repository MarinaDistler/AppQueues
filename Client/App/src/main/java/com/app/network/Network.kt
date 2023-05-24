package com.app.network

import android.content.Context
import android.content.SharedPreferences
import com.app.AppQueuesClient.BaseActivity
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.json.responseJson
import org.json.JSONObject

class Network () {
    private val URL = "http://acledit.ru:8080/AppQueuesServer/"
    private val PREFS_NAME = "network_setting"
    var sharedPref: SharedPreferences? = null

    fun initSharedPreferences(activity: BaseActivity) {
        sharedPref = activity.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun checkForError(answer: JSONObject, names: Array<String>, activity: BaseActivity) : Boolean {
        if (answer.has("error")) {
            activity.showSnackBar("server error: " + answer.get("error"))
            return true
        }
        for (name in names) {
            if (!answer.has(name)) {
                activity.showSnackBar("server error: $name should be in answer")
                return true
            }
        }
        return false
    }

    fun doHttpGet(path: String, params: List<Pair<String, String>> = listOf()): JSONObject {
        val request = Fuel.get(URL + path, params)
        val cookie = sharedPref!!.getStringSet("Cookie", null)
        if (cookie != null) {
            request.header(Headers.COOKIE to cookie!!)
        }
        val response = request
            .responseJson{_, _, _, -> }
            .join()
        println("GET $URL$path")
        println(request)
        if (!response.header("Set-Cookie").isEmpty()) {
            val editor = sharedPref!!.edit()
            editor.putStringSet("Cookie", HashSet(response["Set-Cookie"].toMutableList()))
            editor.commit()
        }
        println(response)
        if (!response.header(Headers.CONTENT_TYPE).contains("application/json;charset=UTF-8")) {
            return JSONObject().put("error", "content type of answer is not JSON")
        }
        if (!response.body().isEmpty()) {
            return JSONObject(response.body().toByteArray().decodeToString())
        }
        return JSONObject()
    }

    fun doHttpPost(path: String, json: JSONObject = JSONObject(),
                   params: List<Pair<String, String>> = listOf()): JSONObject {
        val request = Fuel.post(URL + path, params)
        val cookie = sharedPref!!.getStringSet("Cookie", null)
        if (cookie != null) {
            request.header(Headers.COOKIE to cookie!!)
        }
        val response = request
            .jsonBody(json.toString())
            .responseJson{_, _, _, -> }
            .join()
        println("POST $URL$path")
        println(request)
        if (!response.header("Set-Cookie").isEmpty()) {
            val editor = sharedPref!!.edit()
            editor.putStringSet("Cookie", HashSet(response["Set-Cookie"].toMutableList()))
            editor.commit()
        }
        println(response)
        if (!response.header(Headers.CONTENT_TYPE).contains("application/json;charset=UTF-8")) {
            return JSONObject().put("error", "content type of answer is not JSON")
        }
        if (!response.body().isEmpty()) {
            return JSONObject(response.body().toByteArray().decodeToString())
        }
        return JSONObject()
    }
}