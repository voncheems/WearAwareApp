package com.example.wearawarer

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HelpFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_help, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        view.findViewById<LinearLayout>(R.id.btnHowToAddTeam).setOnClickListener {
            showHelp(
                "How to Add Team Members",
                "Go to My Team from the sidebar, then tap the '+' button to invite a member by email."
            )
        }

        view.findViewById<LinearLayout>(R.id.btnHowToViewHistory).setOnClickListener {
            showHelp(
                "Viewing Inspection History",
                "Open History from the sidebar to browse all past PPE inspection logs sorted by date."
            )
        }

        view.findViewById<LinearLayout>(R.id.btnUnderstandingAlerts).setOnClickListener {
            showHelp(
                "Understanding PPE Alerts",
                "Alerts are triggered when a violation is detected. Each alert shows the type of missing PPE, location, and timestamp."
            )
        }

        view.findViewById<LinearLayout>(R.id.btnManageNotifications).setOnClickListener {
            showHelp(
                "Managing Notification Settings",
                "Go to Settings and toggle 'Alert Notifications' to turn push alerts on or off."
            )
        }

        view.findViewById<LinearLayout>(R.id.btnEmailSupport).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:support@yourapp.com")
                putExtra(Intent.EXTRA_SUBJECT, "App Support Request")
            }
            startActivity(Intent.createChooser(intent, "Send Email"))
        }

        try {
            val pInfo = requireContext().packageManager
                .getPackageInfo(requireContext().packageName, 0)
            view.findViewById<TextView>(R.id.tvAppVersion).text = "v${pInfo.versionName}"
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun showHelp(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Got it", null)
            .show()
    }
}