package com.example.wearawarer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.wearawarer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val homeFragment        = HomeFragment()
    private val alertsFragment      = AlertsFragment()
    private val inspectionsFragment = InspectionsFragment()
    private val historyFragment     = HistoryFragment()
    private val teamFragment        = TeamFragment()

    private var activeFragment: Fragment = homeFragment

    private val tabOrder = listOf(
        R.id.nav_home,
        R.id.nav_alerts,
        R.id.nav_inspections,
        R.id.nav_history,
        R.id.nav_team
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDrawer()
        setupBottomNav()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                add(R.id.fragmentContainer, homeFragment,        "home")
                add(R.id.fragmentContainer, alertsFragment,      "alerts")
                add(R.id.fragmentContainer, inspectionsFragment, "inspections")
                add(R.id.fragmentContainer, historyFragment,     "history")
                add(R.id.fragmentContainer, teamFragment,        "team")
                hide(alertsFragment)
                hide(inspectionsFragment)
                hide(historyFragment)
                hide(teamFragment)
            }.commit()
        }

        // Handle notification tap when app is launched fresh
        handleNotificationIntent(intent)

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

    // Handle notification tap when app is already running in background
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent?.getStringExtra("navigate_to") == "alerts") {
            navigateTo(R.id.nav_alerts)
        }
    }

    fun openDrawer() {
        binding.drawerLayout.openDrawer(GravityCompat.START)
    }

    fun navigateTo(itemId: Int) {
        binding.bottomNavigationView.selectedItemId = itemId
    }

    private fun showFragment(fragment: Fragment, toItemId: Int) {
        if (fragment === activeFragment) return

        val fromIndex = tabOrder.indexOf(binding.bottomNavigationView.selectedItemId)
        val toIndex   = tabOrder.indexOf(toItemId)
        val goingRight = toIndex > fromIndex

        val enter = if (goingRight) R.anim.slide_in_right else R.anim.slide_in_left
        val exit  = if (goingRight) R.anim.slide_out_left else R.anim.slide_out_right

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(enter, exit)
            .hide(activeFragment)
            .show(fragment)
            .commit()

        activeFragment = fragment
    }

    private fun setupDrawer() {
        val headerView    = binding.navigationView.getHeaderView(0)
        val tvNavUserName = headerView.findViewById<TextView>(R.id.tvNavUserName)
        val tvNavUserRole = headerView.findViewById<TextView>(R.id.tvNavUserRole)

        val prefs    = getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
        val userName = prefs.getString("user_name", "Inspector")

        tvNavUserName.text = userName?.uppercase() ?: "INSPECTOR"
        tvNavUserRole.text = "INSPECTOR"

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_my_team     -> navigateTo(R.id.nav_team)
                R.id.nav_inspections -> navigateTo(R.id.nav_inspections)
                R.id.nav_history     -> navigateTo(R.id.nav_history)
                R.id.nav_help        -> Toast.makeText(this, "Help", Toast.LENGTH_SHORT).show()
                R.id.nav_logout      -> performLogout()
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun performLogout() {
        val prefs = getSharedPreferences("WearawarerPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            remove("token")
            remove("user_id")
            remove("user_name")
            remove("user_email")
            putBoolean("is_logged_in", false)
            apply()
        }
        stopService(Intent(this, ViolationService::class.java))
        Intent(this, LoginActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }

    private fun setupBottomNav() {
        binding.bottomNavigationView.setOnItemSelectedListener { menuItem ->
            val fragment = when (menuItem.itemId) {
                R.id.nav_home        -> homeFragment
                R.id.nav_alerts      -> alertsFragment
                R.id.nav_inspections -> inspectionsFragment
                R.id.nav_history     -> historyFragment
                R.id.nav_team        -> teamFragment
                else                 -> return@setOnItemSelectedListener false
            }
            showFragment(fragment, menuItem.itemId)
            true
        }
    }
}