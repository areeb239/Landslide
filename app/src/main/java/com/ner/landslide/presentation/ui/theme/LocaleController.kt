package com.ner.landslide.presentation.ui.theme

import android.app.LocaleManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import android.content.ContextWrapper
import android.content.res.AssetManager
import android.content.res.Resources
import java.util.Locale

/**
 * Data model for North-Eastern Region (NER) and Indian supported languages in Bhurakshak.
 */
data class AppLanguage(
    val code: String,
    val englishName: String,
    val nativeName: String,
    val state: String,
    val badgeCode: String
)

object SupportedLanguages {
    val ALL = listOf(
        AppLanguage("en", "English", "English", "All States", "EN"),
        AppLanguage("hi", "Hindi", "हिन्दी", "All States", "HI"),
        AppLanguage("as", "Assamese", "অসমীয়া", "Assam", "AS"),
        AppLanguage("bn", "Bengali", "বাংলা", "Tripura / Assam", "BN"),
        AppLanguage("ne", "Nepali", "नेपाली", "Sikkim", "NE")
    )

    val STATES = listOf(
        "All States",
        "Assam",
        "Tripura",
        "Sikkim"
    )

    fun findByCode(code: String): AppLanguage {
        return ALL.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: ALL.first()
    }
}

/**
 * Controller to handle in-app runtime dynamic language switching with SharedPreferences persistence.
 */
class LocaleController(
    private val context: Context,
    private val prefs: SharedPreferences
) {
    private val _currentLanguage: MutableState<AppLanguage>

    init {
        val savedCode = prefs.getString("selected_language_code", "en") ?: "en"
        _currentLanguage = mutableStateOf(SupportedLanguages.findByCode(savedCode))
    }

    val currentLanguage: State<AppLanguage> = _currentLanguage

    fun setLanguage(language: AppLanguage) {
        if (_currentLanguage.value.code == language.code) return
        _currentLanguage.value = language
        prefs.edit().putString("selected_language_code", language.code).apply()

        // Update Android runtime configuration
        val locale = if (language.code.contains("-")) {
            val parts = language.code.split("-")
            Locale(parts[0], parts[1])
        } else {
            Locale(language.code)
        }
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        // Android 13+ (API 33) Per-App Language Preferences
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            try {
                val localeManager = context.getSystemService(LocaleManager::class.java)
                localeManager?.applicationLocales = LocaleList.forLanguageTags(language.code)
            } catch (_: Exception) {
                // Ignore fallback
            }
        }
    }
}

val LocalLocaleController = staticCompositionLocalOf<LocaleController> {
    error("LocalLocaleController not provided")
}

/**
 * Context wrapper that delegates resource queries to localized configuration
 * while preserving the base activity context hierarchy (for lifecycle, activity results, etc.).
 */
class LocalizedContextWrapper(
    private val baseContext: Context,
    private val configContext: Context
) : ContextWrapper(baseContext) {
    override fun getResources(): Resources = configContext.resources
    override fun getAssets(): AssetManager = configContext.assets
}

/**
 * Context wrapper composable that injects the active locale into Jetpack Compose.
 */
@Composable
fun ProvideAppLocale(
    controller: LocaleController,
    content: @Composable () -> Unit
) {
    val activeLang = controller.currentLanguage.value
    val context = LocalContext.current
    val currentConfig = LocalConfiguration.current

    val localizedLocale = remember(activeLang.code) {
        Locale(activeLang.code)
    }

    val localizedConfig = remember(activeLang.code, currentConfig) {
        Configuration(currentConfig).apply {
            setLocale(localizedLocale)
        }
    }

    val localizedContext = remember(activeLang.code, context) {
        val baseConfig = Configuration(context.resources.configuration).apply {
            setLocale(localizedLocale)
        }
        val configContext = context.createConfigurationContext(baseConfig)
        LocalizedContextWrapper(context, configContext)
    }

    val currentStrings = remember(activeLang.code) {
        AppStringsRepository.get(activeLang.code)
    }

    CompositionLocalProvider(
        LocalLocaleController provides controller,
        LocalContext provides localizedContext,
        LocalConfiguration provides localizedConfig,
        LocalAppStrings provides currentStrings
    ) {
        content()
    }
}


