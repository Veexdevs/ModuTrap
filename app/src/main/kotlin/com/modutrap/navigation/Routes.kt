package com.modutrap.navigation

/** Navigation routes. Bottom-bar routes are the app's primary destinations. */
object Routes {
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val GRAPHICS = "graphics"
    const val PERFORMANCE = "performance"
    const val LAUNCH = "launch"
    const val PERSONALIZATION = "personalization"
    const val FLAGS = "flags"
    const val INTEGRATIONS = "integrations"
    const val ABOUT = "about"
}

/** Routes that show the bottom navigation bar. */
val bottomBarRoutes = listOf(
    Routes.HOME,
    Routes.SETTINGS,
    Routes.FLAGS,
    Routes.INTEGRATIONS,
    Routes.ABOUT
)
