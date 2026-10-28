package com.example.motivation

import com.example.data.model.StudySubject
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.random.Random

data class JeeMotivationQuote(
    val quote: String,
    val authorOrExam: String,
    val category: String = "JEE Mindset"
)

object JeeMotivationManager {

    val START_SESSION_GOOD_LUCK_MESSAGES = listOf(
        "Good luck for JEE! 🎯 Every numerical problem you solve today brings you one step closer to IIT Bombay / Delhi!",
        "Good luck for JEE! 🏆 Physics concepts aren't conquered by luck, but by relentless focus. You got this!",
        "Good luck for JEE! ⚛️ Remember: When you sit in the exam hall, today's dedication will be your superpower!",
        "Good luck for JEE! 🧪 Chemistry NCERT and practice sheets are your guaranteed high-scoring rank booster. Stay sharp!",
        "Good luck for JEE! 📐 Mathematics requires muscle memory and calmness. Dive deep into problem solving!",
        "Good luck for JEE! 🚀 The dream of walking through the gates of IIT starts right now in this study block!",
        "Good luck for JEE! 🔥 Consistency beats talent when talent doesn't work hard. Put your phone away and dominate!"
    )

    val BREAK_ENCOURAGEMENT_MESSAGES = listOf(
        "Awesome focus block! 💧 Take a deep breath, drink a glass of water, and let your brain synthesize the concepts.",
        "Champion work! 🌿 Look at an object 20 feet away for 20 seconds to relax your eyes (20-20-20 Rule).",
        "Great session! 🧠 Research shows short 5-minute pauses consolidate memory recall by 40%. Recharge now!",
        "One step closer to AIR 1! 🏃 Stand up, stretch your back and shoulders. You are building top-rank stamina!",
        "Well done! 🌟 Even the top JEE rankers take disciplined breaks. Avoid mindless social feeds; let your mind stay calm.",
        "Fantastic progress! ☕ Grab a healthy snack or water. The next study sprint will be even more productive!"
    )

    val JEE_QUOTES = listOf(
        JeeMotivationQuote(
            quote = "Success in JEE is the sum of small problem-solving efforts repeated day in and day out.",
            authorOrExam = "AIR Topper Philosophy"
        ),
        JeeMotivationQuote(
            quote = "Your competition is studying right now. Give yourself every reason to be proud on results day.",
            authorOrExam = "IIT Aspirant Creed"
        ),
        JeeMotivationQuote(
            quote = "Physics is not about memorizing equations; it's about seeing how the universe behaves.",
            authorOrExam = "HC Verma Insight"
        ),
        JeeMotivationQuote(
            quote = "In Inorganic Chemistry, revision is king. In Physical, calculation speed is queen.",
            authorOrExam = "JEE Chemistry Strategy"
        ),
        JeeMotivationQuote(
            quote = "Tough problems don't mean you can't do it. They mean your brain is growing stronger.",
            authorOrExam = "JEE Advanced Mindset"
        )
    )

    fun getRandomStartGoodLuck(subject: StudySubject? = null, studentName: String = ""): String {
        val prefix = if (studentName.isNotBlank()) "Hey $studentName! " else ""
        val baseMessage = when (subject) {
            StudySubject.PHYSICS -> "Good luck for JEE Physics! ⚛️ Visualize the vectors, draw the free body diagrams, and break down each numerical with confidence!"
            StudySubject.CHEMISTRY -> "Good luck for JEE Chemistry! 🧪 Focus on reaction mechanisms and formula precision. High accuracy is yours!"
            StudySubject.MATHEMATICS -> "Good luck for JEE Mathematics! 📐 Keep your scratch pad ready. Tackle every calculus, algebra, and coordinate problem step-by-step!"
            StudySubject.MOCK_TEST -> "Good luck for your JEE Full Mock Test! 🎯 Treat every question like the real exam. Speed, accuracy, and negative marking discipline!"
            else -> START_SESSION_GOOD_LUCK_MESSAGES[Random.nextInt(START_SESSION_GOOD_LUCK_MESSAGES.size)]
        }
        return "$prefix$baseMessage"
    }

    fun getRandomBreakEncouragement(): String {
        return BREAK_ENCOURAGEMENT_MESSAGES[Random.nextInt(BREAK_ENCOURAGEMENT_MESSAGES.size)]
    }

    fun getCountdownDaysToJeeMain(): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance()
        // JEE Main Session 1 typically late January
        target.set(Calendar.MONTH, Calendar.JANUARY)
        target.set(Calendar.DAY_OF_MONTH, 24)
        if (target.before(now)) {
            // Next year's session
            target.add(Calendar.YEAR, 1)
        }
        val diff = target.timeInMillis - now.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0)
    }

    fun getCountdownDaysToJeeAdvanced(): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance()
        // JEE Advanced typically late May
        target.set(Calendar.MONTH, Calendar.MAY)
        target.set(Calendar.DAY_OF_MONTH, 25)
        if (target.before(now)) {
            target.add(Calendar.YEAR, 1)
        }
        val diff = target.timeInMillis - now.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(diff).coerceAtLeast(0)
    }
}
