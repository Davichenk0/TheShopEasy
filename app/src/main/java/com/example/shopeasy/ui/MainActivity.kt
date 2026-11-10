package com.example.shopeasy.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.example.shopeasy.R
import com.example.shopeasy.ui.main.products.SearchFragment
import com.example.shopeasy.ui.main.admin.AdminFragment
import com.example.shopeasy.ui.main.admin.UsersFragment
import com.example.shopeasy.ui.main.products.CatalogFragment
import com.example.shopeasy.ui.main.products.HomeFragment
import com.example.shopeasy.ui.main.products.ProductsFragment
import com.example.shopeasy.ui.main.profile.ProfileFragment
import com.example.shopeasy.ui.main.products.ShoppingCardFragment

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        drawerLayout = findViewById(R.id.drawer_layout)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val navView = findViewById<NavigationView>(R.id.nav_view)

        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navegation_drawer_open,
            R.string.navegation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.white)

        cargarFragment(SearchFragment())
        bottomNav.selectedItemId = R.id.nav_search

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_search -> cargarFragment(fragment = SearchFragment())
                R.id.nav_products -> cargarFragment(fragment = HomeFragment())
                R.id.nav_shopping_cart -> cargarFragment(fragment = ShoppingCardFragment())
                R.id.nav_profile -> cargarFragment(fragment = ProfileFragment())
            }
            true
        }

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_search -> cargarFragment(fragment = SearchFragment())
                R.id.nav_catalog -> cargarFragment(fragment = CatalogFragment())
                R.id.nav_admin -> cargarFragment(fragment = AdminFragment())
                R.id.nav_users -> cargarFragment(fragment = UsersFragment())
                R.id.nav_logout -> finish()
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun cargarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
