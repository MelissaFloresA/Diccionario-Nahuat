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
        // No se usa porque la BD viene desde assets
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Para futuras versiones (opcional)
    }

    @Throws(IOException::class)
    private fun copiarBaseDatos() {
        val rutaBD = appContext.getDatabasePath(DATABASE_NAME)

        // Crear carpeta si no existe
        rutaBD.parentFile?.mkdirs()

        // Copiar desde assets
        appContext.assets.open(DATABASE_NAME).use { entrada ->
            FileOutputStream(rutaBD).use { salida ->
                entrada.copyTo(salida)
            }
        }
    }

    fun abrirBase(): SQLiteDatabase {
        val rutaBD = appContext.getDatabasePath(DATABASE_NAME)

        try {
            // solo verificar existencia física
            if (!rutaBD.exists()) {
                copiarBaseDatos()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException("Error al copiar la base de datos", e)
        }

        return SQLiteDatabase.openDatabase(
            rutaBD.path,
            null,
            SQLiteDatabase.OPEN_READWRITE
        )
    }
}