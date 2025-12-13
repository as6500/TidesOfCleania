package pt.iade.games.tidesofcleania

import android.Manifest
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.httpPost
import com.github.kittinunf.fuel.httpPut
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import kotlin.math.*

class AquaOke : ComponentActivity() {
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)

        val pairingCode = intent.getStringExtra(EXTRA_PAIRING_CODE)
            ?: error("Pairing code missing")

        setContent {
            var pitch by remember { mutableFloatStateOf(0f) }
            val context = LocalContext.current
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission Accepted: Do something
                    Log.d("HomeScreen", "PERMISSION GRANTED")
                    DetectPitchFromMic(
                        context = context,
                        activity = this,
                        callback = { result, event ->
                            pitch = result.pitch
                            if (pitch > 0) { // valid pitch
                                val note = frequencyToNoteAllOctaves(pitch)
                                Log.i("Pitch", "Detected note: $note ($pitch Hz)")
                            }
                            //Log.i("Pitch", "Pitch is " + result.pitch + " Hz");
                        })
                } else {
                    // Permission Denied: Do something
                    Log.d("HomeScreen", "PERMISSION DENIED")
                }
            }

            MaterialTheme {
                AquaOkeScreen(pitch, onBack = { finish() })
                SideEffect {
                    launcher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AquaOkeScreen(
    pitch: Float,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Notes displayed on screen (ignore the octave)
    val noteBoxes = listOf("C", "D", "E", "F", "G", "H", "I")
    val boxHeight = 100.dp

    // Detect if we have a valid pitch
    val hasPitch = pitch > 0f

    // Get note name from detected pitch
    val noteName = remember(pitch) {
        if (hasPitch) frequencyToNoteAllOctaves(pitch)
        else "No Pitch Detected"
    }
    val baseNote = noteName.takeWhile { it.isLetter() }

    // Find matching box index
    val noteIndex = if (hasPitch)
        noteBoxes.indexOfFirst { it == baseNote }.takeIf { it >= 0 } ?: 0
    else
        -1

    // Animate vertical position of the pitch line
    val animatedOffset by animateDpAsState(
        targetValue = (noteIndex * boxHeight.value + boxHeight.value / 2).dp,
        label = "pitchLineOffset"
    )

    // Moving notes state
    var movingNotes by remember { mutableStateOf(listOf<MovingNote>()) }
    var twinkleIndex by remember { mutableStateOf(0) }


    // Score and gamestate
    var score by remember { mutableStateOf(0) }
    var spawnedNotes by remember { mutableStateOf(0) }
    var totalNotes by remember { mutableStateOf(10) }
    var gameOver by remember { mutableStateOf(false) }

    //buff settings
    val maxBuffDurationMinutes = 10 //maximum buff that can be awarded
    val completionThreshold = 0.2f // 20% or above to get a buff
    val fullBuffPercentage = 25 //how much buff is awarded, for the text only


    // update notes position and spawn new notes
    LaunchedEffect(Unit) {
        while (true) {
            val deltaTime = 16L // this 60 FPS
            val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()

            // move each note leftt and remove those that passed the left side
            movingNotes = movingNotes.map { note ->
                note.copy(x = note.x - note.speed * deltaTime / 1000f)
            }.filter { it.x + 50f > 0f } // 50f = note width, remove if off scree

            // Spawn new notes every 300px
            if (spawnedNotes < totalNotes && (movingNotes.isEmpty() || movingNotes.last().x < screenWidth - 300f)) {
                movingNotes = movingNotes + generateTwinkleNote(screenWidth) //CHANGE HERE FOR RANDOM MODE
                spawnedNotes += 1
            }

            // end game after 10 notes and theres no more notes
            if ( spawnedNotes >= totalNotes && movingNotes.isEmpty()) {
                gameOver = true
            }

            kotlinx.coroutines.delay(deltaTime)
        }
    }

    val density = LocalDensity.current
    val hitThreshold = with(density) { 20.dp.toPx() } // vertical difference in pixels to count as a note hit
    val animatedOffsetPx = with(density) { animatedOffset.toPx() } // convert pitch line to px
    val boxHeightPx = with(density) { boxHeight.toPx() } // convert box height to px

    // hit detection
    LaunchedEffect(movingNotes, pitch) {
        if (hasPitch) {

            val screenWidth = context.resources.displayMetrics.widthPixels.toFloat()
            val noteWidth = 50f

            val pitchLineX = screenWidth * 0.25f   // 25% from left
            val hitRangeX = 60f                    // how close horizontally counts

            val toRemove = movingNotes.filter { note ->
                val noteYPx =
                    noteBoxes.indexOf(note.note) * boxHeightPx + boxHeightPx / 2

                val noteXpx = with(density) { note.x.dp.toPx() }

                val isVerticallyAligned =
                    abs(animatedOffsetPx - noteYPx) < hitThreshold

                val isHorizontallyAligned =
                    noteXpx in (pitchLineX - hitRangeX)..(pitchLineX + hitRangeX)

                isVerticallyAligned && isHorizontallyAligned && note.note == baseNote
            }

            if (toRemove.isNotEmpty()) {
                score += toRemove.size
                movingNotes = movingNotes - toRemove
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AquaOke") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            if (gameOver) { //show game over screen instead of board if the game is over

                // calculate buff duration
                val buffDurationMinutes = if (score >= spawnedNotes * completionThreshold) {
                    ((score.toFloat() / spawnedNotes.toFloat()) * maxBuffDurationMinutes).toInt()
                } else {
                    0
                }

                //TODO: Update server with new values
                LaunchedEffect(Unit) {
                    val bodyJson = """
                    {
                      "boostDuration": $buffDurationMinutes,
                      "pairingCode": "$pairingCode"
                    }
                    """


                    "https://tidesofrubbish.onrender.com/hello"
                        .httpPut()
                        .header("Content-Type" to "application/json")
                        .body(bodyJson)
                        .response { _, _, result ->
                            result.fold(
                                success = { bytes ->
                                    Log.i("Server", String(bytes))
                                },
                                failure = { error ->
                                    Log.e("Server", error.toString())
                                }
                            )
                        }
                }



                // game over screen
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xAA000000)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Game Over", fontSize = 40.sp, color = Color.White)
                        Text("Score: $score / $totalNotes", fontSize = 24.sp, color = Color.White)

                        // buff info in endscreen display
                        if (buffDurationMinutes > 0) {
                            Text(
                                "Buff Awarded: +$fullBuffPercentage% speed for $buffDurationMinutes minutes",
                                fontSize = 20.sp,
                                color = Color.Green,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Text("No buff awarded",
                                fontSize = 20.sp,
                                color = Color.Red,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center)
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Button(onClick = {
                            // reset game for next time
                            score = 0
                            spawnedNotes = 0
                            movingNotes = emptyList()
                            twinkleIndex = 0
                            gameOver = false
                        }) {
                            Text("Play Again")
                        }
                    }
                }
            } else {
                // main game screen
                Column(verticalArrangement = Arrangement.Top, modifier = Modifier.align(Alignment.TopStart)) {
                    val colors = listOf(
                        Color(0xffe83b3b),
                        Color(0xfffb6b1d),
                        Color(0xfff79617),
                        Color(0xff91db69),
                        Color(0xff4d9be6),
                        Color(0xffa884f3),
                        Color(0xfff04f78)
                    )
                    noteBoxes.forEachIndexed { index, note ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(boxHeight)
                                .background(colors[index])
                        ) {
                            Text(note, fontSize = 40.sp, modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp))
                        }
                    }
                }

                // draw moving notes
                movingNotes.forEach { note ->
                    val noteY = noteBoxes.indexOf(note.note) * boxHeight.value // vertical position in dp
                    Box(
                        modifier = Modifier
                            .offset(x = note.x.dp, y = noteY.dp)
                            .size(50.dp, boxHeight)
                            .background(Color.White)
                    ) {
                        Text(note.note, modifier = Modifier.align(Alignment.Center), fontSize = 24.sp, color = Color.Black)
                    }
                }

                // draw pitch line
                if (hasPitch) {
                    Box(
                        modifier = Modifier
                            .offset(y = animatedOffset)
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color.Black)
                    )
                }

                // score display
                Column(modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)) {
                    Text("Score: $score / $totalNotes", fontSize = 20.sp)
                    Text("Pitch: %.2f Hz".format(pitch))
                    Text("Note: $noteName")
                }
            }
        }
    }
}

// moving note class and generator
data class MovingNote(
    val note: String, //letter of the note
    var x: Float, //current x of the note
    val speed: Float = 200f //horizontal speed
)

//RANDOM MODE (not used when twinkle twinkle mode is on)
fun generateNote(screenWidth: Float): MovingNote {
    val notes = listOf("Do", "Re", "Me", "Fa", "So", "La", "Ti")
    val note = notes.random() //pick a random note
    return MovingNote(note = note, x = screenWidth, speed = 200f) //spawn on the right
}

//TWINKLE TWINKLE MODE (not used when random mode is on)
var twinkleIndex = 0
val twinkleNotes = listOf("C", "D", "G", "E", "A", "F", "G",
    "F", "B", "E", "C", "A", "D", "C") //twinkle twinkle notes

fun generateTwinkleNote(screenWidth: Float): MovingNote {
    val note = twinkleNotes[twinkleIndex] //pick note from the current twinkleIndex
    twinkleIndex = (twinkleIndex + 1) % twinkleNotes.size //advance index and start again if needed
    return MovingNote(note = note, x = screenWidth, speed = 200f) //spawn note on the right
}

@Preview(showBackground = true)
@Composable
fun AquaOkeScreenPreview() {
    AquaOkeScreen(440f, {})
}

fun frequencyToNoteAllOctaves(freq: Float): String {
    // Create an array of the 12 note names in one octave
    val notes = arrayOf("C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B")
    // Check if there is no sound to evaluate
    if (freq <= 0) return "Invalid frequency"
    // Get the number of semitones difference from A4(440 Hz)
    val numberOfSemitones = 12 * log2(freq / 440.0)
    // Rounding to get to the nearest semitone
    val semitone = numberOfSemitones.roundToInt()
    // Get the note from the semitone
    val noteIndex = (semitone + 9).mod(12)
    // Get the octave from the semitone
    val octave = 4 + ((semitone + 9) / 12)
    return "${notes[noteIndex]}$octave"
}
