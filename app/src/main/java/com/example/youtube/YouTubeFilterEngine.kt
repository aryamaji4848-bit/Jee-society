package com.example.youtube

import com.example.data.model.StudySubject

data class JeeVideo(
    val id: String,
    val title: String,
    val channelName: String,
    val subject: StudySubject,
    val youtubeVideoId: String,
    val durationText: String,
    val tags: List<String>,
    val description: String,
    val isHighYield: Boolean = true
)

data class JeeChannel(
    val name: String,
    val educator: String,
    val subject: StudySubject,
    val subscriberCount: String,
    val badge: String,
    val channelUrl: String
)

object YouTubeFilterEngine {

    // Blacklist of entertainment and non-academic keywords
    val ENTERTAINMENT_KEYWORDS = listOf(
        "song", "songs", "music", "dance", "movie", "movies", "trailer", "teaser",
        "comedy", "standup", "prank", "vlog", "vlogs", "gaming", "gameplay", "roast",
        "bgmi", "free fire", "gta", "minecraft", "anime", "meme", "memes", "reels",
        "shorts", "tiktok", "web series", "episode", "cinema", "cricket", "match highlights",
        "football", "drama", "unboxing", "celebrity", "gossip", "funny", "challenge", "reaction"
    )

    // Academic white-list subjects & terms
    val ACADEMIC_KEYWORDS = listOf(
        "jee", "iit", "physics", "chemistry", "mathematics", "maths", "math",
        "pyq", "pyqs", "ncert", "derivation", "numerical", "rotational", "calculus",
        "integration", "differentiation", "thermodynamics", "organic", "inorganic",
        "physical chemistry", "kinematics", "electrostatics", "optics", "magnetism",
        "matrices", "probability", "vectors", "conic", "revision", "one shot",
        "mock test", "exam analysis", "hc verma", "irodov", "cengage"
    )

    val VERIFIED_JEE_CHANNELS = listOf(
        JeeChannel(
            name = "Eduniti - Physics for JEE",
            educator = "Mohit Goenka Sir",
            subject = StudySubject.PHYSICS,
            subscriberCount = "950K+",
            badge = "Formula & PYQ Master",
            channelUrl = "https://www.youtube.com/@Eduniti"
        ),
        JeeChannel(
            name = "Mohit Tyagi (Competishun)",
            educator = "MT Sir, ABJ Sir, ALK Sir",
            subject = StudySubject.MATHEMATICS,
            subscriberCount = "1.2M+",
            badge = "Deep Theory & Advanced",
            channelUrl = "https://www.youtube.com/@MohitTyagi"
        ),
        JeeChannel(
            name = "MathonGo",
            educator = "Anup Gupta Sir",
            subject = StudySubject.MATHEMATICS,
            subscriberCount = "1.8M+",
            badge = "JEE Math & Marks Analytics",
            channelUrl = "https://www.youtube.com/@mathongo"
        ),
        JeeChannel(
            name = "Physics Galaxy",
            educator = "Ashish Arora Sir",
            subject = StudySubject.PHYSICS,
            subscriberCount = "1.4M+",
            badge = "Advanced Concept Boosters",
            channelUrl = "https://www.youtube.com/@physicsgalaxy74"
        ),
        JeeChannel(
            name = "Pankaj Sir Chemistry",
            educator = "Pankaj Sijairya Sir",
            subject = StudySubject.CHEMISTRY,
            subscriberCount = "2.1M+",
            badge = "Reactions & Mechanism Core",
            channelUrl = "https://www.youtube.com/@PankajsirChemistry"
        ),
        JeeChannel(
            name = "Physics Wallah - Alakh Pandey",
            educator = "Alakh Pandey Sir & Team",
            subject = StudySubject.PHYSICS,
            subscriberCount = "12M+",
            badge = "Free Foundation to Advanced",
            channelUrl = "https://www.youtube.com/@PhysicsWallah"
        ),
        JeeChannel(
            name = "JEE Wallah",
            educator = "Top PW JEE Faculty",
            subject = StudySubject.MOCK_TEST,
            subscriberCount = "2.5M+",
            badge = "Marathons & Test Prep",
            channelUrl = "https://www.youtube.com/@JEEWallah"
        )
    )

    // Curated high-yield JEE lectures ready for in-app distraction-free playback
    val CURATED_STUDY_VIDEOS = listOf(
        JeeVideo(
            id = "phy_1",
            title = "Complete Rotational Motion in One Shot | All Concepts & Formulas",
            channelName = "Eduniti - Physics for JEE",
            subject = StudySubject.PHYSICS,
            youtubeVideoId = "30-5BqjY9_c",
            durationText = "1h 45m",
            tags = listOf("Physics", "Rotational Motion", "Formula Revision", "High Yield"),
            description = "Complete revision of Moment of Inertia, Torque, Angular Momentum, and Rolling without slipping with JEE Main PYQs."
        ),
        JeeVideo(
            id = "phy_2",
            title = "Electrostatics & Gauss Law: Complete Theory & Advanced Numerical",
            channelName = "Physics Galaxy",
            subject = StudySubject.PHYSICS,
            youtubeVideoId = "g8A9sU3j2Nk",
            durationText = "2h 10m",
            tags = listOf("Physics", "Electrostatics", "Field & Potential"),
            description = "Step-by-step masterclass on Coulomb's law, electric dipole, flux calculation, and conductor electrostatic shielding."
        ),
        JeeVideo(
            id = "chem_1",
            title = "Coordination Compounds in One Shot | Crystal Field Theory & Isomerism",
            channelName = "Pankaj Sir Chemistry",
            subject = StudySubject.CHEMISTRY,
            youtubeVideoId = "7K2v9M9L8vY",
            durationText = "2h 30m",
            tags = listOf("Chemistry", "Inorganic", "Coordination", "Guaranteed 8 Marks"),
            description = "IUPAC nomenclature, Werner's theory, VBT hybridization, CFT splitting, and geometrical/optical isomerism."
        ),
        JeeVideo(
            id = "chem_2",
            title = "Chemical Thermodynamics & Thermochemistry: Full Concept Revision",
            channelName = "Competishun (ALK Sir)",
            subject = StudySubject.CHEMISTRY,
            youtubeVideoId = "4p9qT8xY7wZ",
            durationText = "1h 55m",
            tags = listOf("Chemistry", "Physical Chemistry", "Thermodynamics"),
            description = "First & Second laws of thermodynamics, enthalpy changes, entropy, Gibbs free energy criterion for spontaneity."
        ),
        JeeVideo(
            id = "math_1",
            title = "Definite Integration & Area Under Curve: Top 50 JEE Main PYQs",
            channelName = "MathonGo",
            subject = StudySubject.MATHEMATICS,
            youtubeVideoId = "v1L2m8N9xK0",
            durationText = "2h 40m",
            tags = listOf("Maths", "Calculus", "Definite Integration", "PYQ Marathon"),
            description = "King's property, Queen's property, Leibniz theorem for differentiation under integral, and rapid area integration tricks."
        ),
        JeeVideo(
            id = "math_2",
            title = "Matrices & Determinants in One Shot | System of Linear Equations",
            channelName = "Mohit Tyagi (MT Sir)",
            subject = StudySubject.MATHEMATICS,
            youtubeVideoId = "a9X3vK1m9Z0",
            durationText = "1h 35m",
            tags = listOf("Maths", "Algebra", "Cramer's Rule", "Rank of Matrix"),
            description = "Properties of determinants, adjoint & inverse of matrix, Cayley-Hamilton theorem, and consistency of linear equations."
        ),
        JeeVideo(
            id = "mock_1",
            title = "JEE Main Full Syllabus Mock Paper Solving | Time Management Strategy",
            channelName = "JEE Wallah",
            subject = StudySubject.MOCK_TEST,
            youtubeVideoId = "m4P9xK8q7L2",
            durationText = "3h 05m",
            tags = listOf("Mock Test", "Strategy", "PCM Solving", "Speed & Accuracy"),
            description = "Complete 3-hour mock paper simulation with question selection technique, paper-solving order, and negative mark avoidance."
        ),
        JeeVideo(
            id = "rev_1",
            title = "Complete 11th & 12th Physics Formula Revision for JEE Main",
            channelName = "Eduniti - Physics for JEE",
            subject = StudySubject.REVISION,
            youtubeVideoId = "k2L9mX4v7N1",
            durationText = "1h 15m",
            tags = listOf("Revision", "Formulas", "Physics Pocket Book"),
            description = "Quick recall of all 32 chapters' essential formulas, dimensional constants, and short-cut approximations."
        )
    )

    /**
     * Validates if a user's search query is study-oriented.
     * Returns a FilterResult with status and sanitized query.
     */
    data class FilterResult(
        val isAllowed: Boolean,
        val sanitizedQuery: String,
        val blockedReason: String? = null
    )

    fun filterAndSanitizeQuery(query: String): FilterResult {
        val trimmed = query.trim().lowercase()
        if (trimmed.isBlank()) {
            return FilterResult(true, "JEE Main Advanced preparation study lecture")
        }

        // Check against entertainment keywords
        for (keyword in ENTERTAINMENT_KEYWORDS) {
            val words = trimmed.split(" ", "-", "_", ",")
            if (words.contains(keyword) || (trimmed.contains(keyword) && !isAcademicException(trimmed, keyword))) {
                return FilterResult(
                    isAllowed = false,
                    sanitizedQuery = "",
                    blockedReason = "Query contains '$keyword', which is classified as non-educational entertainment. JEE Focus restricts YouTube to academic study content only."
                )
            }
        }

        // Always reinforce academic intent
        val sanitized = if (!trimmed.contains("jee") && !trimmed.contains("iit") && !trimmed.contains("study") && !trimmed.contains("lecture")) {
            "$query JEE Main study lecture"
        } else {
            query
        }

        return FilterResult(isAllowed = true, sanitizedQuery = sanitized)
    }

    private fun isAcademicException(query: String, keyword: String): Boolean {
        // e.g., "wave optics" might contain "wave", etc.
        return false
    }

    fun getEmbedUrl(videoId: String): String {
        // Distraction-free embedded player URL:
        // no related videos from random channels (rel=0), no branding, modetbranding=1
        return "https://www.youtube-nocookie.com/embed/$videoId?autoplay=1&rel=0&modestbranding=1&playsinline=1"
    }
}
