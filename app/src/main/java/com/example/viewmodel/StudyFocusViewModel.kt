package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blocker.BlockerManager
import com.example.data.local.JeeDatabase
import com.example.data.model.DailyGoalEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.StudySubject
import com.example.motivation.JeeMotivationManager
import com.example.youtube.JeeVideo
import com.example.youtube.YouTubeFilterEngine
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class TimerState {
    IDLE,
    RUNNING,
    PAUSED,
    BREAK
}

data class StreakInfo(
    val currentStreak: Int = 1,
    val longestStreak: Int = 3,
    val totalMinutes: Int = 0,
    val todayMinutes: Int = 0
)

class StudyFocusViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = JeeDatabase.getInstance(application).jeeDao()

    val currentEpochDay: Long
        get() = System.currentTimeMillis() / (1000 * 60 * 60 * 24)

    // User Profile Preferences (Stored in SharedPreferences)
    private val prefs = application.getSharedPreferences("jee_focus_prefs", Context.MODE_PRIVATE)

    private val _studentName = MutableStateFlow(prefs.getString("student_name", "Future IITian") ?: "Future IITian")
    val studentName = _studentName.asStateFlow()

    private val _targetCollege = MutableStateFlow(prefs.getString("target_college", "IIT Bombay (CSE)") ?: "IIT Bombay (CSE)")
    val targetCollege = _targetCollege.asStateFlow()

    // Active Study Session State
    private val _selectedSubject = MutableStateFlow(StudySubject.PHYSICS)
    val selectedSubject = _selectedSubject.asStateFlow()

    private val _sessionDurationPreset = MutableStateFlow(50) // in minutes
    val sessionDurationPreset = _sessionDurationPreset.asStateFlow()

    private val _timerState = MutableStateFlow(TimerState.IDLE)
    val timerState = _timerState.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(50 * 60)
    val remainingSeconds = _remainingSeconds.asStateFlow()

    private val _breakSeconds = MutableStateFlow(10 * 60)
    val breakSeconds = _breakSeconds.asStateFlow()

    private val _sessionGoodLuckMessage = MutableStateFlow<String?>(null)
    val sessionGoodLuckMessage = _sessionGoodLuckMessage.asStateFlow()

    private val _breakEncouragementMessage = MutableStateFlow<String?>(null)
    val breakEncouragementMessage = _breakEncouragementMessage.asStateFlow()

    private val _showGoodLuckDialog = MutableStateFlow(false)
    val showGoodLuckDialog = _showGoodLuckDialog.asStateFlow()

    private val _showBreakDialog = MutableStateFlow(false)
    val showBreakDialog = _showBreakDialog.asStateFlow()

    private var timerJob: Job? = null

    // YouTube Study Hub State
    private val _selectedYouTubeSubject = MutableStateFlow<StudySubject?>(null)
    val selectedYouTubeSubject = _selectedYouTubeSubject.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _activePlayingVideo = MutableStateFlow<JeeVideo?>(null)
    val activePlayingVideo = _activePlayingVideo.asStateFlow()

    private val _queryFilterWarning = MutableStateFlow<String?>(null)
    val queryFilterWarning = _queryFilterWarning.asStateFlow()

    // Blocker stats and apps
    val blockedApps = BlockerManager.blockedApps
    val isBlockerActive = BlockerManager.isBlockerActive
    val distractionsBlockedCount = BlockerManager.distractionsBlockedCount
    val lastBlockedAppAlert = BlockerManager.lastBlockedAppAlert

    // Database Observables
    val todaySessions = dao.getSessionsForDay(currentEpochDay)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayGoals = dao.getGoalsForDay(currentEpochDay)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val streakInfo: StateFlow<StreakInfo> = combine(
        dao.getStudyEpochDays(),
        dao.getTotalStudyMinutes(),
        dao.getTodayStudyMinutes(currentEpochDay)
    ) { days, totalMins, todayMins ->
        calculateStreak(days, totalMins ?: 0, todayMins ?: 0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StreakInfo(currentStreak = 1, longestStreak = 1))

    init {
        // Pre-populate initial default JEE daily goals for today if empty
        viewModelScope.launch {
            val today = currentEpochDay
            // Check if goals exist
            dao.getGoalsForDay(today).collect { goals ->
                if (goals.isEmpty()) {
                    dao.insertGoal(
                        DailyGoalEntity(
                            title = "Solve 25 HC Verma Physics Numericals (Mechanics/Optics)",
                            subject = "Physics",
                            targetCount = 25,
                            completedCount = 10,
                            epochDay = today
                        )
                    )
                    dao.insertGoal(
                        DailyGoalEntity(
                            title = "Organic Chemistry NCERT Reaction Mechanisms & Revision",
                            subject = "Chemistry",
                            targetCount = 1,
                            completedCount = 0,
                            epochDay = today
                        )
                    )
                    dao.insertGoal(
                        DailyGoalEntity(
                            title = "Solve 20 Definite Integration & Matrix PYQs",
                            subject = "Mathematics",
                            targetCount = 20,
                            completedCount = 5,
                            epochDay = today
                        )
                    )
                }
            }
        }
    }

    private fun calculateStreak(epochDays: List<Long>, totalMins: Int, todayMins: Int): StreakInfo {
        if (epochDays.isEmpty()) {
            return StreakInfo(currentStreak = if (todayMins > 0) 1 else 0, longestStreak = if (todayMins > 0) 1 else 0, totalMinutes = totalMins, todayMinutes = todayMins)
        }

        val sortedDays = epochDays.sortedDescending()
        val today = currentEpochDay
        var currentStreak = 0
        var expectedDay = if (sortedDays.contains(today)) today else today - 1

        for (day in sortedDays) {
            if (day == expectedDay) {
                currentStreak++
                expectedDay--
            } else if (day < expectedDay) {
                break
            }
        }

        // Always encourage with at least 1-day active streak if studying today
        if (currentStreak == 0 && todayMins > 0) {
            currentStreak = 1
        }

        val longest = maxOf(currentStreak, prefs.getInt("longest_streak", currentStreak))
        prefs.edit().putInt("longest_streak", longest).apply()

        return StreakInfo(
            currentStreak = maxOf(1, currentStreak),
            longestStreak = maxOf(3, longest),
            totalMinutes = totalMins,
            todayMinutes = todayMins
        )
    }

    fun updateProfile(name: String, college: String) {
        _studentName.value = name
        _targetCollege.value = college
        prefs.edit()
            .putString("student_name", name)
            .putString("target_college", college)
            .apply()
    }

    fun setSubject(subject: StudySubject) {
        _selectedSubject.value = subject
    }

    fun setDurationPreset(minutes: Int) {
        if (_timerState.value == TimerState.IDLE) {
            _sessionDurationPreset.value = minutes
            _remainingSeconds.value = minutes * 60
        }
    }

    fun startStudySession() {
        val goodLuck = JeeMotivationManager.getRandomStartGoodLuck(
            subject = _selectedSubject.value,
            studentName = _studentName.value
        )
        _sessionGoodLuckMessage.value = goodLuck
        _showGoodLuckDialog.value = true

        // Ensure blocker is active during study!
        BlockerManager.setBlockerActive(true)

        _timerState.value = TimerState.RUNNING
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value -= 1
            }

            if (_remainingSeconds.value <= 0) {
                // Session completed! Record to database
                recordSessionCompletion()
                startBreakSession()
            }
        }
    }

    fun pauseStudySession() {
        _timerState.value = TimerState.PAUSED
        timerJob?.cancel()
    }

    fun resumeStudySession() {
        _timerState.value = TimerState.RUNNING
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value -= 1
            }
            if (_remainingSeconds.value <= 0) {
                recordSessionCompletion()
                startBreakSession()
            }
        }
    }

    fun stopStudySession() {
        timerJob?.cancel()
        // If studied for more than 5 minutes, record partial session
        val durationMins = _sessionDurationPreset.value - (_remainingSeconds.value / 60)
        if (durationMins >= 5) {
            viewModelScope.launch {
                dao.insertSession(
                    StudySessionEntity(
                        subject = _selectedSubject.value.displayName,
                        durationMinutes = durationMins,
                        completed = false
                    )
                )
            }
        }
        _timerState.value = TimerState.IDLE
        _remainingSeconds.value = _sessionDurationPreset.value * 60
        _showGoodLuckDialog.value = false
        _showBreakDialog.value = false
    }

    private fun startBreakSession() {
        _timerState.value = TimerState.BREAK
        val encouragement = JeeMotivationManager.getRandomBreakEncouragement()
        _breakEncouragementMessage.value = encouragement
        _showBreakDialog.value = true

        val breakDuration = if (_sessionDurationPreset.value >= 180) 20 * 60 else 5 * 60
        _breakSeconds.value = breakDuration

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (isActive && _breakSeconds.value > 0) {
                delay(1000)
                _breakSeconds.value -= 1
            }
            // Break finished, reset to idle
            _timerState.value = TimerState.IDLE
            _remainingSeconds.value = _sessionDurationPreset.value * 60
        }
    }

    fun skipBreak() {
        timerJob?.cancel()
        _timerState.value = TimerState.IDLE
        _remainingSeconds.value = _sessionDurationPreset.value * 60
        _showBreakDialog.value = false
    }

    private fun recordSessionCompletion() {
        viewModelScope.launch {
            dao.insertSession(
                StudySessionEntity(
                    subject = _selectedSubject.value.displayName,
                    durationMinutes = _sessionDurationPreset.value,
                    completed = true
                )
            )
        }
    }

    fun dismissGoodLuckDialog() {
        _showGoodLuckDialog.value = false
    }

    fun dismissBreakDialog() {
        _showBreakDialog.value = false
    }

    fun clearInterventionAlert() {
        BlockerManager.clearAlert()
    }

    // Daily Goals Management
    fun addGoal(title: String, subject: String, targetCount: Int) {
        viewModelScope.launch {
            dao.insertGoal(
                DailyGoalEntity(
                    title = title,
                    subject = subject,
                    targetCount = targetCount,
                    completedCount = 0,
                    epochDay = currentEpochDay
                )
            )
        }
    }

    fun toggleGoalCompletion(goal: DailyGoalEntity) {
        viewModelScope.launch {
            val updated = goal.copy(
                isCompleted = !goal.isCompleted,
                completedCount = if (!goal.isCompleted) goal.targetCount else 0
            )
            dao.updateGoal(updated)
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            dao.deleteGoal(goalId)
        }
    }

    // YouTube Filter & Playback
    fun setYouTubeSubjectFilter(subject: StudySubject?) {
        _selectedYouTubeSubject.value = subject
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        _queryFilterWarning.value = null
    }

    fun executeYouTubeSearch(query: String) {
        val filterResult = YouTubeFilterEngine.filterAndSanitizeQuery(query)
        if (!filterResult.isAllowed) {
            _queryFilterWarning.value = filterResult.blockedReason
        } else {
            _queryFilterWarning.value = null
        }
    }

    fun playVideo(video: JeeVideo) {
        _activePlayingVideo.value = video
    }

    fun closeVideoPlayer() {
        _activePlayingVideo.value = null
    }

    fun toggleAppBlock(pkg: String) {
        BlockerManager.toggleAppBlocked(pkg)
    }

    fun setBlockerActive(active: Boolean) {
        BlockerManager.setBlockerActive(active)
    }
}
