package com.example.metronome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.metronome.audio.Metronome
import com.example.metronome.ui.screens.MetronomeUI
import com.example.metronome.ui.theme.MetronomeTheme

class MainActivity : ComponentActivity() {
    private lateinit var metronome: Metronome
    private var allowBackgroundRun = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 初始化节拍器，传入Context
        metronome = Metronome(this)
        
        setContent {
            MetronomeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MetronomeUI(
                        metronome = metronome,
                        onBackgroundModeChanged = { allow ->
                            allowBackgroundRun = allow
                        }
                    )
                }
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        // 如果不允许后台运行，暂停时停止节拍器
        if (!allowBackgroundRun && metronome.isRunning()) {
            metronome.stop()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        metronome.release()
    }
}
