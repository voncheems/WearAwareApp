package com.example.wearawarer

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.wearawarer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Log.w("MainActivity", "Notification permission denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        askNotificationPermission()
        setupDrawer()
        setupBottomNav()

        if (savedInstanceState == null) {
            loadFragment(HomeFragment(), R.id.nav_home)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else if (binding.bottomNavigationView.selectedItemId != R.id.nav_home) {
                    navigateTo(R.id.nav_home)
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    fun navigateTo(itemId: Int) {
        binding.bottomNavigationView.selectedItemId = itemId
    }

    private fun loadFragment(fragment: Fragment, itemId: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
        binding.bottomNavigationView.selectedItemId = itemId
    }

    private fun setupDrawer() {
        // Update sidebar header with user info
        val headerView = binding.navigationView.getHeaderView(0)
        val tvNavUserName = headerView.findViewById<TextView>(R.id.tvNavUserName)
        val tvNavUserRole = headerView.findViewById<TextView>(R.id.tvNavUserRole)
        
        val prefs = getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
        val userName = prefs.getString("user_name", "Inspector")
        
        tvNavUserName.text = userName?.uppercase() ?: "INSPECTOR"
        tvNavUserRole.text = "INSPECTOR"

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_my_team -> navigateTo(R.id.nav_team)
                R.id.nav_inspections -> navigateTo(R.id.nav_inspections)
                R.id.nav_history -> navigateTo(R.id.nav_history)
                R.id.nav_help -> Toast.makeText(this, "Help", Toast.LENGTH_SHORT).show()
                R.id.nav_logout -> performLogout()
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun performLogout() {
        val prefs = getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
        
        // ONLY clear session data, keep "remembered_email"
        prefs.edit {
            remove("token")
            remove("user_id")
            remove("user_name")
            remove("user_email")
            putBoolean("is_logged_in", false)
        }

        // Stop the violation service
        val serviceIntent = Intent(this, ViolationService::class.java)
        stopService(serviceIntent)
        
        Intent(this, LoginActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    private fun setupBottomNav() {
        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val fragment = when (menuItem.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_alerts -> AlertsFragment()
                R.id.nav_inspections -> InspectionsFragment()
                R.id.nav_history -> HistoryFragment()
                R.id.nav_team -> TeamFragment()
                R.id.nav_settings -> SettingsFragment()
                else -> return@setOnItemSelectedListener false
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
            true
        }
    }
}
