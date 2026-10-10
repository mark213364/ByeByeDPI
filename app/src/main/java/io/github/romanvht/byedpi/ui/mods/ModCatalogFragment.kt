package io.github.romanvht.byedpi.ui.mods

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat

class ModCatalogFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = requireContext()
        val screen = preferenceManager.createPreferenceScreen(context)

        val placeholder = Preference(context).apply {
            title = "Каталог модов"
            summary = "Здесь будет список доступных модов"
        }
        screen.addPreference(placeholder)

        preferenceScreen = screen
    }
}
