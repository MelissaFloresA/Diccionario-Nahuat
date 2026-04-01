package com.example.diccionario.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.FileOutputStream
import java.io.IOException

class DBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "diccionario_nahuat.db"
        private const val DATABASE_VERSION = 1
    }

    private val appContext = context.applicationContext

    override fun onCreate(db: SQLiteDatabase) {
        // No hacer nada - la base de datos se copia desde assets
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Para futuras actualizaciones
        if (oldVersion < newVersion) {
            // Aquí se puede implementar migración de datos
        }
    }

    @Throws(IOException::class)
    private fun copiarBaseDatos() {
        val dbPath = appContext.getDatabasePath(DATABASE_NAME)

        // Crear directorio si no existe
        dbPath.parentFile?.mkdirs()

        // Copiar desde assets
        appContext.assets.open(DATABASE_NAME).use { input ->
            FileOutputStream(dbPath).use { output ->
                input.copyTo(output)
            }
        }
    }

    fun abrirBase(): SQLiteDatabase {
        // Verificar si la base de datos existe y copiarla si es necesario
        val dbPath = appContext.getDatabasePath(DATABASE_NAME)
        if (!dbPath.exists()) {
            try {
                copiarBaseDatos()
            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException("Error al copiar la base de datos", e)
            }
        }
        return this.readableDatabase
    }
}