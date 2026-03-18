package com.example.wearawarer

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class ViolationService : Service() {

    private lateinit var client: OkHttpClient
    private var webSocket: WebSocket? = null
    private val channelId = "violation_alerts"
    private val foregroundId = 101

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        startForeground(foregroundId, createForegroundNotification())
        setupWebSocket()
    }

    private fun setupWebSocket() {
        client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build()

        val request = Request.Builder()
            .url(WS_URL)
            .build()

        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("ViolationService", "Connected to WebSocket Server")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("ViolationService", "Received: $text")
                handleViolationMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("ViolationService", "Error: ${t.message}")
                try {
                    Thread.sleep(5000)
                    setupWebSocket()
                } catch (e: Exception) {}
            }
        }

        webSocket = client.newWebSocket(request, listener)
    }

    private fun handleViolationMessage(jsonText: String) {
        try {
            val data = JSONObject(jsonText)
            val title = data.optString("title", "Violation Detected")
            val message = data.optString("message", "A safety violation occurred.")

            // 1. Show the notification tray alert
            showLocalNotification(title, message)

            // 2. Broadcast raw JSON to AlertsFragment so it can render the card
            val intent = Intent("com.wearaware.NEW_ALERT").apply {
                putExtra("raw_data", jsonText)
            }
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)

        } catch (e: Exception) {
            Log.e("ViolationService", "JSON Parse Error", e)
        }
    }

    private fun showLocalNotification(title: String, body: String) {
        // Tell MainActivity to navigate to the alerts tab when notification is tapped
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "alerts")
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("WearAware Active")
            .setContentText("Monitoring for safety violations...")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId, "Safety Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        webSocket?.close(1000, "Service Destroyed")
        super.onDestroy()
    }
}