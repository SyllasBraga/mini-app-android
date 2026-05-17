package com.mobile.mini_app_android

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ClienteDbHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABLE_CLIENTES (
                $COL_CODIGO INTEGER PRIMARY KEY,
                $COL_NOME TEXT NOT NULL,
                $COL_CPF TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CLIENTES")
        onCreate(db)
    }

    fun buscarTodos(): List<Cliente> {
        val clientes = mutableListOf<Cliente>()
        readableDatabase.query(
            TABLE_CLIENTES,
            arrayOf(COL_CODIGO, COL_NOME, COL_CPF),
            null,
            null,
            null,
            null,
            "$COL_CODIGO ASC"
        ).use { cursor ->
            val codigoIndex = cursor.getColumnIndexOrThrow(COL_CODIGO)
            val nomeIndex = cursor.getColumnIndexOrThrow(COL_NOME)
            val cpfIndex = cursor.getColumnIndexOrThrow(COL_CPF)

            while (cursor.moveToNext()) {
                clientes.add(
                    Cliente(
                        codigo = cursor.getInt(codigoIndex),
                        nome = cursor.getString(nomeIndex),
                        cpf = cursor.getString(cpfIndex)
                    )
                )
            }
        }
        return clientes
    }

    fun salvarClientes(clientes: List<Cliente>): Int {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            clientes.forEach { cliente ->
                val values = ContentValues().apply {
                    put(COL_CODIGO, cliente.codigo)
                    put(COL_NOME, cliente.nome)
                    put(COL_CPF, cliente.cpf)
                }
                db.insertWithOnConflict(
                    TABLE_CLIENTES,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }
            db.setTransactionSuccessful()
            clientes.size
        } finally {
            db.endTransaction()
        }
    }

    fun excluirPorCodigos(codigos: Collection<Int>): Int {
        val db = writableDatabase
        var removidos = 0
        db.beginTransaction()
        return try {
            codigos.forEach { codigo ->
                removidos += db.delete(
                    TABLE_CLIENTES,
                    "$COL_CODIGO = ?",
                    arrayOf(codigo.toString())
                )
            }
            db.setTransactionSuccessful()
            removidos
        } finally {
            db.endTransaction()
        }
    }

    companion object {
        private const val DATABASE_NAME = "mini_clientes.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_CLIENTES = "clientes"
        private const val COL_CODIGO = "codigo"
        private const val COL_NOME = "nome"
        private const val COL_CPF = "cpf"
    }
}
