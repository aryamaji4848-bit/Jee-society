package com.example

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.blocker.BlockerManager
import com.example.blocker.FocusBlockerService
import com.example.ui.components.DistractionInterventionDialog
import com.example.ui.components.GoodLuckSessionStartDialog
import com.example.ui.components.BreakEncouragementDialog
import com.example.ui.components.JeeHeader
import com.example.ui.screens.BlockerScreen
import com.example.ui.screens.ProfileDialog
import com.example.ui.screens.StreaksScreen
import com.example.ui.screens.TimerScreen
import com.example.ui.screens.YouTubeScreen
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NavyDark
import com.example.ui.theme.SurfaceDark
import com.example.viewmodel.StudyFocusViewModel

enum class JeeAppTab(val title: String) {
    TIMER("Focus Timer"),
    BLOCKER("Distractions"),
    YOUTUBE("Study YT"),
    STREAKS("Streaks & Goals")
}

class MainActivity : ComponentActivity() {

    private val viewModel: StudyFocusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle possible launch from Blocker Service intervention
        handleIntent(intent)

        setContent {
            MyApplicationTheme {
                JeeApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        val blockedApp = intent?.getStringExtra("EXTRA_BLOCKED_APP")
        if (blockedApp != null) {
            BlockerManager.recordBlockedAttempt(blockedApp)
        }
    }
}

@Composable
fun JeeApp(viewModel: StudyFocusViewModel) {
    var currentTab by remember { mutableStateOf(JeeAppTab.TIMER) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var simulatedInterventionApp by remember { mutableStateOf<String?>(null) }

    val studentName by viewModel.studentName.collectAsStateWithLifecycle()
    val targetCollege by viewModel.targetCollege.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()
    val breakSeconds by viewModel.breakSeconds.collectAsStateWithLifecycle()
    val sessionPreset by viewModel.sessionDurationPreset.collectAsStateWithLifecycle()
    val selectedSubject by viewModel.selectedSubject.collectAsStateWithLifecycle()
    val showGoodLuckDialog by viewModel.showGoodLuckDialog.collectAsStateWithLifecycle()
    val showBreakDialog by viewModel.showBreakDialog.collectAsStateWithLifecycle()
    val goodLuckMessage by viewModel.sessionGoodLuckMessage.collectAsStateWithLifecycle()
    val breakMessage by viewModel.breakEncouragementMessage.collectAsStateWithLifecycle()

    val streakInfo by viewModel.streakInfo.collectAsStateWithLifecycle()
    val blockedApps by viewModel.blockedApps.collectAsStateWithLifecycle()
    val isBlockerActive by viewModel.isBlockerActive.collectAsStateWithLifecycle()
    val distractionsBlockedCount by viewModel.distractionsBlockedCount.collectAsStateWithLifecycle()
    val lastBlockedAppAlert by viewModel.lastBlockedAppAlert.collectAsStateWithLifecycle()

    val selectedYtSubject by viewModel.selectedYouTubeSubject.collectAsStateWithLifecycle()
    val ytSearchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activePlayingVideo by viewModel.activePlayingVideo.collectAsStateWithLifecycle()
    val ytFilterWarning by viewModel.queryFilterWarning.collectAsStateWithLifecycle()

    val goals by viewModel.todayGoals.collectAsStateWithLifecycle()
    val todaySessions by viewModel.todaySessions.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        containerColor = NavyDark,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                JeeAppTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            when (tab) {
                                JeeAppTab.TIMER -> Icon(
                                    imageVector = if (isSelected) Icons.Filled.Timer else Icons.Outlined.Timer,
                                    contentDescription = "Focus Timer"
                                )
                                JeeAppTab.BLOCKER -> Icon(
                                    imageVector = if (isSelected) Icons.Filled.Block else Icons.Outlined.Block,
                                    contentDescription = "Distraction Blocker"
                                )
                                JeeAppTab.YOUTUBE -> Icon(
                                    imageVector = if (isSelected) Icons.Filled.PlayCircle else Icons.Outlined.PlayCircle,
                                    contentDescription = "Study YouTube"
                                )
                                JeeAppTab.STREAKS -> Icon(
                                    imageVector = if (isSelected) Icons.Filled.LocalFireDepartment else Icons.Outlined.LocalFireDepartment,
                                    contentDescription = "Streaks and Goals"
                                )
                            }
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NavyDark,
                            selectedTextColor = GoldAccent,
                            indicatorColor = GoldAccent,
                            unselectedIconColor = Color(0xFF94A3B8),
                            unselectedTextColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header with Aspirant Info, JEE Countdown, and Streak Badge
            JeeHeader(
                studentName = studentName,
                targetCollege = targetCollege,
                currentStreak = streakInfo.currentStreak,
                isShieldActive = isBlockerActive,
                onEditProfileClick = { showProfileDialog = true },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Current Screen Content
            Box(modifier = Modifier.weight(1f)) {
                when (currentTab) {
                    JeeAppTab.TIMER -> {
                        TimerScreen(
                            timerState = timerState,
                            remainingSeconds = remainingSeconds,
                            breakSeconds = breakSeconds,
                            sessionPresetMinutes = sessionPreset,
                            selectedSubject = selectedSubject,
                            todayMinutesStudied = streakInfo.todayMinutes,
                            onSelectSubject = viewModel::setSubject,
                            onSelectPreset = viewModel::setDurationPreset,
                            onStartStudy = viewModel::startStudySession,
                            onPauseStudy = viewModel::pauseStudySession,
                            onResumeStudy = viewModel::resumeStudySession,
                            onStopStudy = viewModel::stopStudySession
                        )
                    }
                    JeeAppTab.BLOCKER -> {
                        BlockerScreen(
                            blockedApps = blockedApps,
                            isBlockerActive = isBlockerActive,
                            distractionsBlockedCount = distractionsBlockedCount,
                            onToggleBlockerActive = viewModel::setBlockerActive,
                            onToggleAppBlock = viewModel::toggleAppBlock,
                            onTriggerTestIntervention = {
                                simulatedInterventionApp = "Instagram"
                            }
                        )
                    }
                    JeeAppTab.YOUTUBE -> {
                        YouTubeScreen(
                            selectedSubject = selectedYtSubject,
                            searchQuery = ytSearchQuery,
                            activePlayingVideo = activePlayingVideo,
                            filterWarning = ytFilterWarning,
                            onSelectSubject = viewModel::setYouTubeSubjectFilter,
                            onSearchQueryChange = viewModel::onSearchQueryChange,
                            onSearchSubmit = viewModel::executeYouTubeSearch,
                            onPlayVideo = viewModel::playVideo,
                            onCloseVideoPlayer = viewModel::closeVideoPlayer
                        )
                    }
                    JeeAppTab.STREAKS -> {
                        StreaksScreen(
                            streakInfo = streakInfo,
                            goals = goals,
                            sessions = todaySessions,
                            onToggleGoal = viewModel::toggleGoalCompletion,
                            onDeleteGoal = viewModel::deleteGoal,
                            onAddGoal = viewModel::addGoal
                        )
                    }
                }
            }
        }
    }

    // "Good Luck for JEE!" Session Start Dialog
    if (showGoodLuckDialog && goodLuckMessage != null) {
        GoodLuckSessionStartDialog(
            message = goodLuckMessage!!,
            subject = selectedSubject,
            studentName = studentName,
            targetCollege = targetCollege,
            onDismiss = viewModel::dismissGoodLuckDialog
        )
    }

    // Break Encouragement Dialog
    if (showBreakDialog && breakMessage != null) {
        BreakEncouragementDialog(
            message = breakMessage!!,
            breakRemainingSeconds = breakSeconds,
            onSkipBreak = viewModel::skipBreak,
            onDismiss = viewModel::dismissBreakDialog
        )
    }

    // Distraction Intervention Dialog (Real detection alert OR simulated test)
    val alertApp = lastBlockedAppAlert ?: simulatedInterventionApp
    if (alertApp != null) {
        DistractionInterventionDialog(
            blockedAppName = alertApp,
            targetCollege = targetCollege,
            onReturnToStudy = {
                simulatedInterventionApp = null
                viewModel.clearInterventionAlert()
                currentTab = JeeAppTab.TIMER
            },
            onOpenYouTube = {
                simulatedInterventionApp = null
                viewModel.clearInterventionAlert()
                currentTab = JeeAppTab.YOUTUBE
            },
            onDismiss = {
                simulatedInterventionApp = null
                viewModel.clearInterventionAlert()
            }
        )
    }

    // Profile & Goal Customization Dialog
    if (showProfileDialog) {
        ProfileDialog(
            initialName = studentName,
            initialCollege = targetCollege,
            onDismiss = { showProfileDialog = false },
            onSave = viewModel::updateProfile
        )
    }
}
