package com.mobile.mini_app_android

import org.json.JSONArray
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

object ClienteApi {
    private const val API_URL =
        "http://hwsistemas.homelinux.com/apiclienteteste/api/cliente/retornaclientes?tipo=json"

    fun buscarClientes(): List<Cliente> {
        val connection = (URL(API_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15_000
            readTimeout = 15_000
        }

        return try {
            val statusCode = connection.responseCode
            val stream = if (statusCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val body = BufferedReader(InputStreamReader(stream)).use { it.readText() }

            if (statusCode !in 200..299) {
                throw IOException("HTTP $statusCode: $body")
            }

            parseClientes(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun parseClientes(body: String): List<Cliente> {
        val jsonArray = JSONArray(body)
        val clientes = mutableListOf<Cliente>()

        for (index in 0 until jsonArray.length()) {
            val json = jsonArray.getJSONObject(index)
            clientes.add(
                Cliente(
                    codigo = json.getInt("codigo"),
                    nome = json.optString("nome"),
                    cpf = json.optString("cpf")
                )
            )
        }

        return clientes
    }
}
