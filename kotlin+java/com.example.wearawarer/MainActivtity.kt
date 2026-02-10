package com.example.wearawarer

import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var layoutUserName: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        layoutUserName = findViewById(R.id.layoutUserName)

        // If you're using a DrawerLayout, initialize it here
        // drawerLayout = findViewById(R.id.drawerLayout)

        // Set click listener for the username + arrow layout
        layoutUserName.setOnClickListener {
            openSidebar()
        }
    }

    private fun openSidebar() {
        // Option 1: If using DrawerLayout
        // drawerLayout.openDrawer(GravityCompat.START)

        // Option 2: Start a new activity
        // startActivity(Intent(this, SidebarActivity::class.java))

        // Option 3: Show a bottom sheet or dialog
        // showSidebarBottomSheet()

        // For now, just show a toast for testing
        android.widget.Toast.makeText(this, "Opening sidebar...", android.widget.Toast.LENGTH_SHORT).show()
    }
}
