package io.github.romanvht.byedpi.ui.mods

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import io.github.romanvht.byedpi.mods.ModManager

class ModsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        val context = requireContext()
        ModManager.init(context)

        val screen = preferenceManager.createPreferenceScreen(context)

        ModManager.getAllMods().forEach { mod ->
            val pref = SwitchPreferenceCompat(context).apply {
                key = mod.id
                title = mod.name
                summary = mod.description
                isChecked = ModManager.isEnabled(mod.id)
                setOnPreferenceChangeListener { _, newValue ->
                    ModManager.setEnabled(context, mod, newValue as Boolean)
                    true
                }
            }
            screen.addPreference(pref)
        }

        preferenceScreen = screen
    }
}
