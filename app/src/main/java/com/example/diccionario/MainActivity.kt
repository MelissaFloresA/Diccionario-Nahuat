package com.example.diccionario

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Fragment inicial
        replaceFragment(InicioFragment())

        bottomNavigation.setOnItemSelectedListener {

            when(it.itemId){

                R.id.nav_home -> replaceFragment(InicioFragment())

                R.id.nav_search -> replaceFragment(BusquedaFragment())

                R.id.nav_favorites -> replaceFragment(FavoritosFragment())
            }

            true
        }
    }

    private fun replaceFragment(fragment: Fragment){

        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()

    }
}