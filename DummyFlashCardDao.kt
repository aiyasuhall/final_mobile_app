// File: app/src/test/java/com/example/menuannam/DummyFlashCardDao.kt
package com.example.menuannam

import android.database.sqlite.SQLiteConstraintException
import com.example.menuannam.data.FlashCard
import com.example.menuannam.data.FlashCardDao

class DummyFlashCardDao : FlashCardDao {

    private val cards = mutableListOf<FlashCard>()

    override suspend fun getAll(): List<FlashCard> = cards.toList()

    override suspend fun loadAllByIds(flashCardIds: IntArray): List<FlashCard> {
        return cards.filter { it.uid in flashCardIds }
    }

    // --- SỬA ĐOẠN NÀY ---
    // Phải trả về FlashCard? (nullable) để logic "if (existing == null)" hoạt động đúng
    override suspend fun findByCards(english: String, vietnamese: String): FlashCard? {
        return cards.find {
            it.englishCard.equals(english, ignoreCase = true) &&
                    it.vietnameseCard.equals(vietnamese, ignoreCase = true)
        }
        // Xóa đoạn "?: FlashCard(...)" đi.
        // Nếu không tìm thấy, nó sẽ tự trả về null.
    }
    // --------------------

    override suspend fun insertAll(vararg flashCard: FlashCard) {
        val hasDuplicate = flashCard.any { new ->
            cards.any {
                it.englishCard.equals(new.englishCard, ignoreCase = true) &&
                        it.vietnameseCard.equals(new.vietnameseCard, ignoreCase = true)
            }
        }
        if (hasDuplicate) throw SQLiteConstraintException("Duplicate")

        // Giả lập việc tạo UID tự động (DB thật tự làm, còn Dummy phải tự tăng)
        val newCardsWithIds = flashCard.mapIndexed { index, card ->
            if (card.uid == 0) card.copy(uid = (cards.maxOfOrNull { it.uid } ?: 0) + 1 + index) else card
        }
        cards.addAll(newCardsWithIds)
    }

    override suspend fun update(card: FlashCard) {
        cards.removeAll { it.uid == card.uid }
        cards.add(card)
    }

    override suspend fun update(vararg cards: FlashCard) {
        cards.forEach { update(it) }
    }

    override suspend fun delete(flashCard: FlashCard) {
        cards.removeAll { it.uid == flashCard.uid }
    }
}

class DummyFlashCardDaoUnsuccessfulInsert : FlashCardDao {
    override suspend fun getAll() = emptyList<FlashCard>()
    override suspend fun loadAllByIds(flashCardIds: IntArray) = emptyList<FlashCard>()

    // Cũng sửa luôn class này cho đồng bộ return type
    override suspend fun findByCards(english: String, vietnamese: String): FlashCard? = null

    override suspend fun insertAll(vararg flashCard: FlashCard) {
        throw SQLiteConstraintException()
    }
    override suspend fun update(card: FlashCard) {}
    override suspend fun update(vararg cards: FlashCard) {}
    override suspend fun delete(flashCard: FlashCard) {}
}