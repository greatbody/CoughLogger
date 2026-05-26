# CoughLogger

通过手机麦克风持续监听并自动记录咳嗽事件的 Android 应用。

## 功能

- 后台持续监听麦克风（前台 Service，常驻通知）
- 使用 **YAMNet** (TFLite, 521 类音频事件分类) 检测 `Cough` 类
- 记录每次咳嗽的时间戳 + 置信度，写入 Room 数据库
- 主界面：今日次数 / 历史列表 / 开始-停止 / 清空
- 一键将全部记录以 **CSV** 复制到剪贴板

## 技术栈

- Kotlin + Jetpack Compose (Material3)
- Room (SQLite)
- TensorFlow Lite Task Audio (YAMNet)
- Foreground Service (`microphone` 类型)
- minSdk 26, targetSdk 34

## 权限

- `RECORD_AUDIO` — 监听麦克风
- `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MICROPHONE` — 后台监听 (Android 14+ 必需)
- `POST_NOTIFICATIONS` — Android 13+ 通知显示

## 构建

```bash
./gradlew assembleDebug
```

需要：Android SDK 34，JDK 17。

## 调参

`CoughDetector.kt` 中：
- `THRESHOLD = 0.30f` — 越高越严格，越低越敏感
- `DEDUP_MS = 700` — 同一次咳嗽去重窗口
- `INFER_INTERVAL_MS = 250` — 推理间隔（YAMNet 单次约 1s 音频）

## 隐私

所有推理、数据完全本地，无任何网络上传。
