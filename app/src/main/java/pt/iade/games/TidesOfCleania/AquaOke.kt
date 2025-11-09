package pt.iade.games.TidesOfCleania

import android.Manifest
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AquaOke : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)

        // Request microphone permission
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

        }
        requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        setContent {
            MaterialTheme {
                AquaOkeScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AquaOkeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val soundPool = remember {
        SoundPool.Builder()
            .setMaxStreams(7)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .build()
    }

    val notes = remember { mutableStateListOf<Int>() }
    var allLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val rawIds = listOf(
            R.raw.marimba_do,
            R.raw.marimba_re,
            R.raw.marimba_mi,
            R.raw.marimba_fa,
            R.raw.marimba_sol,
            R.raw.marimba_la,
            R.raw.marimba_si
        )
        var loadedCount = 0
        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                notes.add(sampleId)
                loadedCount++
                if (loadedCount == rawIds.size) {
                    allLoaded = true
                }
            }
        }
        rawIds.forEach { soundPool.load(context, it, 1) }
    }

    fun playNote(index: Int) {
        if (allLoaded && index in notes.indices) {
            soundPool.play(notes[index], 1f, 1f, 0, 0, 1f)
        }
    }

    fun playRandomSong() {
        val randomNotes = List(20) { (0..6).random() }
        coroutineScope.launch {
            for (i in randomNotes) {
                playNote(i)
                delay(600L)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AquaOke") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val noteLabels = listOf("Do", "Re", "Mi", "Fa", "Sol", "La", "Si")
            noteLabels.forEachIndexed { index, label ->
                Button(
                    onClick = { playNote(index) },
                    enabled = allLoaded, // disable until loaded
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(label)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { playRandomSong() },
                enabled = allLoaded
            ) {
                Text("Play Random Song")
            }

            if (!allLoaded) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading sounds...")
            }
        }
    }
}
