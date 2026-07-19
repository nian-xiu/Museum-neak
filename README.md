# Museum Heist 2D

一款使用 Android Java、SurfaceView 与 Canvas 实现的 2D 博物馆潜行游戏。当前源码构建版本为 **4.1.0（versionCode 41）**。

> `docs` 目录目前保留 3.0.0 的版本说明与验证记录；实际构建版本以 `app/build.gradle` 为准。

## 已记录的 3.0.0 更新亮点

### 潜行玩法

- 新增全局侦测压力系统，统一守卫、摄像头与场景警戒反馈。
- 新增 CLEAR、WATCHED、DANGER 三档状态，以及渐变式 HUD 警戒条和危险边缘提示。
- 新增潜行拾取连携、连携倒计时、最佳连携、奖励与超时中断机制。
- 结算页加入峰值警戒和最佳连携统计，使路线规划与风险控制更有反馈。

### 角色、NPC 与道具

- 玩家携带展品时会显示分层挎包、宝石和数量，移动动画更加平滑。
- 守卫增加胸章、肩部电台和装备；工作人员增加挂绳、证件和夹板。
- 机器人增加状态信标、天线及随警戒变化的灯色，NPC 脚下增加调查/警报环。
- 五类道具由 IconRenderer 集中绘制，图形辨识度和渲染复用性更高。

### 场景与交互体验

- 十个展厅拥有独立色彩方案、地面纹理、灯带、馆标与动态光尘。
- HUD 新增全局警戒、状态文字、连携层级和剩余时间显示。
- 新增危险警告音和兼容震动反馈，并提供减少动态、大字体等可持久化设置。
- 中文界面统一使用 Android sans-serif 字体族，Java 编译编码固定为 UTF-8。

### 代码结构与性能

- 将纯玩法状态抽离为 StealthTracker，便于 JVM 单元测试和后续扩展。
- 场景与 HUD 分别由 EnvironmentRenderer、HudRenderer 负责，降低 GameView 的绘制职责。
- 优化守卫、摄像头和玩家的 delta、警戒钳制与调查行为。
- 复用 Path、RectF 等绘制对象，减少高频绘制路径中的临时对象和垃圾回收压力。

完整更新记录见 <code>docs/RELEASE_NOTES_3.0.0.md</code>，验证记录见 <code>docs/VALIDATION_3.0.0.md</code>。

## 项目结构

- <code>app/src/main</code>：Android 应用源码和资源。
- <code>app/src/test</code>：JVM 单元测试。
- <code>docs</code>：版本说明和验证记录。
- <code>Non-project</code>：本地发布 APK、SHA-256 摘要及签名材料；该目录已被 <code>.gitignore</code> 排除，不随源码上传。

## 构建环境

- Java 源码兼容级别：17；本次本地构建使用 JDK 21。
- Android Gradle Plugin 9.2.0、Gradle 9.4.1。
- Android SDK Platform 36.1、Build Tools 36.1.0。
- 项目通过 <code>local.properties</code> 指向本地 Android SDK。

Debug 测试和构建命令：

~~~powershell
$env:GRADLE_USER_HOME = "$PWD\.gradle-build"
$env:ANDROID_USER_HOME = "$PWD\.android-home"
$env:TEMP = "$PWD\.tmp-build"
$env:TMP = $env:TEMP
.\gradlew.bat --offline --no-configuration-cache testDebugUnitTest assembleDebug
~~~

## Release 签名

Release 构建通过 Gradle 属性读取以下配置：

- <code>MUSEUM_HEIST_STORE_FILE</code>
- <code>MUSEUM_HEIST_STORE_PASSWORD</code>
- <code>MUSEUM_HEIST_KEY_ALIAS</code>
- <code>MUSEUM_HEIST_KEY_PASSWORD</code>

在 PowerShell 中可使用 <code>ORG_GRADLE_PROJECT_</code> 前缀映射这些属性。不要把密码写入源码、文档、构建日志或版本控制。

## 本地历史发布材料（不纳入版本控制）

- APK：<code>Non-project/MuseumHeist2D-v3.0.0-release.apk</code>
- SHA-256：<code>84915dcb373340c34e7a759410006ad61ce9fc6bed732ec36cf8ac5844ebcc62</code>
- 包名：<code>com.museumheist</code>
- 应用名：博物馆潜行盗宝
- minSdk：23；targetSdk：36

3.0.0 发布版本已通过 Debug 编译、45 项 JVM 单元测试、Release 构建、APK 清单检查和 v1/v2 签名验证。由于 Google Maven 下载 Lint 32.2.0 大型依赖持续超时，完整 Android Lint 未能启动；当前也未连接 Android 设备，因此未执行真机安装和启动验证。详情见验证文档。
