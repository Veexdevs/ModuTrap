package com.modutrap.models

/** Snapshot of the Shizuku integration state. */
data class ShizukuStatus(
    val installed: Boolean,
    val binderAlive: Boolean,
    val permissionGranted: Boolean,
    val shouldShowRationale: Boolean,
    val uid: Int?,
    val version: String?
) {
    val ready: Boolean get() = installed && binderAlive && permissionGranted

    companion object {
        val NOT_INSTALLED = ShizukuStatus(
            installed = false,
            binderAlive = false,
            permissionGranted = false,
            shouldShowRationale = false,
            uid = null,
            version = null
        )
    }
}
