package com.mind.ps2emulator

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val PREFS_NAME = "ps2_emulator_library"
private const val GAMES_KEY = "game_uris"
private const val BIOS_KEY = "bios_uri"

private data class GameEntry(val uri: String, val name: String)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmulatorHome(
                initialGames = loadSavedGames(),
                initialBiosName = loadBiosName(),
                onImportGame = ::saveGame,
                onImportBios = ::saveBios,
                onRemoveGame = ::removeGame
            )
        }
    }

    private fun displayName(uri: Uri): String {
        var cursor: Cursor? = null
        return try {
            cursor = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index) else null
            } else null
        } catch (_: Exception) {
            null
        } finally {
            cursor?.close()
        } ?: uri.lastPathSegment?.substringAfterLast('/') ?: "Selected file"
    }

    private fun keepReadAccess(uri: Uri) {
        contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    private fun loadSavedGames(): List<GameEntry> {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        return prefs.getStringSet(GAMES_KEY, emptySet()).orEmpty().mapNotNull { raw ->
            runCatching {
                val uri = Uri.parse(raw)
                GameEntry(raw, displayName(uri))
            }.getOrNull()
        }.distinctBy { it.uri }.sortedBy { it.name.lowercase() }
    }

    private fun saveGame(uri: Uri): GameEntry? {
        val name = displayName(uri)
        val extension = name.substringAfterLast('.', "").lowercase()
        if (extension !in setOf("iso", "chd")) return null
        return try {
            keepReadAccess(uri)
            val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            val saved = prefs.getStringSet(GAMES_KEY, emptySet()).orEmpty().toMutableSet()
            saved.add(uri.toString())
            prefs.edit().putStringSet(GAMES_KEY, saved).apply()
            GameEntry(uri.toString(), name)
        } catch (_: SecurityException) {
            null
        }
    }

    private fun removeGame(entry: GameEntry) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val saved = prefs.getStringSet(GAMES_KEY, emptySet()).orEmpty().toMutableSet()
        saved.remove(entry.uri)
        prefs.edit().putStringSet(GAMES_KEY, saved).apply()
        runCatching {
            contentResolver.releasePersistableUriPermission(
                Uri.parse(entry.uri), Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
    }

    private fun saveBios(uri: Uri): String? {
        val name = displayName(uri)
        val extension = name.substringAfterLast('.', "").lowercase()
        if (extension !in setOf("bin", "rom", "bios")) return null
        return try {
            keepReadAccess(uri)
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putString(BIOS_KEY, uri.toString()).apply()
            name
        } catch (_: SecurityException) {
            null
        }
    }

    private fun loadBiosName(): String? {
        val raw = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).getString(BIOS_KEY, null) ?: return null
        return runCatching { displayName(Uri.parse(raw)) }.getOrNull()
    }
}

@Composable
private fun EmulatorHome(
    initialGames: List<GameEntry>,
    initialBiosName: String?,
    onImportGame: (Uri) -> GameEntry?,
    onImportBios: (Uri) -> String?,
    onRemoveGame: (GameEntry) -> Unit
) {
    val importedGames = remember { mutableStateListOf<GameEntry>().apply { addAll(initialGames) } }
    var biosName by remember { mutableStateOf(initialBiosName) }
    var message by remember { mutableStateOf<String?>(null) }

    val gameLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            val entry = onImportGame(uri)
            if (entry != null) {
                if (importedGames.none { it.uri == entry.uri }) importedGames.add(entry)
                message = "Game added to your library"
            } else {
                message = "Choose a supported .iso or .chd game image"
            }
        }
    }
    val biosLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            biosName = onImportBios(uri)
            message = if (biosName != null) "BIOS file saved" else "Choose a BIOS file (.bin, .rom or .bios)"
        }
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF070B16)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(Color(0xFF101D3A), Color(0xFF080D1B), Color(0xFF050811)))
                ).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(Modifier.height(28.dp))
                    Text("PLAYSTATION 2", color = Color(0xFFE8F0FF), fontSize = 25.sp,
                        fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    Text("EMULATOR • ANDROID", color = Color(0xFF8FADE0), fontSize = 11.sp, letterSpacing = 3.sp)
                    Spacer(Modifier.height(26.dp))
                    BootPanel()
                    if (message != null) {
                        Text(message!!, color = Color(0xFFB9D4FF), fontSize = 12.sp,
                            modifier = Modifier.padding(top = 10.dp))
                    }
                }
                item {
                    Text("BROWSER", color = Color(0xFFE5EDFF), fontSize = 18.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    Text("Your game collection", color = Color(0xFF93A3C4), fontSize = 13.sp)
                }
                item {
                    ActionCard("Import game image", "Choose a game file you own (.iso / .chd)",
                        { Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFB9D4FF)) }) {
                        gameLauncher.launch(arrayOf("*/*"))
                    }
                }
                if (importedGames.isEmpty()) {
                    item { EmptyLibrary() }
                } else {
                    items(importedGames, key = { it.uri }) { entry ->
                        GameRow(entry) {
                            onRemoveGame(entry)
                            importedGames.remove(entry)
                            message = "Game removed from your library"
                        }
                    }
                }
                item {
                    Text("SYSTEM", color = Color(0xFFE5EDFF), fontSize = 18.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    ActionCard("BIOS manager", biosName ?: "No BIOS file selected",
                        { Icon(Icons.Default.Memory, contentDescription = null, tint = Color(0xFFB9D4FF)) }) {
                        biosLauncher.launch(arrayOf("*/*"))
                    }
                    ActionCard("System configuration", "Graphics, audio and controller settings",
                        { Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFFB9D4FF)) }) {
                        message = "Graphics, audio and controller options require the emulation engine."
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Your game and BIOS selections are stored on this device. Game execution is not available until a PS2 emulation engine is integrated.",
                        color = Color(0xFF7384A5), fontSize = 11.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BootPanel() {
    Box(
        modifier = Modifier.fillMaxWidth().height(176.dp).background(
            Brush.linearGradient(listOf(Color(0xFF1D376A), Color(0xFF101A36), Color(0xFF090E1C))),
            RoundedCornerShape(22.dp)
        ).padding(20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text("THE NEXT LEVEL", color = Color(0xFFBBD4FF), fontSize = 11.sp, letterSpacing = 4.sp)
            Spacer(Modifier.height(8.dp))
            Text("Your console experience\nstarts here.", color = Color.White, fontSize = 23.sp,
                fontWeight = FontWeight.Bold, lineHeight = 29.sp)
            Spacer(Modifier.height(12.dp))
            Text("Native Android interface", color = Color(0xFF9EB4DE), fontSize = 12.sp)
        }
    }
}

@Composable
private fun ActionCard(title: String, subtitle: String, icon: @Composable () -> Unit, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14213B))) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(modifier = Modifier.size(44.dp).background(Color(0xFF22385F), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center) { icon() }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Color(0xFF9AAAC8), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun EmptyLibrary() {
    Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF10192C), RoundedCornerShape(18.dp))
        .padding(26.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Gamepad, contentDescription = null, tint = Color(0xFF8FADE0), modifier = Modifier.size(38.dp))
        Spacer(Modifier.height(10.dp))
        Text("No games imported", color = Color.White, fontWeight = FontWeight.SemiBold)
        Text("Add a game image to see it in your browser.", color = Color(0xFF91A0BC), fontSize = 12.sp)
    }
}

@Composable
private fun GameRow(entry: GameEntry, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().background(Color(0xFF14213B), RoundedCornerShape(14.dp))
            .padding(start = 15.dp, top = 12.dp, bottom = 12.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Gamepad, contentDescription = null, tint = Color(0xFFB9D4FF))
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(entry.name, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("Game image • Ready for engine", color = Color(0xFF91A0BC), fontSize = 10.sp)
        }
        Icon(
            Icons.Default.Delete,
            contentDescription = "Remove game",
            tint = Color(0xFF91A0BC),
            modifier = Modifier.size(22.dp).clickable(onClick = onRemove).padding(2.dp)
        )
    }
}
