package com.example.blocker

import android.app.AppOpsManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Process
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DistractionAppItem(
    val packageName: String,
    val appName: String,
    val category: String,
    val isBlocked: Boolean,
    val isWhitelisted: Boolean = false, // e.g. WhatsApp
    val iconEmoji: String = "📱"
)

object BlockerManager {
    // List of known social media and entertainment apps
    val DEFAULT_APPS = listOf(
        DistractionAppItem("com.whatsapp", "WhatsApp", "Messaging (Allowed for Study)", isBlocked = false, isWhitelisted = true, iconEmoji = "💬"),
        DistractionAppItem("com.instagram.android", "Instagram", "Social Media / Reels", isBlocked = true, iconEmoji = "📸"),
        DistractionAppItem("com.snapchat.android", "Snapchat", "Social Media", isBlocked = true, iconEmoji = "👻"),
        DistractionAppItem("com.facebook.katana", "Facebook", "Social Media", isBlocked = true, iconEmoji = "👥"),
        DistractionAppItem("com.twitter.android", "X (Twitter)", "Social Media", isBlocked = true, iconEmoji = "🐦"),
        DistractionAppItem("com.reddit.frontpage", "Reddit", "Social Forum", isBlocked = true, iconEmoji = "🤖"),
        DistractionAppItem("com.zhiliaoapp.musically", "TikTok", "Short Videos", isBlocked = true, iconEmoji = "🎵"),
        DistractionAppItem("tv.twitch.android.app", "Twitch", "Live Streaming", isBlocked = true, iconEmoji = "🎮"),
        DistractionAppItem("com.netflix.mediaclient", "Netflix", "Entertainment", isBlocked = true, iconEmoji = "🎬"),
        DistractionAppItem("com.amazon.avod.thirdpartyclient", "Prime Video", "Entertainment", isBlocked = true, iconEmoji = "🍿"),
        DistractionAppItem("com.discord", "Discord", "Social Chat", isBlocked = true, iconEmoji = "🎧")
    )

    private val _blockedApps = MutableStateFlow<List<DistractionAppItem>>(DEFAULT_APPS)
    val blockedApps = _blockedApps.asStateFlow()

    private val _isBlockerActive = MutableStateFlow(true)
    val isBlockerActive = _isBlockerActive.asStateFlow()

    private val _distractionsBlockedCount = MutableStateFlow(0)
    val distractionsBlockedCount = _distractionsBlockedCount.asStateFlow()

    private val _lastBlockedAppAlert = MutableStateFlow<String?>(null)
    val lastBlockedAppAlert = _lastBlockedAppAlert.asStateFlow()

    fun toggleAppBlocked(pkg: String) {
        // WhatsApp cannot be blocked per user requirement ("except whatsapp")
        if (pkg == "com.whatsapp") return

        _blockedApps.value = _blockedApps.value.map { app ->
            if (app.packageName == pkg) {
                app.copy(isBlocked = !app.isBlocked)
            } else app
        }
    }

    fun setBlockerActive(active: Boolean) {
        _isBlockerActive.value = active
    }

    fun recordBlockedAttempt(appName: String) {
        _distractionsBlockedCount.value += 1
        _lastBlockedAppAlert.value = appName
    }

    fun clearAlert() {
        _lastBlockedAppAlert.value = null
    }

    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun checkCurrentForegroundApp(context: Context): String? {
        val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return null
        val endTime = System.currentTimeMillis()
        val startTime = endTime - 1000 * 10 // Last 10 seconds

        val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
        var lastPackage: String? = null
        val event = UsageEvents.Event()
        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastPackage = event.packageName
            }
        }
        return lastPackage
    }
}

class FocusBlockerService : Service() {
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = createNotification("JEE Focus Shield Active", "Monitoring distractions. WhatsApp is allowed for study!")
        startForeground(1001, notification)

        startMonitoring()
        return START_STICKY
    }

    private fun startMonitoring() {
        job?.cancel()
        job = scope.launch {
            while (isActive) {
                delay(3000)
                if (!BlockerManager.isBlockerActive.value) continue

                val fgPackage = BlockerManager.checkCurrentForegroundApp(this@FocusBlockerService)
                if (fgPackage != null && fgPackage != packageName) {
                    val matchingApp = BlockerManager.blockedApps.value.find { it.packageName == fgPackage }
                    if (matchingApp != null && matchingApp.isBlocked && !matchingApp.isWhitelisted) {
                        // Distraction detected!
                        BlockerManager.recordBlockedAttempt(matchingApp.appName)

                        // Launch Intervention Screen in JEE Focus
                        val alertIntent = Intent(this@FocusBlockerService, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            putExtra("EXTRA_BLOCKED_APP", matchingApp.appName)
                        }
                        startActivity(alertIntent)
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "jee_focus_shield_channel",
                "JEE Focus Shield & Study Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifies when JEE distraction blocker is active"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(title: String, text: String) =
        NotificationCompat.Builder(this, "jee_focus_shield_channel")
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE
                )
            )
            .build()

    override fun onDestroy() {
        job?.cancel()
        super.onDestroy()
    }
}
