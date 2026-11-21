package com.modutrap.flags

import com.modutrap.models.FastFlagInfo
import com.modutrap.models.FlagCategory
import com.modutrap.models.FlagValueType

/**
 * Curated catalog of Roblox Fast Flags that are part of the current, publicly
 * documented Fast Flag set.
 *
 * Sources: Roblox's official Fast Flag documentation and community-verified
 * lists used by bootstrappers such as Bloxstrap. These are real flags — no
 * fictional entries are added. A flag not present here can still be stored by
 * the user, but is treated as **unverified** (see [FastFlagValidator]) and the
 * UI warns that it may not take effect.
 *
 * Categories are logical groupings for the UI only; they do not affect whether
 * a flag is accepted.
 */
object FastFlagCatalog {

    private val flags: List<FastFlagInfo> = listOf(
        // ---- Graphics ----
        FastFlagInfo("FFlagDisablePostFx", FlagCategory.GRAPHICS, FlagValueType.BOOLEAN, "True",
            "Disables post-processing effects."),
        FastFlagInfo("FFlagDisableShadowRender", FlagCategory.GRAPHICS, FlagValueType.BOOLEAN, "True",
            "Disables shadow rendering entirely."),
        FastFlagInfo("FFlagRenderShadow", FlagCategory.GRAPHICS, FlagValueType.BOOLEAN, "False",
            "Master switch for shadow rendering."),
        FastFlagInfo("FIntRenderShadowIntensity", FlagCategory.GRAPHICS, FlagValueType.INTEGER, "0",
            "Shadow intensity; 0 disables soft shadows."),
        FastFlagInfo("FIntDebugForceMSAASamples", FlagCategory.GRAPHICS, FlagValueType.INTEGER, "1",
            "Forces an MSAA sample count; 1 disables MSAA."),
        FastFlagInfo("FFlagGraphicsQualityAutoLevel", FlagCategory.GRAPHICS, FlagValueType.BOOLEAN, "False",
            "Auto-adjusts the graphics quality level."),
        FastFlagInfo("FIntQualityLevel", FlagCategory.GRAPHICS, FlagValueType.INTEGER, "0",
            "Base graphics quality level."),
        FastFlagInfo("FFlagFixGraphicsQualityChange", FlagCategory.GRAPHICS, FlagValueType.BOOLEAN, "True",
            "Fixes graphics quality change handling."),
        FastFlagInfo("FFlagFixGraphicsQualityReset", FlagCategory.GRAPHICS, FlagValueType.BOOLEAN, "True",
            "Fixes graphics quality reset behaviour."),
        FastFlagInfo("FFlagDebugGraphicsPreferVulkan", FlagCategory.GRAPHICS, FlagValueType.BOOLEAN, "False",
            "Prefers the Vulkan renderer."),
        FastFlagInfo("FFlagDebugGraphicsPreferD3D11", FlagCategory.GRAPHICS, FlagValueType.BOOLEAN, "False",
            "Prefers the D3D11 renderer (desktop)."),
        FastFlagInfo("FFlagDebugGraphicsDisableDirect3D11", FlagCategory.GRAPHICS, FlagValueType.BOOLEAN, "False",
            "Disables the D3D11 renderer."),
        FastFlagInfo("DFFlagTextureQualityOverrideEnabled", FlagCategory.GRAPHICS, FlagValueType.BOOLEAN, "False",
            "Enables the texture-quality override."),
        FastFlagInfo("DFIntTextureQualityOverride", FlagCategory.GRAPHICS, FlagValueType.INTEGER, "0",
            "Texture quality override level."),
        FastFlagInfo("FFlagTerrainSkipTextures", FlagCategory.GRAPHICS, FlagValueType.BOOLEAN, "False",
            "Skips terrain texture loading."),
        FastFlagInfo("FFlagUserDisableCoreScriptTexturePreload", FlagCategory.GRAPHICS, FlagValueType.BOOLEAN, "False",
            "Disables core-script texture preload."),
        FastFlagInfo("FIntTerrainArraySliceSize", FlagCategory.GRAPHICS, FlagValueType.INTEGER, "0",
            "Terrain array slice size."),
        FastFlagInfo("FFlagDontRerenderForBadTexture", FlagCategory.GRAPHICS, FlagValueType.BOOLEAN, "True",
            "Stops re-rendering when a texture is invalid."),
        FastFlagInfo("FFlagRenderDynamicResolutionScale9", FlagCategory.GRAPHICS, FlagValueType.BOOLEAN, "False",
            "Enables dynamic resolution scaling."),
        FastFlagInfo("DFIntMaxFrameBufferSize", FlagCategory.GRAPHICS, FlagValueType.INTEGER, "4",
            "Maximum frame buffer size."),

        // ---- Performance ----
        FastFlagInfo("DFIntTaskSchedulerTargetFps", FlagCategory.PERFORMANCE, FlagValueType.INTEGER, "0",
            "Target frame rate for the task scheduler."),
        FastFlagInfo("FFlagTaskSchedulerLimitTargetFpsTo2402", FlagCategory.PERFORMANCE, FlagValueType.BOOLEAN, "False",
            "Caps the target FPS at 240."),
        FastFlagInfo("DFFlagEsportsFrameRate", FlagCategory.PERFORMANCE, FlagValueType.BOOLEAN, "False",
            "Enables esports frame-rate mode."),
        FastFlagInfo("FFlagDebugDisableScriptingGC", FlagCategory.PERFORMANCE, FlagValueType.BOOLEAN, "False",
            "Disables scripting garbage collection."),
        FastFlagInfo("FFlagLuauCodegen", FlagCategory.PERFORMANCE, FlagValueType.BOOLEAN, "False",
            "Enables Luau code generation."),
        FastFlagInfo("FIntLuaGcParallelMinMultiTasks", FlagCategory.PERFORMANCE, FlagValueType.INTEGER, "4",
            "Minimum parallel tasks for the Lua GC."),
        FastFlagInfo("FFlagSkipFrameForSlowLoadingAssets", FlagCategory.PERFORMANCE, FlagValueType.BOOLEAN, "False",
            "Skips frames while assets are loading."),
        FastFlagInfo("FFlagDoNotUploadRecentlyUsedAssets", FlagCategory.PERFORMANCE, FlagValueType.BOOLEAN, "False",
            "Skips uploading recently used assets."),
        FastFlagInfo("FFlagMovePreRender", FlagCategory.PERFORMANCE, FlagValueType.BOOLEAN, "False",
            "Moves work before the render step."),
        FastFlagInfo("FFlagFasterPreciseTime4", FlagCategory.PERFORMANCE, FlagValueType.BOOLEAN, "False",
            "Uses a faster high-precision timer."),
        FastFlagInfo("FIntRuntimeMaxNumOfThreads", FlagCategory.PERFORMANCE, FlagValueType.INTEGER, "0",
            "Maximum number of runtime threads."),
        FastFlagInfo("DFFlagDebugPerfMode", FlagCategory.PERFORMANCE, FlagValueType.BOOLEAN, "False",
            "Enables performance benchmarking mode."),
        FastFlagInfo("FFlagDebugCheckRenderThreading", FlagCategory.PERFORMANCE, FlagValueType.BOOLEAN, "False",
            "Checks render-thread health."),
        FastFlagInfo("FIntRenderMeshOptimizeVertexBuffer", FlagCategory.PERFORMANCE, FlagValueType.INTEGER, "0",
            "Vertex buffer mesh optimization level."),
        FastFlagInfo("FFlagVisBugChecksThreadYield", FlagCategory.PERFORMANCE, FlagValueType.BOOLEAN, "False",
            "Yields in visualization thread checks."),
        FastFlagInfo("DFFlagUseVisBugChecks", FlagCategory.PERFORMANCE, FlagValueType.BOOLEAN, "False",
            "Enables visualization bug checks."),
        FastFlagInfo("DFFlagTaskSchedulerAvoidSleep", FlagCategory.PERFORMANCE, FlagValueType.BOOLEAN, "False",
            "Keeps the CPU awake for the task scheduler."),

        // ---- Network ----
        FastFlagInfo("FFlagNetworkCdnBypass", FlagCategory.NETWORK, FlagValueType.BOOLEAN, "False",
            "Bypasses the content delivery network."),
        FastFlagInfo("FFlagNetworkBypassUdpThrottling", FlagCategory.NETWORK, FlagValueType.BOOLEAN, "False",
            "Bypasses UDP throttling."),
        FastFlagInfo("FFlagReducePacketLoss", FlagCategory.NETWORK, FlagValueType.BOOLEAN, "False",
            "Reduces packet-loss handling."),
        FastFlagInfo("FFlagEnableFastGameJoin", FlagCategory.NETWORK, FlagValueType.BOOLEAN, "False",
            "Enables fast game join."),
        FastFlagInfo("FFlagEnableQuickGameLaunch", FlagCategory.NETWORK, FlagValueType.BOOLEAN, "False",
            "Enables quick game launch."),
        FastFlagInfo("FLogNetwork", FlagCategory.NETWORK, FlagValueType.INTEGER, "0",
            "Network logging verbosity level."),
        FastFlagInfo("DFIntConnectionMTUSize", FlagCategory.NETWORK, FlagValueType.INTEGER, "0",
            "Connection MTU size in bytes."),

        // ---- Telemetry ----
        FastFlagInfo("FFlagDebugDisableTelemetryPoint", FlagCategory.TELEMETRY, FlagValueType.BOOLEAN, "False",
            "Disables telemetry points."),
        FastFlagInfo("FFlagDebugDisableTelemetryEphemeralCounter", FlagCategory.TELEMETRY, FlagValueType.BOOLEAN, "False",
            "Disables ephemeral telemetry counters."),
        FastFlagInfo("FFlagDebugDisableTelemetryEventIngest", FlagCategory.TELEMETRY, FlagValueType.BOOLEAN, "False",
            "Disables telemetry event ingest."),
        FastFlagInfo("FFlagDebugDisableTelemetryV2Counter", FlagCategory.TELEMETRY, FlagValueType.BOOLEAN, "False",
            "Disables V2 telemetry counters."),
        FastFlagInfo("FFlagDebugDisableTelemetryV2Event", FlagCategory.TELEMETRY, FlagValueType.BOOLEAN, "False",
            "Disables V2 telemetry events."),
        FastFlagInfo("FFlagDebugDisableTelemetryV2Stat", FlagCategory.TELEMETRY, FlagValueType.BOOLEAN, "False",
            "Disables V2 telemetry stats."),
        FastFlagInfo("FFlagDebugDisableTelemetryEphemeralStat", FlagCategory.TELEMETRY, FlagValueType.BOOLEAN, "False",
            "Disables ephemeral telemetry stats."),
        FastFlagInfo("DFStringHttpPointsReporterUrl", FlagCategory.TELEMETRY, FlagValueType.STRING, "",
            "HTTP telemetry points reporter URL."),
        FastFlagInfo("DFStringAltHttpPointsReporterUrl", FlagCategory.TELEMETRY, FlagValueType.STRING, "",
            "Alternate HTTP telemetry reporter URL."),
        FastFlagInfo("FStringCoreScriptBacktraceErrorUploadToken", FlagCategory.TELEMETRY, FlagValueType.STRING, "",
            "Core-script backtrace error upload token."),
        FastFlagInfo("DFFlagEnableLightstepReporting2", FlagCategory.TELEMETRY, FlagValueType.BOOLEAN, "False",
            "Enables Lightstep telemetry reporting."),
        FastFlagInfo("DFStringLightstepHTTPTransportUrlPath", FlagCategory.TELEMETRY, FlagValueType.STRING, "",
            "Lightstep HTTP transport URL path."),

        // ---- Audio ----
        FastFlagInfo("FFlagEnableDirectAudioOcclusion2", FlagCategory.AUDIO, FlagValueType.BOOLEAN, "False",
            "Enables direct audio occlusion."),
        FastFlagInfo("DFIntAudioOcclusionMaxNumQueriesPerFrame", FlagCategory.AUDIO, FlagValueType.INTEGER, "6",
            "Maximum audio occlusion queries per frame."),
        FastFlagInfo("DFFlagAudioToggleVolumetricPanning", FlagCategory.AUDIO, FlagValueType.BOOLEAN, "False",
            "Toggles volumetric audio panning."),

        // ---- UI ----
        FastFlagInfo("FFlagHandleAltEnterFullscreenManually", FlagCategory.UI, FlagValueType.BOOLEAN, "False",
            "Handles Alt+Enter fullscreen manually."),
        FastFlagInfo("FFlagEnableNewLoadingScreen", FlagCategory.UI, FlagValueType.BOOLEAN, "False",
            "Enables the new loading screen."),
        FastFlagInfo("FFlagEnableNewVRSystem", FlagCategory.UI, FlagValueType.BOOLEAN, "False",
            "Enables the new VR system."),
        FastFlagInfo("FFlagAlwaysShowVRToggleV3", FlagCategory.UI, FlagValueType.BOOLEAN, "False",
            "Always shows the VR toggle."),
        FastFlagInfo("FFlagAdServiceEnabled", FlagCategory.UI, FlagValueType.BOOLEAN, "False",
            "Enables the ads service."),
        FastFlagInfo("FFlagDisableFeedbackSoothsayerCheck", FlagCategory.UI, FlagValueType.BOOLEAN, "False",
            "Disables the feedback soothsayer check."),
        FastFlagInfo("FFlagDeveloperToastNotificationsEnabled", FlagCategory.UI, FlagValueType.BOOLEAN, "False",
            "Enables developer toast notifications."),
        FastFlagInfo("FFlagSquadToastNotificationsEnabled", FlagCategory.UI, FlagValueType.BOOLEAN, "False",
            "Enables squad toast notifications."),
        FastFlagInfo("FFlagEnableInGameMenuControls", FlagCategory.UI, FlagValueType.BOOLEAN, "False",
            "Enables in-game menu controls."),
        FastFlagInfo("FFlagGameBasicSettingsFramerateCap5", FlagCategory.UI, FlagValueType.BOOLEAN, "False",
            "Exposes the game framerate cap setting."),
        FastFlagInfo("DFIntFullscreenTitleBarTriggerDelayMillis", FlagCategory.UI, FlagValueType.INTEGER, "0",
            "Fullscreen title-bar trigger delay in milliseconds."),
        FastFlagInfo("FIntRomarkStartWithGraphicQualityLevel", FlagCategory.UI, FlagValueType.INTEGER, "0",
            "Starting graphics quality level for Romark."),
        FastFlagInfo("FFlagHighlightOutlinesOnMobile", FlagCategory.UI, FlagValueType.BOOLEAN, "False",
            "Highlights outlines on mobile."),
        FastFlagInfo("FFlagEnablePreferredTextSizeSettingInMenus2", FlagCategory.UI, FlagValueType.BOOLEAN, "False",
            "Enables the preferred text-size setting in menus."),

        // ---- Other ----
        FastFlagInfo("FIntDebugFRMQualityLevelOverride", FlagCategory.OTHER, FlagValueType.INTEGER, "0",
            "Overrides the FRM quality level."),
        FastFlagInfo("DFIntPerformanceControlTextureQualityBestUtility", FlagCategory.OTHER, FlagValueType.INTEGER, "-1",
            "Best-utility texture-quality performance control."),
        FastFlagInfo("FFlagPreloadAllFonts", FlagCategory.OTHER, FlagValueType.BOOLEAN, "False",
            "Preloads all fonts at startup.")
    )

    private val byKey: Map<String, FastFlagInfo> = flags.associateBy { it.key }

    /** All catalog entries, sorted by key for a stable UI. */
    val all: List<FastFlagInfo> = flags.sortedBy { it.key }

    /** Look up catalog metadata for [key], or null when unknown/unverified. */
    fun info(key: String): FastFlagInfo? = byKey[key]

    /** True when [key] is part of the verified catalog. */
    fun isKnown(key: String): Boolean = byKey.containsKey(key)

    fun byCategory(category: FlagCategory): List<FastFlagInfo> =
        flags.filter { it.category == category }.sortedBy { it.key }
}
