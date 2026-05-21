package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.MangaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MangaViewModel,
    modifier: Modifier = Modifier
) {
    val isAmoled by viewModel.isAmoledMode.collectAsState()
    val backupMessage by viewModel.backupStatusMessage.collectAsState()
    
    val clipboardManager = LocalClipboardManager.current
    var showExportBackupSheet by remember { mutableStateOf(false) }
    var generatedBackupText by remember { mutableStateOf("") }
    
    var showImportBackupDialog by remember { mutableStateOf(false) }
    var inputBackupJson by remember { mutableStateOf("") }

    val samplePreset = """{
  "version": 1,
  "library": [
    {
      "id": "wt-1",
      "title": "Tower of God (Restored)",
      "author": "SIU",
      "coverUrl": "https://images.unsplash.com/photo-1626544827763-d516dce335e2?w=500&q=80",
      "description": "What do you desire? Money and wealth? Honor and pride? Authority and power?",
      "status": "Ongoing",
      "sourceId": "webtoons",
      "sourceName": "WEBTOON Scraper",
      "category": "Reading"
    }
  ]
}"""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("App Preferences", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // General Display Setting
            item {
                Text(
                    "Appearance & Rendering",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ListItem(
                    headlineContent = { Text("AMOLED Pure Black Theme", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text("Save battery on OLED displays with deep background canvas colors.") },
                    leadingContent = { Icon(imageVector = Icons.Default.Brightness4, contentDescription = null) },
                    trailingContent = {
                        Switch(
                            checked = isAmoled,
                            onCheckedChange = { viewModel.setAmoledMode(it) },
                            modifier = Modifier.testTag("amoled_toggle")
                        )
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                )
            }

            // Backup Restore Settings Section
            item {
                Text(
                    "Backup & Recovery Sync",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            "Export / Import Platform Library",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Create standard portable .json files holding favorites, kategorie lists, reading progress, and customized extensions mappings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    viewModel.createBackup { json ->
                                        generatedBackupText = json
                                        showExportBackupSheet = true
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_backup_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Export JSON")
                                }
                            }

                            Button(
                                onClick = { showImportBackupDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("import_backup_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Restore JSON")
                                }
                            }
                        }
                    }
                }
            }

            // Developer Diagnostics about Ecosystem
            item {
                Text(
                    "Ecosystem Diagnostics",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null)
                            Column {
                                Text("Decentralized Architecture Node", fontWeight = FontWeight.Bold)
                                Text("Platform Engine Core: v1.0.0", style = MaterialTheme.typography.labelSmall)
                                Text("Storage: Safe Offline Sandboxed SQLite", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }
    }

    // Snackbar / Feedback Alert Popup
    if (backupMessage != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissBackupStatus() },
            title = { Text("Backup Engine") },
            text = { Text(backupMessage!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissBackupStatus() }) {
                    Text("Dismiss")
                }
            }
        )
    }

    // Sheet popup: Export JSON show card
    if (showExportBackupSheet) {
        Dialog(onDismissRequest = { showExportBackupSheet = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        "Backup JSON Code",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Copy this backup block and paste it anywhere to restore this state.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Code viewer panel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(10.dp)
                    ) {
                        Text(
                            text = generatedBackupText,
                            fontFamily = FontFamily.Monospace,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(generatedBackupText))
                                showExportBackupSheet = false
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Copy Code")
                        }
                        
                        TextButton(
                            onClick = { showExportBackupSheet = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }

    // Dialog: Import Restore input box
    if (showImportBackupDialog) {
        AlertDialog(
            onDismissRequest = { showImportBackupDialog = false },
            title = { Text("Restore Library Backup") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Paste raw backup JSON text below.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    OutlinedTextField(
                        value = inputBackupJson,
                        onValueChange = { inputBackupJson = it },
                        placeholder = { Text("Enter copy JSON block...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .testTag("import_backup_input_field")
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { inputBackupJson = samplePreset },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Insert Demo Setup String Preset")
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputBackupJson.isNotBlank()) {
                            viewModel.restoreBackup(inputBackupJson)
                            inputBackupJson = ""
                            showImportBackupDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_restore_backup_btn"),
                    enabled = inputBackupJson.isNotBlank()
                ) {
                    Text("Restore Setup")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportBackupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
