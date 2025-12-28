package com.example.menuannam.ui.theme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.menuannam.data.FlashCard
import com.example.menuannam.data.FlashCardDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.menuannam.database.AppDatabase

@Composable
fun AddCardScreen(
    navigation: NavController,
    changeMessage: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        changeMessage("Day la Add Screen")
    }

    var english by rememberSaveable { mutableStateOf("") }
    var vietnamese by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var database by remember { mutableStateOf<AppDatabase?>(null) }
    var dao by remember { mutableStateOf<FlashCardDao?>(null) }
    var cardList by remember { mutableStateOf<List<FlashCard>>(emptyList()) } // Danh sách hiển thị
    var showList by remember { mutableStateOf(false) } // Hiển thị danh sách?

    // Tạo database trên background
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            database = db
            dao = db.flashCardDao()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = english,
            onValueChange = { english = it },
            label = { Text("English") },
            placeholder = { Text("Enter English word") },
            modifier = Modifier.fillMaxWidth()
//                .fillMaxWidth()
//                .testTag("EnglishInput")
        )

        TextField(
            value = vietnamese,
            onValueChange = { vietnamese = it },
            label = { Text("Vietnamese") },
            placeholder = { Text("Nhập từ tiếng Việt") },
            modifier = Modifier.fillMaxWidth()
        )

        // NÚT CHECK - LẤY TẤT CẢ DỮ LIỆU
//        Button(
//            onClick = {
//                scope.launch(Dispatchers.IO) {
//                    if (dao != null) {
//                        val list = dao!!.getAll()
//                        withContext(Dispatchers.Main) {
//                            cardList = list
//                            showList = true
//                            changeMessage("Found ${list.size} cards!")
//                        }
//                    }
//                }
//            },
//            enabled = dao != null
//        ) {
//            Text("Check Saved Cards")
//        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            Button(onClick = { navigation.navigateUp() }) {
                Text("Back")
            }

            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        val en = english.trim()
                        val vi = vietnamese.trim()
                        if (en.isNotBlank() && vi.isNotBlank() && dao != null) {
                            val existing = dao!!.findByCards(en, vi)
                            withContext(Dispatchers.Main) {
                                if (existing == null) {
                                    dao!!.insertAll(
                                        FlashCard(
                                            englishCard = en,
                                            vietnameseCard = vi
                                        )
                                    )
                                    changeMessage("Card saved!")
                                    english = ""
                                    vietnamese = ""
                                } else {
                                    changeMessage("Card already exists!")
                                }
                            }
                        }
                    }
                },
                enabled = english.isNotBlank() && vietnamese.isNotBlank() && dao != null
            ) {
                Text("Save")
            }

//            Button(
//                onClick = {
//                    scope.launch(Dispatchers.IO) {
//                        if (database != null) {
//                            database!!.clearAllTables()
//                            withContext(Dispatchers.Main) {
//                                changeMessage("All cards deleted!")
//                                cardList = emptyList()
//                                showList = false
//                            }
//                        }
//                    }
//                },
//                enabled = database != null,
//                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
//            ) {
//                Text("Delete All", color = MaterialTheme.colorScheme.onError)
//            }
//        }

            // HIỂN THỊ DANH SÁCH
            if (showList && cardList.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    LazyColumn(modifier = Modifier.padding(8.dp)) {
                        items(cardList) { card ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    card.englishCard ?: "",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text("→", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    card.vietnameseCard ?: "",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Divider()
                        }
                    }
                }
            } else if (showList) {
                Text("No cards found.", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}