package io.github.romanvht.byedpi.ui.mods

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.romanvht.byedpi.R
import io.github.romanvht.byedpi.mods.ModManager

class ModsFragment : PreferenceFragmentCompat() {

    private var fab: FloatingActionButton? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        buildPreferenceScreen()
    }

    /**
     * Пересобирает список модов из ModManager.
     * Вызывается при создании и при возвращении на экран.
     */
    private fun buildPreferenceScreen() {
        val context = requireContext()
        ModManager.init(context)

        val screen = preferenceManager.createPreferenceScreen(context)

        val seenIds = mutableSetOf<String>()
        ModManager.getAllMods().forEach { mod ->
            if (seenIds.add(mod.id)) {
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
        }

        preferenceScreen = screen
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fab = requireActivity().findViewById(R.id.fab_add_mod)
        fab?.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.settings, ModCatalogFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Пересобираем список — на случай, если после установки мода он изменился
        buildPreferenceScreen()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        fab?.visibility = View.GONE
        fab = null
    }
}
