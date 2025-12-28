package com.example.menuannam.ui.theme

import android.content.Context
import android.util.Base64
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import com.example.menuannam.*
import com.example.menuannam.data.FlashCard
import com.example.menuannam.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyScreen(
    navController: NavController,
    changeMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // States
    var lesson by remember { mutableStateOf<List<FlashCard>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var isEnglishShown by remember { mutableStateOf(true) }
    var isLoadingLesson by remember { mutableStateOf(true) }
    var isGeneratingAudio by remember { mutableStateOf(false) }
    var player by remember { mutableStateOf<ExoPlayer?>(null) }

    suspend fun loadNewLesson() {
        isLoadingLesson = true
        player?.release()
        player = null
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context)
            val newLesson = db.flashCardDao().getLesson(3)
            withContext(Dispatchers.Main) {
                lesson = newLesson
                currentIndex = 0
                isEnglishShown = true
                isLoadingLesson = false
                changeMessage("New lesson loaded!")
            }
        }
    }

    LaunchedEffect(Unit) { loadNewLesson() }

    DisposableEffect(Unit) { onDispose { player?.release() } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lesson (3 Cards)") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { scope.launch { loadNewLesson() } }) {
                        Icon(Icons.Default.Refresh, "New Lesson")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            if (isLoadingLesson) {
                CircularProgressIndicator()
            } else if (lesson.isEmpty()) {
                Text("No cards found. Please add at least 1 card.", textAlign = TextAlign.Center)
            } else {
                val currentCard = lesson[currentIndex]
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text("Card ${currentIndex + 1} / ${lesson.size}", style = MaterialTheme.typography.labelLarge)

                    Card(
                        modifier = Modifier.fillMaxWidth().height(200.dp).clickable { isEnglishShown = !isEnglishShown },
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Box(Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                                if (isGeneratingAudio) {
                                    CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                                } else {
                                    IconButton(
                                        onClick = {
                                            val wordToGenerate = if (isEnglishShown) {
                                                currentCard.englishCard.orEmpty()
                                            } else {
                                                currentCard.vietnameseCard.orEmpty()
                                            }
                                            scope.launch {
                                                playOrGenerateAudio(context, wordToGenerate, changeMessage,
                                                    onLoadingChange = { isLoading -> isGeneratingAudio = isLoading },
                                                    onPlayerReady = { newPlayer ->
                                                        player?.release()
                                                        player = newPlayer
                                                    }
                                                )
                                            }
                                        },
                                        enabled = !isGeneratingAudio
                                    ) {
                                        Icon(Icons.Filled.VolumeUp, "Play Audio")
                                    }
                                }
                            }

                            Text(
                                text = if (isEnglishShown) currentCard.englishCard.orEmpty() else currentCard.vietnameseCard.orEmpty(),
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(32.dp)
                            )

                            Text(
                                text = if (isEnglishShown) "Tap to see Meaning" else "Tap to see English",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
                                color = Color.Gray
                            )
                        }
                    }

                    if (!isEnglishShown) {
                        Button(
                            onClick = {
                                currentIndex = (currentIndex + 1) % lesson.size
                                isEnglishShown = true
                                player?.release()
                                player = null
                            },
                            modifier = Modifier.width(200.dp)
                        ) {
                            Text("Next Card")
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, null)
                        }
                    } else {
                        Spacer(Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}

// --- HÀM HỖ TRỢ AUDIO (PHIÊN BẢN HOÀN THIỆN) ---

private suspend fun playOrGenerateAudio(
    context: Context,
    word: String,
    changeMessage: (String) -> Unit,
    onLoadingChange: (Boolean) -> Unit, // Callback để cập nhật trạng thái loading
    onPlayerReady: (ExoPlayer) -> Unit
) {
    if (word.isBlank()){
        changeMessage("No word to generate.")
        return
    }

    val fileName = "audio_${word.replace(" ", "_").lowercase()}.mp3"
    val file = File(context.filesDir, fileName)

    if (file.exists()) {
        changeMessage("Playing from cache...")
        val player = createAndPlayExoPlayer(context, file, changeMessage)
        onPlayerReady(player)
        return
    }

    // Nếu file chưa tồn tại -> bắt đầu quá trình loading
    onLoadingChange(true)
    changeMessage("Generating audio for '$word'...")

    val prefs = context.dataStore.data.firstOrNull()
    val email = prefs?.get(EMAIL)
    val token = prefs?.get(TOKEN)

    if (email == null || token == null) {
        changeMessage("Error: Not logged in.")
        onLoadingChange(false) // Tắt loading nếu lỗi
        return
    }

    try {
        val request = GenerateAudioRequest(word, email, token)
        val response = NetworkModule.networkService.generateAudio(request)

        if (response.code == 200) {
            val audioData = Base64.decode(response.message, Base64.DEFAULT)
            saveAudioToInternalStorage(context, audioData, fileName)
            val newFile = File(context.filesDir, fileName)
            val player = createAndPlayExoPlayer(context, newFile, changeMessage)
            onPlayerReady(player)
        } else {
            changeMessage("API Error: ${response.message}")
        }
    } catch (e: Exception) {
        changeMessage("Network Error: ${e.message}")
    } finally {
        onLoadingChange(false) // Luôn tắt loading khi quá trình kết thúc (dù thành công hay thất bại)
    }
}

private fun saveAudioToInternalStorage(context: Context, audioData: ByteArray, filename: String) {
    FileOutputStream(File(context.filesDir, filename)).use { it.write(audioData) }
}

private fun createAndPlayExoPlayer(context: Context, audioFile: File, changeMessage: (String) -> Unit): ExoPlayer {
    return ExoPlayer.Builder(context).build().apply {
        setMediaItem(MediaItem.fromUri(audioFile.toUri()))
        prepare()
        play()
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if(isPlaying) changeMessage("Playing...")
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    changeMessage("Finished.")
                    release()
                }
            }
        })
    }
}
