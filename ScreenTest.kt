// File: app/src/test/java/com/example/menuannam/ScreenTest.kt
package com.example.menuannam

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.menuannam.ui.theme.AddCardScreen
import com.example.menuannam.ui.theme.AppNavigation
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Hàm tiện ích để setup AppNavigation với Controller giả
    private fun setupAppNav(): TestNavHostController {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val navController = TestNavHostController(context)
        navController.navigatorProvider.addNavigator(ComposeNavigator())

        composeTestRule.setContent {
            // Gọi đúng tên hàm là AppNavigation
            AppNavigation(navController = navController)
        }
        return navController
    }

    @Test
    fun homeScreen_isDisplayed() {
        setupAppNav()
        // Kiểm tra xem tiêu đề ở TopAppBar có hiện không
        composeTestRule.onNodeWithText("An Nam Study Room").assertExists()
        // Kiểm tra xem nút "Study" có hiện không
        composeTestRule.onNodeWithText("Study").assertExists()
    }

    @Test
    fun clickOnStudyCards_navigateToStudyScreen() {
        val navController = setupAppNav()

        // Click vào nút có chữ "Study"
        composeTestRule.onNodeWithText("Study").performClick()

        // Kiểm tra Route
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        assertEquals(Screen.Study.route, currentRoute)
    }

    @Test
    fun clickOnAddCard_navigateToAddScreen() {
        val navController = setupAppNav()

        // Click vào nút "Add"
        composeTestRule.onNodeWithText("Add").performClick()

        // Kiểm tra Route
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        assertEquals(Screen.Add.route, currentRoute)

        // Kiểm tra xem ô nhập liệu "English" có xuất hiện không
        composeTestRule.onNodeWithText("English").assertExists()
    }

    @Test
    fun clickOnSearchCards_navigateToSearchScreen() {
        val navController = setupAppNav()

        // Click vào nút "Search"
        composeTestRule.onNodeWithText("Search").performClick()

        // Kiểm tra Route
        val currentRoute = navController.currentBackStackEntry?.destination?.route
        assertEquals(Screen.Search.route, currentRoute)
    }

    @Test
    fun clickOnAddCardAndBack_returnToHome() {
        val navController = setupAppNav()

        // 1. Vào màn hình Add
        composeTestRule.onNodeWithText("Add").performClick()
        assertEquals(Screen.Add.route, navController.currentBackStackEntry?.destination?.route)

        // 2. Click nút "Back"
        composeTestRule.onNodeWithText("Back").performClick()

        // 3. Kiểm tra đã về Home chưa
        assertEquals(Screen.Main.route, navController.currentBackStackEntry?.destination?.route)
    }

    @Test
    fun typeOnEnTextInput() {
        setupAppNav()

        // Vào màn hình Add
        composeTestRule.onNodeWithText("Add").performClick()

        // Tìm ô có Label là "English" và nhập liệu
        val textToType = "hello"
        composeTestRule.onNodeWithText("English") // Tìm theo Label
            .performTextInput(textToType)

        // Kiểm tra xem text đã được nhập chưa
        composeTestRule.onNodeWithText(textToType).assertExists()
    }

    @Test
    fun viDisplayEmptyEnglish() {
        // 1. Tạo controller giả
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val navController = TestNavHostController(context)

        composeTestRule.setContent {
            // 2. Giả lập tiếng Việt
            DeviceConfigurationOverride(
                DeviceConfigurationOverride.Locales(LocaleList(Locale("vi")))
            ) {
                // 3. SỬA TẠI ĐÂY: Truyền đúng 2 tham số mà lỗi yêu cầu
                AddCardScreen(
                    navigation = navController, // Truyền navController đã tạo ở trên
                    changeMessage = {}          // Truyền hàm rỗng cho changeMessage
                )
            }
        }

        // 4. Kiểm tra hiển thị
        composeTestRule.onNodeWithText("Tiếng Anh").assertExists()
    }
}