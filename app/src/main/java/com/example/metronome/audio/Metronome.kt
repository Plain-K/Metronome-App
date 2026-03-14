package com.example.metronome.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.util.Log

class Metronome(private val context: Context) {
    private val TAG = "Metronome"
    private var isRunning = false
    private var bpm = 120
    private var beatsPerMeasure = 4
    private var currentBeat = 0
    private val handler = Handler(Looper.getMainLooper())
    private var beatCallback: ((Int) -> Unit)? = null
    
    // 媒体播放器 - 使用木鱼声音
    private var mediaPlayer: MediaPlayer? = null

    init {
        // 初始化媒体播放器
        initMediaPlayer()
    }

    private fun initMediaPlayer() {
        try {
            // 加载木鱼声音文件
            mediaPlayer = createMediaPlayer("muyu.mp3")
            Log.d(TAG, "成功加载木鱼声音文件")
        } catch (e: Exception) {
            Log.e(TAG, "无法加载音频文件: ${e.message}")
            // 如果没有自定义文件，使用系统默认声音
            mediaPlayer = MediaPlayer.create(context, android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
        }
    }

    private fun createMediaPlayer(filename: String): MediaPlayer {
        val assetManager = context.assets
        val afd = assetManager.openFd(filename)
        val player = MediaPlayer()
        player.setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        player.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        player.prepare()
        return player
    }

    fun setBpm(newBpm: Int) {
        bpm = newBpm.coerceIn(30, 240) // 限制BPM范围
        Log.d(TAG, "BPM设置为: $bpm")
    }

    fun setBeatsPerMeasure(beats: Int) {
        beatsPerMeasure = beats.coerceIn(1, 12) // 限制节拍范围
        Log.d(TAG, "节拍模式设置为: $beatsPerMeasure/4")
    }

    fun setBeatCallback(callback: (Int) -> Unit) {
        beatCallback = callback
    }

    fun start() {
        if (!isRunning) {
            isRunning = true
            currentBeat = 0
            Log.d(TAG, "节拍器启动，BPM: $bpm, 节拍: $beatsPerMeasure/4")
            handler.post(beatRunnable)
        }
    }

    fun stop() {
        isRunning = false
        handler.removeCallbacks(beatRunnable)
        Log.d(TAG, "节拍器停止")
    }

    fun isRunning(): Boolean {
        return isRunning
    }

    fun getBpm(): Int {
        return bpm
    }

    fun getBeatsPerMeasure(): Int {
        return beatsPerMeasure
    }

    private val beatRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                currentBeat = (currentBeat % beatsPerMeasure) + 1
                Log.d(TAG, "播放节拍: $currentBeat/$beatsPerMeasure")
                playBeat()
                beatCallback?.invoke(currentBeat)
                
                // 计算下一个节拍的延迟时间（毫秒）
                val delay = (60000 / bpm).toLong()
                handler.postDelayed(this, delay)
            }
        }
    }

    private fun playBeat() {
        // 所有节拍都使用木鱼声音
        mediaPlayer?.let {
            try {
                if (it.isPlaying) {
                    it.seekTo(0)
                }
                it.start()
            } catch (e: Exception) {
                Log.e(TAG, "播放声音失败: ${e.message}")
            }
        }
    }

    fun release() {
        stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
