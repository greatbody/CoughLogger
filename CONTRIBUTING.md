# Contributing to CoughLogger

感谢愿意为本项目贡献！

## 开发环境

- Android Studio Hedgehog+ 或纯命令行
- JDK 17
- Android SDK 34

## 工作流

1. Fork 本仓库并新建分支：`git checkout -b feat/your-feature`
2. 在改动前先开 Issue 讨论较大设计，避免无效工作
3. 提交前确保本地能跑通：
   ```bash
   ./gradlew assembleDebug
   ```
4. Commit message 推荐 [Conventional Commits](https://www.conventionalcommits.org/) 风格：
   - `feat: 新增按小时统计图表`
   - `fix: 修复 Android 14 前台服务崩溃`
   - `chore: 升级 Kotlin 到 2.0`
5. 发 Pull Request，描述清楚动机、做法、自测情况

## 代码风格

- Kotlin official code style（项目 `gradle.properties` 已声明 `kotlin.code.style=official`）
- 优先 Jetpack Compose，避免引入 View 体系
- 避免引入网络/分析/广告 SDK——本项目核心承诺是 100% 离线

## Bug Report

请尽量提供：

- Android 版本、设备型号
- 复现步骤
- 期望 vs 实际行为
- `adb logcat` 相关输出（去除隐私信息）

## License

提交 PR 即表示你同意你的贡献以 [MIT License](LICENSE) 发布。
