package com.modutrap.models

/** Snapshot of the Roblox installation on the device. */
data class RobloxStatus(
    val installed: Boolean,
    val packageName: String,
    val versionName: String?,
    val dataDir: String?,
    val isDebugBuild: Boolean,
    val hasLaunchIntent: Boolean,
    /** True when the specific package could be queried at all (visibility). */
    val packageVisible: Boolean
)
