package io.github.romanvht.byedpi.ui.mods

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.github.romanvht.byedpi.mods.Mod
import io.github.romanvht.byedpi.mods.ModManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModsScreen() {
    val context = LocalContext.current
    val mods = remember { ModManager.getAllMods() }
    val enabledStates = remember {
        mutableStateMapOf<String, Boolean>().apply {
            mods.forEach { put(it.id, ModManager.isEnabled(it.id)) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Моды") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(mods, key = { it.id }) { mod ->
                ModItem(
                    mod = mod,
                    enabled = enabledStates[mod.id] ?: false,
                    onToggle = { enabled ->
                        ModManager.setEnabled(context, mod, enabled)
                        enabledStates[mod.id] = enabled
                    },
                    onOpenSettings = { mod.openSettings(context) }
                )
            }
        }
    }
}

@Composable
fun ModItem(
    mod: Mod,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(mod.iconResId),
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(mod.name, style = MaterialTheme.typography.titleMedium)
            Text(
                mod.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (mod.hasSettings && enabled) {
                TextButton(onClick = onOpenSettings) {
                    Text("Настройки мода")
                }
            }
        }
        Switch(
            checked = enabled,
            onCheckedChange = onToggle
        )
    }
}
