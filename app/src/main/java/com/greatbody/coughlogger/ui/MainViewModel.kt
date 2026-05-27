package com.greatbody.coughlogger.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.greatbody.coughlogger.data.AppDatabase
import com.greatbody.coughlogger.service.CoughDetectionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).coughEventDao()

    val recent = dao.observeRecent(500)
    val todayCount = dao.countSince(CoughDetectionService.startOfTodayMillis())

    /** 选中日期（当天 0:00 的毫秒值） */
    private val _selectedDateStart = MutableStateFlow(startOfDay(System.currentTimeMillis()))
    val selectedDateStart: StateFlow<Long> = _selectedDateStart

    @OptIn(ExperimentalCoroutinesApi::class)
    val hourlyCounts: StateFlow<IntArray> = _selectedDateStart
        .flatMapLatest { start ->
            val end = start + DAY_MS
            dao.observeBetween(start, end).map { events ->
                val bins = IntArray(24)
                val cal = Calendar.getInstance()
                for (e in events) {
                    cal.timeInMillis = e.timestamp
                    bins[cal.get(Calendar.HOUR_OF_DAY)]++
                }
                bins
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IntArray(24))

    fun shiftSelectedDate(days: Int) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = _selectedDateStart.value
            add(Calendar.DAY_OF_MONTH, days)
        }
        _selectedDateStart.value = cal.timeInMillis
    }

    fun jumpToToday() {
        _selectedDateStart.value = startOfDay(System.currentTimeMillis())
    }

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

    companion object {
        const val DAY_MS = 24L * 60 * 60 * 1000

        fun startOfDay(ts: Long): Long {
            val c = Calendar.getInstance().apply {
                timeInMillis = ts
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return c.timeInMillis
        }
    }
}
