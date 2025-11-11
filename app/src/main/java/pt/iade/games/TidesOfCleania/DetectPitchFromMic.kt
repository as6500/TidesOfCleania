package pt.iade.games.tidesofcleania

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.android.AudioDispatcherFactory
import be.tarsos.dsp.pitch.PitchDetectionHandler
import be.tarsos.dsp.pitch.PitchDetectionResult
import be.tarsos.dsp.pitch.PitchProcessor

fun DetectPitchFromMic(
    context: Context,
    activity: Activity,
    callback: (result: PitchDetectionResult, event: AudioEvent?) -> Unit
) {
    if (ContextCompat.checkSelfPermission(context,
            Manifest.permission.RECORD_AUDIO)
        != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(activity,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            1234)
    }

    val dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(
        48000, 2048, 0)
    val p: AudioProcessor = PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN,
        48000f, 2048,
        PitchDetectionHandler { result, e -> callback(result, e) })
    dispatcher.addAudioProcessor(p)
    Thread(dispatcher, "Audio Dispatcher").start()
}