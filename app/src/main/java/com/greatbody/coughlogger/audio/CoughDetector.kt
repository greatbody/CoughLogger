package com.greatbody.coughlogger.audio

import android.content.Context
import android.util.Log
import com.greatbody.coughlogger.data.AppDatabase
import com.greatbody.coughlogger.data.CoughEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.support.audio.TensorAudio
import org.tensorflow.lite.task.audio.classifier.AudioClassifier

/**
 * 持续从 mic 采集音频，喂给 YAMNet。
 * 检测到 "Cough" 且 score >= THRESHOLD 时写入 DB。
 * 简单去重：DEDUP_MS 内只算一次。
 */
class CoughDetector(
    private val context: Context,
    private val onDetected: (CoughEvent) -> Unit = {}
) {
    private val tag = "CoughDetector"
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch { runLoop() }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private suspend fun runLoop() {
        val classifier = try {
            AudioClassifier.createFromFile(context, MODEL_FILE)
        } catch (t: Throwable) {
            Log.e(tag, "Failed to load YAMNet", t)
            return
        }
        val tensor: TensorAudio = classifier.createInputTensorAudio()
        val record = classifier.createAudioRecord()
        try {
            record.startRecording()
        } catch (t: Throwable) {
            Log.e(tag, "startRecording failed", t)
            classifier.close()
            return
        }

        val db = AppDatabase.get(context).coughEventDao()
        var lastCoughAt = 0L

        Log.i(tag, "Detection loop started")
        while (scope.isActive && job?.isActive == true) {
            try {
                tensor.load(record)
                val results = classifier.classify(tensor)
                val categories = results.firstOrNull()?.categories ?: emptyList()
                val cough = categories.firstOrNull {
                    val name = it.label?.lowercase().orEmpty()
                    name == "cough" || name.contains("cough")
                }
                if (cough != null && cough.score >= THRESHOLD) {
                    val now = System.currentTimeMillis()
                    if (now - lastCoughAt >= DEDUP_MS) {
                        lastCoughAt = now
                        val event = CoughEvent(
                            timestamp = now,
                            score = cough.score,
                            label = cough.label ?: "Cough"
                        )
                        withContext(Dispatchers.IO) { db.insert(event) }
                        onDetected(event)
                        Log.i(tag, "Cough detected score=${cough.score}")
                    }
                }
            } catch (t: Throwable) {
                Log.w(tag, "inference error", t)
            }
            delay(INFER_INTERVAL_MS)
        }

        try { record.stop() } catch (_: Throwable) {}
        try { record.release() } catch (_: Throwable) {}
        classifier.close()
        Log.i(tag, "Detection loop stopped")
    }

    companion object {
        private const val MODEL_FILE = "yamnet.tflite"
        private const val THRESHOLD = 0.30f
        private const val DEDUP_MS = 700L
        private const val INFER_INTERVAL_MS = 250L
    }
}
