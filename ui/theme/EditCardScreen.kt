// app/src/main/java/com/example/menuannam/EditCardScreen.kt
package com.example.menuannam.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.menuannam.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.filled.ArrowBack


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCardScreen(
    navController: NavController,
    cardId: Int
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var english by remember { mutableStateOf("") }
    var vietnamese by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    // Load card từ database
    LaunchedEffect(cardId) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            val card = db.flashCardDao().loadAllByIds(intArrayOf(cardId)).firstOrNull()
            withContext(Dispatchers.Main) {
                if (card != null) {
                    english = card.englishCard.orEmpty()
                    vietnamese = card.vietnameseCard.orEmpty()
                }
                isLoading = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Card") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = english,
                    onValueChange = { english = it },
                    label = { Text("Tiếng Anh") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = vietnamese,
                    onValueChange = { vietnamese = it },
                    label = { Text("Tiếng Việt") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                val db = AppDatabase.getDatabase(context)
                                val dao = db.flashCardDao()
                                val oldCard = dao.loadAllByIds(intArrayOf(cardId)).firstOrNull()
                                if (oldCard != null) {
                                    val updated = oldCard.copy(
                                        englishCard = english.trim(),
                                        vietnameseCard = vietnamese.trim()
                                    )
                                    dao.update(updated) // Room sẽ tự UPDATE vì có uid
                                }
                                withContext(Dispatchers.Main) {
                                    navController.popBackStack()
                                }
                            }
                        },
                        enabled = english.isNotBlank() && vietnamese.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}