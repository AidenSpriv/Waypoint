package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

data class UserProfileData(
    val name: String = "Ahmed Al-Mansoori",
    val email: String = "cfnevertheless@gmail.com",
    val referralSource: String = "Doha Smart Mobility Initiative",
    val primaryWalkingGoal: String = "Maximum Shade & Heat Avoidance",
    val preferredPace: String = "Normal (4 km/h)",
    val preferShade: Boolean = true,
    val stepFreeRequired: Boolean = true,
    val autoAvoidObstacles: Boolean = true,
    val isOnboardingCompleted: Boolean = false
)

class UserProfilePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("waypoint_user_profile_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_NAME = "key_user_name"
        private const val KEY_EMAIL = "key_user_email"
        private const val KEY_REFERRAL = "key_referral_source"
        private const val KEY_GOAL = "key_primary_goal"
        private const val KEY_PACE = "key_preferred_pace"
        private const val KEY_PREFER_SHADE = "key_prefer_shade"
        private const val KEY_STEP_FREE = "key_step_free"
        private const val KEY_AVOID_OBSTACLES = "key_avoid_obstacles"
        private const val KEY_ONBOARDING_DONE = "key_onboarding_done"
    }

    fun getUserProfile(): UserProfileData {
        val isDone = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        val defaultName = "Ahmed Al-Mansoori"
        val defaultEmail = "cfnevertheless@gmail.com"
        val defaultReferral = "Doha Smart Mobility Initiative"
        val defaultGoal = "Maximum Shade & Heat Avoidance"
        return UserProfileData(
            name = prefs.getString(KEY_NAME, defaultName) ?: defaultName,
            email = prefs.getString(KEY_EMAIL, defaultEmail) ?: defaultEmail,
            referralSource = prefs.getString(KEY_REFERRAL, defaultReferral) ?: defaultReferral,
            primaryWalkingGoal = prefs.getString(KEY_GOAL, defaultGoal) ?: defaultGoal,
            preferredPace = prefs.getString(KEY_PACE, "Normal (4 km/h)") ?: "Normal (4 km/h)",
            preferShade = prefs.getBoolean(KEY_PREFER_SHADE, true),
            stepFreeRequired = prefs.getBoolean(KEY_STEP_FREE, true),
            autoAvoidObstacles = prefs.getBoolean(KEY_AVOID_OBSTACLES, true),
            isOnboardingCompleted = isDone
        )
    }

    fun saveUserProfile(profile: UserProfileData) {
        prefs.edit()
            .putString(KEY_NAME, profile.name)
            .putString(KEY_EMAIL, profile.email)
            .putString(KEY_REFERRAL, profile.referralSource)
            .putString(KEY_GOAL, profile.primaryWalkingGoal)
            .putString(KEY_PACE, profile.preferredPace)
            .putBoolean(KEY_PREFER_SHADE, profile.preferShade)
            .putBoolean(KEY_STEP_FREE, profile.stepFreeRequired)
            .putBoolean(KEY_AVOID_OBSTACLES, profile.autoAvoidObstacles)
            .putBoolean(KEY_ONBOARDING_DONE, profile.isOnboardingCompleted)
            .apply()
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, completed).apply()
    }

    fun resetOnboarding() {
        prefs.edit().putBoolean(KEY_ONBOARDING_DONE, false).apply()
    }
}
