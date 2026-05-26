package com.greatbody.coughlogger.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.greatbody.coughlogger.data.AppDatabase
import com.greatbody.coughlogger.service.CoughDetectionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).coughEventDao()

    val recent = dao.observeRecent(500)
    val todayCount = dao.countSince(CoughDetectionService.startOfTodayMillis())

    fun exportToClipboard(context: Context, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val all = withContext(Dispatchers.IO) { dao.getAll() }
            if (all.isEmpty()) {
                onResult(false, "暂无数据")
                return@launch
            }
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val sb = StringBuilder()
            sb.append("id,timestamp,datetime,label,score\n")
            for (e in all) {
                sb.append(e.id).append(',')
                    .append(e.timestamp).append(',')
                    .append(sdf.format(Date(e.timestamp))).append(',')
                    .append(e.label).append(',')
                    .append("%.4f".format(e.score)).append('\n')
            }
            copyToClipboard(context, "cough_events.csv", sb.toString())
            onResult(true, "已复制 ${all.size} 条到剪贴板")
        }
    }

    fun clearAll() {
        viewModelScope.launch(Dispatchers.IO) { dao.deleteAll() }
    }
}
