package com.example.menuannam.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.menuannam.GenerateTokenRequest
import com.example.menuannam.NetworkModule
import com.example.menuannam.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    changeMessage: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) } // Thêm trạng thái loading
//    val context = LocalContext.current

    LaunchedEffect(Unit) {
        changeMessage("Please, introduce your email.")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Cards") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") },
                    singleLine = true,
                    enabled = !isLoading // Vô hiệu hóa khi đang tải
                )

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    // Vô hiệu hóa nút khi đang gọi API hoặc email trống
                    enabled = !isLoading && email.isNotBlank(),
                    onClick = {
                        // BẮT ĐẦU GỌI API
                        scope.launch {
                            isLoading = true // Bắt đầu loading
                            changeMessage("Requesting token, please wait...")
                            try {
                                // Gọi API generateToken từ NetworkModule
                                val response = NetworkModule.networkService.generateToken(
                                    request = GenerateTokenRequest(email = email)
                                )

                                // Kiểm tra kết quả từ server
                                if (response.code == 200) {
                                    changeMessage("Token sent! Please check your email.")
                                    // Nếu thành công, điều hướng đến TokenScreen và truyền email
                                    navController.navigate(Screen.Token.createRoute(email))
                                } else {
                                    // Nếu server trả về lỗi, hiển thị message lỗi
                                    changeMessage("Error: ${response.message}")
                                }

                            } catch (e: Exception) {
                                // Xử lý các lỗi mạng hoặc lỗi khác
                                changeMessage("Network error: ${e.message}")
                            } finally {
                                isLoading = false // Kết thúc loading
                            }
                        }
                    }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    } else {
                        Text("Get Token")
                    }
                }
            }
        }
    }
}
