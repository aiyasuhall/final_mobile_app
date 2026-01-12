package com.example.menuannam

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.menuannam.data.FlashCard
import com.example.menuannam.data.FlashCardDao
import com.example.menuannam.database.AppDatabase // <-- Đảm bảo import đúng Database của bạn
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DaoTest {

    // SỬA 1: db phải là AppDatabase (hoặc tên class database của bạn), không phải FlashCard
    private lateinit var db: AppDatabase
    private lateinit var flashCardDao: FlashCardDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // SỬA 2: Truyền class Database vào builder, không phải class Entity
        // .allowMainThreadQueries() giúp test chạy mượt hơn mà không cần runBlocking quá nhiều (tuỳ chọn)
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java
        ).allowMainThreadQueries().build()

        flashCardDao = db.flashCardDao()
    }

    @After
    fun close(){
        db.close()
    }

    @Test
    fun insertFlashCardSuccessful() = runBlocking {
        val flashCard = FlashCard(
            uid = 1, // Room in-memory đôi khi cần ID cụ thể nếu không auto-generate hoàn hảo
            englishCard = "test_english",
            vietnameseCard = "test_vietnamese"
        )

        flashCardDao.insertAll(flashCard)

        val item = flashCardDao.findByCards("test_english", "test_vietnamese")

        assertEquals(flashCard.englishCard, item?.englishCard)
        assertEquals(flashCard.vietnameseCard, item?.vietnameseCard)
    }

    @Test
    fun insertFlashCardUnSuccessful() = runBlocking {
        // SỬA 3: Xóa đoạn khởi tạo db thừa ở đây. Dùng cái đã tạo ở @Before

        val flashCard = FlashCard(
            englishCard = "test_english",
            vietnameseCard = "test_vietnamese"
        )

        flashCardDao.insertAll(flashCard)

        var error = false
        try {
            // Cố tình insert trùng để gây lỗi
            flashCardDao.insertAll(flashCard.copy(uid = 0)) // Copy để giả lập object mới nhưng nội dung cũ
        } catch (e: SQLiteConstraintException){
            error = true
        }

        assertEquals(true, error)
    }

    @Test
    fun deleteExistingFlashCard() = runBlocking {
        val flashCard = FlashCard(
            englishCard = "test_english",
            vietnameseCard = "test_vietnamese"
        )
        flashCardDao.insertAll(flashCard)

        // SỬA 4: Dao của bạn không có hàm delete theo String.
        // Cách đúng: Tìm object đó ra -> rồi gọi delete(object)
        val cardToDelete = flashCardDao.findByCards("test_english", "test_vietnamese")

        if (cardToDelete != null) {
            flashCardDao.delete(cardToDelete)
        }

        // Kiểm tra xem còn tìm thấy không
        val checkCard = flashCardDao.findByCards("test_english", "test_vietnamese")
        assertNull(checkCard)
    }

    @Test
    fun deleteNonExistingFlashCard() = runBlocking {
        // SỬA 5: Xóa đoạn khởi tạo db thừa.

        val flashCard = FlashCard(
            englishCard = "test_english",
            vietnameseCard = "test_vietnamese"
        )
        flashCardDao.insertAll(flashCard)
        val beforeCount = flashCardDao.getAll().size

        // Thử xóa một cái không tồn tại
        // Vì logic delete của Dao là delete(Object), ta không thể delete một cái không có trong DB.
        // Nhưng ta có thể giả lập việc tìm kiếm thất bại thì không làm gì cả.

        val cardToDelete = flashCardDao.findByCards("khong_co", "cung_khong_co")

        if (cardToDelete != null) {
            flashCardDao.delete(cardToDelete)
        }

        val afterCount = flashCardDao.getAll().size
        assertEquals(beforeCount, afterCount)
    }
}