package com.modutrap.core

import android.content.Context
import com.modutrap.integrations.ShizukuIntegration
import com.modutrap.personalization.BackupManager
import com.modutrap.repositories.FFlagRepository
import com.modutrap.repositories.PersonalizationRepository
import com.modutrap.repositories.PresetRepository
import com.modutrap.roblox.RobloxManager
import com.modutrap.services.DiagnosticsService
import com.modutrap.services.FastFlagApplier
import com.modutrap.services.RobloxLauncher
import com.modutrap.shizuku.ShizukuManager
import com.modutrap.data.SettingsDataStore
import com.modutrap.settings.SettingsRepository

/**
 * Manual dependency container. Instantiated once in [com.modutrap.BloxstrapMobileApp]
 * and handed to every ViewModel, which keeps the app free of global singletons
 * while remaining simple and explicit.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    val paths: AppPaths = AppPaths.also { it.init(appContext) }

    val settings: SettingsRepository =
        SettingsRepository(SettingsDataStore.create(appContext))

    val flags: FFlagRepository = FFlagRepository()

    val presets: PresetRepository = PresetRepository()

    val personalization: PersonalizationRepository = PersonalizationRepository(presets)

    val backups: BackupManager = BackupManager(settings, flags)

    val roblox: RobloxManager = RobloxManager(appContext)

    val shizuku: ShizukuManager = ShizukuManager.also { it.initContext(appContext) }

    val integration: ShizukuIntegration = ShizukuIntegration(shizuku, appContext)

    val flagApplier: FastFlagApplier = FastFlagApplier(flags, shizuku)

    val launcher: RobloxLauncher = RobloxLauncher(appContext, settings, flags, roblox, flagApplier)

    val diagnostics: DiagnosticsService =
        DiagnosticsService(appContext, roblox, shizuku, settings, flags)
}
