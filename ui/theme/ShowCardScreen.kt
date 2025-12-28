package com.example.menuannam.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.menuannam.Screen.EditCard
import com.example.menuannam.data.FlashCard
import com.example.menuannam.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowCardScreen(
    navController: NavController,
    changeMessage: (String) -> Unit,
    cardId: Int
) {
    LaunchedEffect(Unit) { changeMessage("Show Card") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var card by remember { mutableStateOf<FlashCard?>(null) }
    var isDeleted by remember { mutableStateOf(false) }

    // Load card từ DB
    LaunchedEffect(cardId) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            val c = db.flashCardDao().loadAllByIds(intArrayOf(cardId)).firstOrNull()
            withContext(Dispatchers.Main) { card = c }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Flash Card") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isDeleted) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text("Card deleted!", color = Color.Green, style = MaterialTheme.typography.titleLarge)
            }
            return@Scaffold
        }

        card?.let { flashCard ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // === PHẦN NỘI DUNG CARD ===
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = flashCard.englishCard.orEmpty(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(32.dp))
                    Text("=", style = MaterialTheme.typography.displayMedium)
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = flashCard.vietnameseCard.orEmpty(),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // === PHẦN 2 NÚT DƯỚI CÙNG ===
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Nút Delete
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                val db = AppDatabase.getDatabase(context)
                                db.flashCardDao().delete(flashCard)
                                withContext(Dispatchers.Main) {
                                    isDeleted = true
                                    changeMessage("Card deleted")
                                    kotlinx.coroutines.delay(800)
                                    navController.popBackStack()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) {
//                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Delete")
                    }

                    // Nút Edit – nằm ngay cạnh Delete
                    Button(
                        onClick = {
                            navController.navigate(EditCard.createRoute(cardId))
                        },
                        modifier = Modifier.weight(1f)
                    ) {
//                        Icon(Icons.Default.Edit, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Edit")
                    }
                }
            }
        } ?: Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}