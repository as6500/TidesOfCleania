package pt.iade.games.TidesOfCleania

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.media.AudioAttributes
import android.media.SoundPool

class AquaOke : AppCompatActivity() {
    private lateinit var soundPool: SoundPool
    private var si = 0
    private var la = 0
    private var sol = 0
    private var fa = 0
    private var mi = 0
    private var re = 0
    private var doo = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aquaoke)

        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            // go back to home menu
            val intent = Intent(this, HomeMenu::class.java)
            startActivity(intent)
            finish()
        }
        // initialize soundpool
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(7)
            .setAudioAttributes(audioAttributes)
            .build()

        // load notes
        si = soundPool.load(this, R.raw.marimba_si, 1)
        la = soundPool.load(this, R.raw.marimba_la, 1)
        sol = soundPool.load(this, R.raw.marimba_sol, 1)
        fa = soundPool.load(this, R.raw.marimba_fa, 1)
        mi = soundPool.load(this, R.raw.marimba_mi, 1)
        re = soundPool.load(this, R.raw.marimba_re, 1)
        doo = soundPool.load(this, R.raw.marimba_do, 1)

        //functions to play notes
        fun playNoteDo() {
            soundPool.play(doo, 1f, 1f, 0, 0, 1f)
        }
        fun playNoteRe() {
            soundPool.play(re, 1f, 1f, 0, 0, 1f)
        }
        fun playNoteMi() {
            soundPool.play(mi, 1f, 1f, 0, 0, 1f)
        }
        fun playNoteFa() {
            soundPool.play(fa, 1f, 1f, 0, 0, 1f)
        }
        fun playNoteSol() {
            soundPool.play(sol, 1f, 1f, 0, 0, 1f)
        }
        fun playNoteLa() {
            soundPool.play(la, 1f, 1f, 0, 0, 1f)
        }
        fun playNoteSi() {
            soundPool.play(si, 1f, 1f, 0, 0, 1f)
        }

        //buttons
        findViewById<Button>(R.id.buttonC).setOnClickListener { playNoteDo() }
        findViewById<Button>(R.id.buttonD).setOnClickListener { playNoteRe() }
        findViewById<Button>(R.id.buttonE).setOnClickListener { playNoteMi() }
        findViewById<Button>(R.id.buttonF).setOnClickListener { playNoteFa() }
        findViewById<Button>(R.id.buttonG).setOnClickListener { playNoteSol() }
        findViewById<Button>(R.id.buttonA).setOnClickListener { playNoteLa() }
        findViewById<Button>(R.id.buttonB).setOnClickListener { playNoteSi() }
    }
}
