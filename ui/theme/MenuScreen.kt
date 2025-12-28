package com.example.menuannam

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun MenuAnNam(
    navController: NavController,
    isLoggedIn: Boolean, // <-- NHẬN TRẠNG THÁI TỪ BÊN NGOÀI
    onLogout: () -> Unit
) {
    // KHÔNG CẦN DÙNG state, DisposableEffect, scope, context gì ở đây nữa.
    // Composable này bây giờ rất đơn giản, chỉ nhận dữ liệu và hiển thị.

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Các nút chức năng chính ---
        Button(onClick = { navController.navigate(Screen.Study.route) }) { Text("Study") }
        Button(onClick = { navController.navigate(Screen.Add.route) }) { Text("Add") }
        Button(onClick = { navController.navigate(Screen.Search.route) }) { Text("Search") }

        Spacer(modifier = Modifier.height(32.dp))
        Divider()
        Spacer(modifier = Modifier.height(32.dp))

        // --- Logic hiển thị nút Login hoặc Logout ---
        // Đơn giản chỉ cần kiểm tra tham số `isLoggedIn`
        if (isLoggedIn) {
            // Nếu ĐÃ đăng nhập, hiển thị nút "Log out"
            Button(onClick = onLogout) {
                Text("Log out")
            }
        } else {
            // Nếu CHƯA đăng nhập, hiển thị nút "Log in"
            Button(onClick = { navController.navigate(Screen.Login.route) }) {
                Text("Login")
            }
        }
    }
}
