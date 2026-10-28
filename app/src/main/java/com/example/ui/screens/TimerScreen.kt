package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.StudySubject
import com.example.motivation.JeeMotivationManager
import com.example.ui.components.launchWhatsApp
import com.example.ui.theme.ChemistryColor
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldDark
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.MathsColor
import com.example.ui.theme.NavyDark
import com.example.ui.theme.PhysicsColor
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceLightDark
import com.example.ui.theme.WhatsAppGreen
import com.example.viewmodel.TimerState

@Composable
fun TimerScreen(
    timerState: TimerState,
    remainingSeconds: Int,
    breakSeconds: Int,
    sessionPresetMinutes: Int,
    selectedSubject: StudySubject,
    todayMinutesStudied: Int,
    onSelectSubject: (StudySubject) -> Unit,
    onSelectPreset: (Int) -> Unit,
    onStartStudy: () -> Unit,
    onPauseStudy: () -> Unit,
    onResumeStudy: () -> Unit,
    onStopStudy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val currentSeconds = if (timerState == TimerState.BREAK) breakSeconds else remainingSeconds
    val totalTargetSeconds = if (timerState == TimerState.BREAK) (5 * 60) else (sessionPresetMinutes * 60)
    val progress = (currentSeconds.toFloat() / totalTargetSeconds.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    val minutes = currentSeconds / 60
    val seconds = currentSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    val subjectColor = when (selectedSubject) {
        StudySubject.PHYSICS -> PhysicsColor
        StudySubject.CHEMISTRY -> ChemistryColor
        StudySubject.MATHEMATICS -> MathsColor
        StudySubject.MOCK_TEST -> GoldAccent
        StudySubject.REVISION -> CyanAccent
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Subject Selector
        Text(
            text = "SELECT JEE STUDY SUBJECT",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(StudySubject.values()) { subject ->
                val isSelected = subject == selectedSubject
                val color = when (subject) {
                    StudySubject.PHYSICS -> PhysicsColor
                    StudySubject.CHEMISTRY -> ChemistryColor
                    StudySubject.MATHEMATICS -> MathsColor
                    StudySubject.MOCK_TEST -> GoldAccent
                    StudySubject.REVISION -> CyanAccent
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) color.copy(alpha = 0.2f) else SurfaceLightDark)
                        .border(
                            width = if (isSelected) 1.5.dp else 0.dp,
                            color = if (isSelected) color else Color.Transparent,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable(enabled = timerState == TimerState.IDLE) { onSelectSubject(subject) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("subject_chip_${subject.code.lowercase()}")
                ) {
                    Text(
                        text = subject.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) color else Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preset Duration Selector (Only active when Idle)
        if (timerState == TimerState.IDLE) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(25, 50, 90, 180).forEach { mins ->
                    val isSelected = sessionPresetMinutes == mins
                    val label = when (mins) {
                        25 -> "25m Pomo"
                        50 -> "50m Sprint"
                        90 -> "90m Deep"
                        else -> "180m Mock"
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelectPreset(mins) }
                            .testTag("preset_${mins}m"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) GoldAccent.copy(alpha = 0.2f) else SurfaceLightDark
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, GoldAccent) else null
                    ) {
                        Text(
                            text = label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) GoldAccent else Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Circular Timer Visualizer
        Box(
            modifier = Modifier
                .size(260.dp)
                .testTag("circular_timer_container"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(240.dp)) {
                // Background Track
                drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                )

                // Active Progress Arc
                val arcBrush = Brush.sweepGradient(
                    listOf(subjectColor, GoldAccent, subjectColor)
                )
                drawArc(
                    brush = arcBrush,
                    startAngle = -90f,
                    sweepAngle = progress * 360f,
                    useCenter = false,
                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Central Content
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when (timerState) {
                        TimerState.IDLE -> "READY TO FOCUS"
                        TimerState.RUNNING -> "STUDYING ${selectedSubject.displayName.uppercase()}"
                        TimerState.PAUSED -> "SESSION PAUSED"
                        TimerState.BREAK -> "REJUVENATING BREAK"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (timerState == TimerState.BREAK) CyanAccent else subjectColor,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (timerState == TimerState.BREAK) "Rest your eyes & hydrate" else "Blocker Active 🛡️",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            when (timerState) {
                TimerState.IDLE -> {
                    Button(
                        onClick = onStartStudy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("start_session_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldAccent,
                            contentColor = NavyDark
                        )
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Start")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START STUDY SESSION 🚀",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
                TimerState.RUNNING -> {
                    OutlinedButton(
                        onClick = onPauseStudy,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("pause_session_btn"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause", tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Pause", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onStopStudy,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("stop_session_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Finish", fontWeight = FontWeight.Bold)
                    }
                }
                TimerState.PAUSED -> {
                    Button(
                        onClick = onResumeStudy,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("resume_session_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = NavyDark)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Resume")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Resume", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onStopStudy,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("cancel_session_btn"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("End Early", color = Color(0xFFEF4444))
                    }
                }
                TimerState.BREAK -> {
                    Button(
                        onClick = onStopStudy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("skip_break_timer_btn"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = NavyDark)
                    ) {
                        Text("Skip Break & Begin Next Study Session 🎯", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // WhatsApp Quick Study Doubts Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { launchWhatsApp(context) }
                .testTag("whatsapp_study_quick_btn"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(WhatsAppGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp Allowed",
                        tint = WhatsAppGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WhatsApp (Study & Doubts Allowed)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Permitted for teacher discussions and JEE study group queries.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
                Text(
                    text = "OPEN",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = WhatsAppGreen
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Daily JEE Wisdom Quote Card
        val quote = JeeMotivationManager.JEE_QUOTES.first()
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceLightDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FormatQuote,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "JEE ASPIRANT CREED",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldAccent
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "\"${quote.quote}\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFE2E8F0),
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "— ${quote.authorOrExam}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF94A3B8),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
