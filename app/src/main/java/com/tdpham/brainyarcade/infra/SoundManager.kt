package com.tdpham.brainyarcade.infra

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.util.concurrent.ConcurrentHashMap

/**
 * Singleton SoundManager to handle SFX across the app.
 */
class SoundManager private constructor(context: Context) {
    private val soundPool: SoundPool
    private val soundMap = ConcurrentHashMap<Int, Int>()
    private var enabled = true

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()
    }

    fun load(context: Context, resId: Int) {
        if (!soundMap.containsKey(resId)) {
            val id = soundPool.load(context, resId, 1)
            soundMap[resId] = id
        }
    }

    fun play(resId: Int) {
        if (!enabled) return
        val soundId = soundMap[resId] ?: return
        soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    fun release() {
        soundPool.release()
        soundMap.clear()
        instance = null
    }

    companion object {
        @Volatile
        private var instance: SoundManager? = null

        fun getInstance(context: Context): SoundManager {
            return instance ?: synchronized(this) {
                instance ?: SoundManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
