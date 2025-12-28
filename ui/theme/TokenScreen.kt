package com.example.menuannam // Hoặc com.example.menuannam.ui.theme

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.launch

// --- IMPORT CÁC BIẾN "internal" TỪ NAVIGATOR.KT ---
// Giả sử Navigator.kt nằm trong package "com.example.menuannam.ui.theme"
import com.example.menuannam.ui.theme.EMAIL
import com.example.menuannam.ui.theme.TOKEN
import com.example.menuannam.ui.theme.dataStore
// ----------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenScreen(
    email: String,
    changeMessage: (String) -> Unit,
    navigateToHome: () -> Unit
) {
    var token by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // KHÔNG CÒN KHAI BÁO DATASORE Ở ĐÂY NỮA

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("An email with a token has been sent to $email. Please enter it below.")
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = token,
            onValueChange = { token = it },
            label = { Text("Token") },
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (token.isNotBlank()) {
                    scope.launch {
                        // Ghi vào DataStore (đã được import từ Navigator)
                        context.dataStore.edit { preferences ->
                            preferences[EMAIL] = email
                            preferences[TOKEN] = token
                        }
                        // Điều hướng về nhà
                        navigateToHome()
                    }
                } else {
                    Toast.makeText(context, "Please enter a token.", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text("Enter")
        }
    }
}

