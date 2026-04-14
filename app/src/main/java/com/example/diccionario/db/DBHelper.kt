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
        private const val MIS_PREFERENCIAS = "mis_preferencias"
        private const val BD_COPIADA = "bd_copiada"
    }

    private val appContext = context.applicationContext
    private val preferencias = appContext.getSharedPreferences(MIS_PREFERENCIAS, Context.MODE_PRIVATE)

    override fun onCreate(db: SQLiteDatabase) {
        // No necesito hacer nada aquí porque la BD ya viene lista desde assets
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Por ahora no necesito actualizar la BD
    }

    // Esta función se encarga de copiar la BD desde la carpeta assets al teléfono
    @Throws(IOException::class)
    private fun copiarBaseDatos() {
        val rutaBD = appContext.getDatabasePath(DATABASE_NAME)

        // Crear la carpeta si no existe
        rutaBD.parentFile?.mkdirs()

        // Copiar el archivo
        appContext.assets.open(DATABASE_NAME).use { entrada ->
            FileOutputStream(rutaBD).use { salida ->
                entrada.copyTo(salida)
            }
        }
    }

    // Función principal para abrir la base de datos
    fun abrirBase(): SQLiteDatabase {
        val rutaBD = appContext.getDatabasePath(DATABASE_NAME)

        // Verificar si ya copiamos la BD antes
        val yaCopiada = preferencias.getBoolean(BD_COPIADA, false)

        // Solo copio la BD si es la primera vez que se instala la app o si no existe
        if (!yaCopiada || !rutaBD.exists()) {
            try {
                // Si la BD existe pero es primera instalación, la borro para copiar una nueva
                if (rutaBD.exists() && !yaCopiada) {
                    rutaBD.delete()
                }

                copiarBaseDatos()

                // Guardo en preferencias que ya copié la BD
                preferencias.edit().putBoolean(BD_COPIADA, true).apply()

            } catch (e: Exception) {
                e.printStackTrace()
                throw RuntimeException("Error al copiar la base de datos", e)
            }
        }

        // Devuelvo la BD lista para usar
        return this.writableDatabase
    }
}