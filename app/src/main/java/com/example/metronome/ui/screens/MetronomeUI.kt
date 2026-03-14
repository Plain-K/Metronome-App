package com.example.metronome.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.metronome.audio.Metronome
import com.example.metronome.ui.theme.MetronomeTheme
import kotlinx.coroutines.delay

@Composable
fun MetronomeUI(
    metronome: Metronome,
    onBackgroundModeChanged: (Boolean) -> Unit = {}
) {
    var isRunning by remember { mutableStateOf(false) }
    var bpm by remember { mutableIntStateOf(120) }
    var bpmText by remember { mutableStateOf(bpm.toString()) }
    var beatsPerMeasure by remember { mutableIntStateOf(4) }
    var currentBeat by remember { mutableIntStateOf(0) }
    var allowBackgroundRun by remember { mutableStateOf(false) }
    var showGreenSwitch by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    // 设置节拍回调
    remember {
        metronome.setBeatCallback {
            currentBeat = it
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 后台运行开关（左上角）
        Row(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Switch(
                checked = allowBackgroundRun,
                onCheckedChange = { checked ->
                    allowBackgroundRun = checked
                    onBackgroundModeChanged(checked)
                    showGreenSwitch = true
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = if (showGreenSwitch) Color.Green else MaterialTheme.colorScheme.primary,
                    checkedTrackColor = if (showGreenSwitch) Color.Green.copy(alpha = 0.5f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )
            
            // 1秒后恢复默认颜色
            LaunchedEffect(showGreenSwitch) {
                if (showGreenSwitch) {
                    delay(1000)
                    showGreenSwitch = false
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "后台运行",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 节拍视觉反馈 - 动态圆点数量
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..beatsPerMeasure) {
                    val isActive = currentBeat == i
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 60.dp else 50.dp)
                            .background(
                                color = if (isActive) {
                                    Color.Yellow
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                shape = CircleShape
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // BPM显示和调节
            Text(
                text = "BPM",
                fontSize = 18.sp,
                fontWeight = MaterialTheme.typography.titleMedium.fontWeight
            )
            
            // BPM输入框和加减按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 减号按钮
                Button(
                    onClick = {
                        if (bpm > 30) {
                            bpm--
                            bpmText = bpm.toString()
                            metronome.setBpm(bpm)
                        }
                    },
                    modifier = Modifier.size(72.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text(
                        "−",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // BPM输入框
                OutlinedTextField(
                    value = bpmText,
                    onValueChange = { newValue ->
                        // 只允许数字输入，输入过程中不检查范围
                        if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                            bpmText = newValue
                        }
                    },
                    modifier = Modifier
                        .width(80.dp)
                        .height(72.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            // 完成输入时检查并应用值
                            val newBpm = bpmText.toIntOrNull()
                            if (newBpm != null && newBpm in 30..240) {
                                // 输入有效，更新BPM
                                bpm = newBpm
                                metronome.setBpm(bpm)
                            } else {
                                // 输入无效（为空或超出范围），恢复原值
                                if (newBpm != null) {
                                    // 超出范围，调整为边界值
                                    bpm = newBpm.coerceIn(30, 240)
                                    metronome.setBpm(bpm)
                                }
                                bpmText = bpm.toString()
                            }
                            focusManager.clearFocus()
                        }
                    ),
                    singleLine = true,
                    textStyle = TextStyle(
                        fontSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.width(16.dp))

                // 加号按钮
                Button(
                    onClick = {
                        if (bpm < 240) {
                            bpm++
                            bpmText = bpm.toString()
                            metronome.setBpm(bpm)
                        }
                    },
                    modifier = Modifier.size(72.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text(
                        "+",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // BPM滑动条
            Slider(
                value = bpm.toFloat(),
                onValueChange = {
                    bpm = it.toInt()
                    bpmText = bpm.toString()
                    metronome.setBpm(bpm)
                },
                valueRange = 30f..240f,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 节拍模式选择（三个默认选项）
            Text(
                text = "节拍模式",
                fontSize = 18.sp,
                fontWeight = MaterialTheme.typography.titleMedium.fontWeight
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val beatOptions = listOf(2, 3, 4)
                for (beats in beatOptions) {
                    Button(
                        onClick = {
                            beatsPerMeasure = beats
                            metronome.setBeatsPerMeasure(beats)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (beats == beatsPerMeasure) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.secondary
                            }
                        )
                    ) {
                        Text(text = "$beats/4")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 启动/停止按钮
            Button(
                onClick = {
                    if (isRunning) {
                        metronome.stop()
                    } else {
                        metronome.start()
                    }
                    isRunning = !isRunning
                },
                modifier = Modifier
                    .width(200.dp)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) {
                        Color.Red
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                Text(
                    text = if (isRunning) "停止" else "启动",
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MetronomePreview() {
    MetronomeTheme {
        // 预览时使用LocalContext
        val context = LocalContext.current
        val metronome = Metronome(context)
        MetronomeUI(metronome = metronome)
    }
}
