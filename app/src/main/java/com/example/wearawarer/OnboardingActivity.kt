package com.example.wearawarer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var layoutIndicators: LinearLayout
    private lateinit var btnNext: MaterialCardView
    private lateinit var tvBtnText: TextView
    private lateinit var tvSkip: TextView

    private val layouts = intArrayOf(
        R.layout.onboarding_slide_1,
        R.layout.onboarding_slide_2,
        R.layout.onboarding_slide_3
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if onboarding was already completed
        val prefs = getSharedPreferences("WearawarerPrefs", MODE_PRIVATE)
        if (prefs.getBoolean("has_completed_onboarding", false)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_onboarding)

        initViews()
        setupViewPager()
        setupIndicators()
        setupListeners()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPagerOnboarding)
        layoutIndicators = findViewById(R.id.layoutIndicators)
        btnNext = findViewById(R.id.btnNext)
        tvBtnText = findViewById(R.id.tvBtnText)
        tvSkip = findViewById(R.id.tvSkip)
    }

    private fun setupViewPager() {
        val adapter = OnboardingViewPagerAdapter(layouts)
        viewPager.adapter = adapter

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateIndicators(position)

                // Update button text on last page
                if (position == layouts.size - 1) {
                    tvBtnText.text = "Get Started"
                    tvSkip.visibility = View.GONE
                } else {
                    tvBtnText.text = "Next"
                    tvSkip.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(layouts.size)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)

        for (i in indicators.indices) {
            indicators[i] = ImageView(this)
            indicators[i]?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.indicator_inactive)
            )
            indicators[i]?.layoutParams = layoutParams
            layoutIndicators.addView(indicators[i])
        }

        // Set first indicator as active
        if (indicators.isNotEmpty()) {
            indicators[0]?.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.indicator_active)
            )
        }
    }

    private fun updateIndicators(position: Int) {
        val childCount = layoutIndicators.childCount
        for (i in 0 until childCount) {
            val view = layoutIndicators.getChildAt(i)
            if (view is ImageView) {
                if (i == position) {
                    view.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.indicator_active)
                    )
                } else {
                    view.setImageDrawable(
                        ContextCompat.getDrawable(this, R.drawable.indicator_inactive)
                    )
                }
            }
        }
    }

    private fun setupListeners() {
        btnNext.setOnClickListener {
            val current = viewPager.currentItem + 1
            if (current < layouts.size) {
                // Move to next page
                viewPager.currentItem = current
            } else {
                // Last page - finish onboarding
                finishOnboarding()
            }
        }

        tvSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        val prefs = getSharedPreferences("WearawarerPrefs", MODE_PRIVATE)
        prefs.edit {
            putBoolean("has_completed_onboarding", true)
        }

        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
