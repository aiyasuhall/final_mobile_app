package com.example.menuannam.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(
    tableName = "FlashCards",
    indices = [Index(value = ["english_card", "vietnamese_card"], unique = true)]
)
@Serializable // <-- THÊM ANNOTATION NÀY
data class FlashCard(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "english_card") val englishCard: String?,
    @ColumnInfo(name = "vietnamese_card") val vietnameseCard: String?
)
