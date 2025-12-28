package com.example.menuannam.ui.theme

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.text.style.TextAlign
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.menuannam.* // Import mọi thứ cần thiết
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- PHẦN SỬA LỖI BẮT ĐẦU ---

    // 1. State cho thông báo tạm thời
    var temporaryMessage by remember { mutableStateOf<String?>(null) }

    // 2. Lắng nghe trạng thái email từ DataStore
    val emailState by context.dataStore.data
        .map { preferences -> preferences[EMAIL] }
        .collectAsState(initial = null)

    // 3. Hàm `changeMessage` mới: Cập nhật thông báo tạm thời
    val changeMessage: (String) -> Unit = { newMessage ->
        temporaryMessage = newMessage
    }

    // 4. Logic hiển thị cho BottomBar
    val bottomBarText = temporaryMessage ?: emailState ?: "Please log in"
    // Ưu tiên 1: Hiển thị thông báo tạm thời (nếu có)
    // Ưu tiên 2: Hiển thị email (nếu có)
    // Ưu tiên 3: Hiển thị "Please log in"

    // --- PHẦN SỬA LỖI KẾT THÚC ---


    val onLogout: () -> Unit = {
        scope.launch {
            context.dataStore.edit { preferences ->
                preferences.remove(EMAIL)
                preferences.remove(TOKEN)
            }
            // Khi logout, xóa luôn thông báo tạm thời
            temporaryMessage = null
        }
    }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("An Nam Study Room") }) },
        bottomBar = {
            BottomAppBar {
                Text(
                    text = bottomBarText, // <-- Sử dụng biến đã được tính toán
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Main.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Khi điều hướng đến Main, xóa thông báo tạm thời để hiển thị lại email
            composable(Screen.Main.route) {
                // `LaunchedEffect` để hành động này chỉ chạy 1 lần khi vào màn hình
                LaunchedEffect(Unit) {
                    temporaryMessage = null
                }
                MenuAnNam(
                    navController = navController,
                    isLoggedIn = emailState != null,
                    onLogout = onLogout
                )
            }

            // Các màn hình khác bây giờ có thể sử dụng `changeMessage` hiệu quả
            composable(Screen.Add.route)    { AddCardScreen(navController, changeMessage) }
            composable(Screen.Study.route)  { StudyScreen(navController, changeMessage) }
            composable(Screen.Search.route) { SearchScreen(navController, changeMessage) }

            // Khi vào màn hình Login, cũng xóa thông báo tạm thời
            composable(Screen.Login.route)  {
                LaunchedEffect(Unit) {
                    temporaryMessage = null
                }
                LoginScreen(navController, changeMessage)
            }

            composable(
                route = Screen.Token.route,
                // ... (giữ nguyên phần còn lại)
                arguments = listOf(navArgument("email") { type = NavType.StringType })
            ) { backStackEntry ->
                val emailArg = backStackEntry.arguments?.getString("email") ?: ""
                TokenScreen(
                    email = emailArg,
                    changeMessage = changeMessage,
                    navigateToHome = {
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // ... (các composable khác giữ nguyên)
            composable(
                route = Screen.ShowCard.route,
                arguments = showCardArguments
            ) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getInt("cardId") ?: 0
                ShowCardScreen(
                    navController = navController,
                    changeMessage = changeMessage,
                    cardId = cardId
                )
            }
            composable(
                route = Screen.EditCard.route,
                arguments = editCardArguments
            ) { backStackEntry ->
                val cardId = backStackEntry.arguments?.getInt("cardId") ?: 0
                EditCardScreen(navController = navController, cardId = cardId)
            }
        }
    }
}
