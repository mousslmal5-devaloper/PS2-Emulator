package com.mind.ps2emulator

import android.net.Uri
import android.os.Bundle
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EmulatorHome(
                onImportSelected = { uri ->
                    contentResolver.takePersistableUriPermission(
                        uri,
                        android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                }
            )
        }
    }
}

@Composable
private fun EmulatorHome(onImportSelected: (Uri) -> Unit) {
    val importedGames = remember { mutableStateListOf<String>() }
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            runCatching { onImportSelected(uri) }
            val name = uri.lastPathSegment?.substringAfterLast('/') ?: "Selected game file"
            if (name !in importedGames) importedGames.add(name)
        }
    }

    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF070B16)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF101D3A), Color(0xFF080D1B), Color(0xFF050811))
                        )
                    )
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(Modifier.height(28.dp))
                    Text(
                        text = "PLAYSTATION 2",
                        color = Color(0xFFE8F0FF),
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "EMULATOR • ANDROID",
                        color = Color(0xFF8FADE0),
                        fontSize = 11.sp,
                        letterSpacing = 3.sp
                    )
                    Spacer(Modifier.height(26.dp))
                    BootPanel()
                }
                item {
                    Text(
                        text = "BROWSER",
                        color = Color(0xFFE5EDFF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Your game collection",
                        color = Color(0xFF93A3C4),
                        fontSize = 13.sp
                    )
                }
                item {
                    ActionCard(
                        title = "Import game image",
                        subtitle = "Choose a game file you own (.iso / .chd)",
                        icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFB9D4FF)) },
                        onClick = { importLauncher.launch(arrayOf("*/*")) }
                    )
                }
                if (importedGames.isEmpty()) {
                    item {
                        EmptyLibrary()
                    }
                } else {
                    items(importedGames.size) { index ->
                        GameRow(importedGames[index])
                    }
                }
                item {
                    Text(
                        text = "SYSTEM",
                        color = Color(0xFFE5EDFF),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    ActionCard(
                        title = "BIOS manager",
                        subtitle = "Configure a BIOS file supplied by you",
                        icon = { Icon(Icons.Default.Memory, contentDescription = null, tint = Color(0xFFB9D4FF)) },
                        onClick = { }
                    )
                    ActionCard(
                        title = "System configuration",
                        subtitle = "Graphics, audio and controller settings",
                        icon = { Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFFB9D4FF)) },
                        onClick = { }
                    )
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = "Emulation engine integration is not yet enabled.",
                        color = Color(0xFF7384A5),
                        fontSize = 11.sp,
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
        modifier = Modifier
            .fillMaxWidth()
            .height(176.dp)
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF1D376A), Color(0xFF101A36), Color(0xFF090E1C))
                ),
                RoundedCornerShape(22.dp)
            )
            .padding(20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column {
            Text(
                text = "THE NEXT LEVEL",
                color = Color(0xFFBBD4FF),
                fontSize = 11.sp,
                letterSpacing = 4.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Your console experience\nstarts here.",
                color = Color.White,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 29.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Native Android interface",
                color = Color(0xFF9EB4DE),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14213B))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF22385F), RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) { icon() }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Color(0xFF9AAAC8), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun EmptyLibrary() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF10192C), RoundedCornerShape(18.dp))
            .padding(26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Gamepad, contentDescription = null, tint = Color(0xFF8FADE0), modifier = Modifier.size(38.dp))
        Spacer(Modifier.height(10.dp))
        Text("No games imported", color = Color.White, fontWeight = FontWeight.SemiBold)
        Text("Add a game image to see it in your browser.", color = Color(0xFF91A0BC), fontSize = 12.sp)
    }
}

@Composable
private fun GameRow(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF14213B), RoundedCornerShape(14.dp))
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Gamepad, contentDescription = null, tint = Color(0xFFB9D4FF))
        Text(name, color = Color.White, modifier = Modifier.padding(start = 12.dp), fontSize = 13.sp)
    }
}
