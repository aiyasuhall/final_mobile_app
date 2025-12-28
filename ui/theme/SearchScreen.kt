package com.example.menuannam.ui.theme

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.menuannam.Screen
import com.example.menuannam.data.FlashCard
import com.example.menuannam.data.FlashCardDao
import com.example.menuannam.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FlashCardList(
    flashCards: List<FlashCard>,
    onCardSelected: (FlashCard) -> Unit
) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        items(flashCards, key = { it.uid }) { card ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color.LightGray)
                    .padding(6.dp)
                    .clickable { onCardSelected(card) }
            ) {
                Column(modifier = Modifier.padding(6.dp)) { Text(card.englishCard.orEmpty()) }
                Column(modifier = Modifier.padding(6.dp)) { Text(" = ") }
                Column(modifier = Modifier.padding(6.dp)) { Text(card.vietnameseCard.orEmpty()) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navigation: NavController,
    changeMessage: (String) -> Unit
) {
    LaunchedEffect(Unit) { changeMessage("Day la Search Screen") }

    val context = LocalContext.current

    var dao by remember { mutableStateOf<FlashCardDao?>(null) }
    var cards by remember { mutableStateOf<List<FlashCard>>(emptyList()) }
//    val scope = rememberCoroutineScope()
//    var database by remember { mutableStateOf<AppDatabase?>(null) }
//    var cardList by remember { mutableStateOf<List<FlashCard>>(emptyList()) } // Danh sách hiển thị
//    var showList by remember { mutableStateOf(false) } // Hiển thị danh sách?

    // ---- Init DB -------------------------------------------------
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            dao = db.flashCardDao()
        }
    }

    // ---- Load cards when DAO is ready ----------------------------
    LaunchedEffect(dao) {
        dao?.let {
            withContext(Dispatchers.IO) {
                val list = it.getAll()
                withContext(Dispatchers.Main) { cards = list }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Cards") },
                navigationIcon = {
                    IconButton(onClick = { navigation.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            if (cards.isEmpty()) {
                Text("No cards yet – add some in \"Add\".", color = Color.Gray)
            } else {
                FlashCardList(
                    flashCards = cards,
                    onCardSelected = { card ->
                        // ---- TYPE-SAFE NAVIGATION ---------------------------------
                        navigation.navigate(Screen.ShowCard.createRoute(card.uid))
                        // -----------------------------------------------------------
                    }
                )
            }
        }
//        Button(
//            onClick = {
//                scope.launch(Dispatchers.IO) {
//                    if (database != null) {
//                        database!!.clearAllTables()
//                        withContext(Dispatchers.Main) {
//                            changeMessage("All cards deleted!")
//                            cardList = emptyList()
//                            showList = false
//                        }
//                    }
//                }
//            },
//            enabled = database != null,
//            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
//        ) {
//            Text("Delete All", color = MaterialTheme.colorScheme.onError)
//        }
    }
}
