package com.modutrap.models

/** Result of a Roblox launch attempt. */
sealed class LaunchResult {
    /**
     * Roblox was launched. [notices] carries non-fatal warnings (e.g. Fast
     * Flags could not be injected because Shizuku was unavailable) so the UI can
     * inform the user without treating the launch as failed.
     */
    data class Success(val notices: List<String> = emptyList()) : LaunchResult()

    data class Failure(val message: String, val cause: Throwable? = null) : LaunchResult()

    /** Roblox is not installed on the device. */
    data object NotInstalled : LaunchResult()

    /**
     * Roblox is installed but the chosen settings could not be applied
     * (e.g. Fast Flags could not be injected without Shizuku/root). The launch
     * itself is not attempted in this case.
     */
    data class Blocked(val message: String) : LaunchResult()
}
