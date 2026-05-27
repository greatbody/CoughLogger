# CoughLogger

[![CI](https://github.com/greatbody/CoughLogger/actions/workflows/ci.yml/badge.svg)](https://github.com/greatbody/CoughLogger/actions/workflows/ci.yml)
[![Release](https://img.shields.io/github/v/release/greatbody/CoughLogger?include_prereleases)](https://github.com/greatbody/CoughLogger/releases)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![minSdk](https://img.shields.io/badge/minSdk-26-green.svg)](#)

通过手机麦克风持续监听并自动记录咳嗽事件的 Android 应用。完全离线、不联网、不上传任何音频。

> An Android app that continuously listens for coughs via the microphone and logs each event locally. 100% on-device, no network, no audio leaves the phone.

## 功能 Features

- 后台持续监听麦克风（前台 Service + 常驻通知）
- 使用 **YAMNet** (TFLite, 521 类音频事件分类) 检测 `Cough` 类
- 记录每次咳嗽的时间戳与置信度，写入本地 Room/SQLite
- 主界面：今日次数 / 历史列表 / 开始-停止 / 清空
- 一键将全部记录以 **CSV** 复制到剪贴板（方便贴到表格/笔记/AI 分析）

## 截图 Screenshots

> TODO：欢迎贡献截图 PR。

## 安装 Install

从 [Releases](https://github.com/greatbody/CoughLogger/releases) 下载最新 `app-debug.apk`，传到手机安装即可（需允许"未知来源"）。

或自行构建：

```bash
git clone https://github.com/greatbody/CoughLogger.git
cd CoughLogger
./gradlew assembleDebug
# APK 在 app/build/outputs/apk/debug/app-debug.apk
```

要求：Android SDK 34、JDK 17。

## 权限 Permissions

| 权限 | 用途 |
|------|------|
| `RECORD_AUDIO` | 监听麦克风做咳嗽分类 |
| `FOREGROUND_SERVICE` + `FOREGROUND_SERVICE_MICROPHONE` | Android 14+ 后台持续监听 |
| `POST_NOTIFICATIONS` | 显示常驻通知 |

应用不申请任何网络权限。

## 技术栈 Tech Stack

- Kotlin + Jetpack Compose (Material3)
- Room (SQLite)
- TensorFlow Lite Task Audio (YAMNet)
- Foreground Service (`microphone` 类型)
- minSdk 26, targetSdk 34

## 调参 Tuning

`app/src/main/java/com/greatbody/coughlogger/audio/CoughDetector.kt`：

| 参数 | 默认 | 说明 |
|------|------|------|
| `THRESHOLD` | `0.30f` | 分类置信度阈值。越高越严格、越少误报；越低越敏感、易把笑声/清嗓识别为咳嗽 |
| `DEDUP_MS` | `700` | 去重窗口，同一次连续咳嗽只记一次 |
| `INFER_INTERVAL_MS` | `250` | 推理间隔，单位毫秒 |

## 隐私 Privacy

- 所有音频处理与推理均在设备本地完成
- 不录音、不保存音频文件，仅保存"咳嗽事件 = (时间戳, 置信度)"
- 不申请、不使用 INTERNET 权限
- 数据仅以 SQLite 存于应用沙箱；卸载即清空

## 贡献 Contributing

欢迎 PR 与 Issue。请阅读 [CONTRIBUTING.md](CONTRIBUTING.md) 与 [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md)。

适合上手的方向：
- 接入更轻量/更准确的咳嗽专用模型替换 YAMNet
- 图表统计（按小时/天/周）
- 导出 JSON、写入文件、分享到其他 App
- 多语言（当前主要中文 UI）
- 单元测试与 instrumentation 测试

## 第三方组件 Third-Party Notices

本仓库包含的 `app/src/main/assets/yamnet.tflite` 为 Google **YAMNet** 模型，采用 [Apache 2.0 License](https://www.apache.org/licenses/LICENSE-2.0) 发布，原始项目见
<https://github.com/tensorflow/models/tree/master/research/audioset/yamnet>。

其它运行时依赖（AndroidX、TensorFlow Lite、Material3 等）均采用 Apache 2.0。

## License

[MIT](LICENSE) © greatbody
