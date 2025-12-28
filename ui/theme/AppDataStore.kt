package com.example.menuannam.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// Tạo một thuộc tính mở rộng 'dataStore' cho Context
// Tên "user_preferences" sẽ là tên của tệp lưu trữ trên thiết bị
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
    