package com.example.network

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
import com.github.kittinunf.fuel.json.responseJson
import org.json.JSONObject

class Network {
    val URL = "http://10.0.2.2:8080/"

    fun HttpGet(path: String): JSONObject {
        val response = Fuel.get(URL + path)
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

    fun HttpPost(path: String, json: JSONObject = JSONObject()): JSONObject {
        val response = Fuel.post(URL + path)
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