// File: app/src/main/java/com/example/menuannam/Screen.kt
package com.example.menuannam

import androidx.navigation.NavType
import androidx.navigation.navArgument

// TẤT CẢ CÁC MÀN HÌNH
sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Add : Screen("add")
    object Study : Screen("study")
    object Search : Screen("search")
    object Login : Screen("login")

    object Token : Screen("token_screen/{email}") { // Thêm route cho TokenScreen
        fun createRoute(email: String) = "token_screen/$email"
    }

    // Xem chi tiết card
    object ShowCard : Screen("showCard/{cardId}") {
        fun createRoute(cardId: Int) = "showCard/$cardId"
    }

    // SỬA CARD – BẮT BUỘC PHẢI CÓ DÒNG NÀY
    object EditCard : Screen("editCard/{cardId}") {
        fun createRoute(cardId: Int) = "editCard/$cardId"
    }
}

val showCardArguments = listOf(
    navArgument("cardId") { type = NavType.IntType }
)

val editCardArguments = listOf(
    navArgument("cardId") { type = NavType.IntType }
)