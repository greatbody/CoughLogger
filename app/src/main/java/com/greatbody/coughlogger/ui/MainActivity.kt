package com.greatbody.coughlogger.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.greatbody.coughlogger.data.CoughEvent
import com.greatbody.coughlogger.service.CoughDetectionService
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CoughLoggerApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoughLoggerApp(vm: MainViewModel = viewModel()) {
    val context = LocalContext.current
    var running by remember { mutableStateOf(isServiceRunning(context)) }
    val today by vm.todayCount.collectAsStateWithLifecycle(initialValue = 0)
    val history by vm.recent.collectAsStateWithLifecycle(initialValue = emptyList())

    val micPerm = rememberMicPermissionLauncher { granted ->
        if (granted) {
            CoughDetectionService.start(context)
            running = true
        } else {
            Toast.makeText(context, "需要麦克风权限", Toast.LENGTH_SHORT).show()
        }
    }
    val notifPerm = rememberNotifPermissionLauncher { /* ignore */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notifPerm.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("CoughLogger") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("今日咳嗽", fontSize = 14.sp)
                    Text("$today", fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (running) "监听中" else "已停止",
                        color = if (running) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        if (running) {
                            CoughDetectionService.stop(context)
                            running = false
                        } else {
                            if (ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                            ) {
                                CoughDetectionService.start(context)
                                running = true
                            } else {
                                micPerm.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text(if (running) "停止监听" else "开始监听") }

                OutlinedButton(
                    onClick = {
                        vm.exportToClipboard(context) { ok, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("导出剪贴板") }
            }

            OutlinedButton(
                onClick = { vm.clearAll() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("清空记录") }

            Divider()
            Text("最近记录 (${history.size})", fontWeight = FontWeight.SemiBold)
            LazyColumn(modifier = Modifier.weight(1f)) {
                lazyItems(items = history, key = { e: CoughEvent -> e.id }) { e ->
                    EventRow(e)
                    Divider()
                }
                if (history.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) { Text("暂无记录") }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventRow(e: CoughEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(SDF.format(Date(e.timestamp)))
        Text("score=${"%.2f".format(e.score)}")
    }
}

private val SDF = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

@Composable
private fun rememberMicPermissionLauncher(onResult: (Boolean) -> Unit) =
    androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult
    )

@Composable
private fun rememberNotifPermissionLauncher(onResult: (Boolean) -> Unit) =
    androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult
    )

private fun isServiceRunning(context: Context): Boolean {
    @Suppress("DEPRECATION")
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
    @Suppress("DEPRECATION")
    return am.getRunningServices(Int.MAX_VALUE).any {
        it.service.className == CoughDetectionService::class.java.name
    }
}

fun copyToClipboard(context: Context, label: String, text: String) {
    val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
}
