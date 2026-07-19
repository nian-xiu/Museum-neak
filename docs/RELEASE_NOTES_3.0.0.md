# Museum Heist 2D 3.0.0 发布说明

发布日期：2026-07-18  
版本：3.0.0  
versionCode：30

## 版本概述

3.0.0 是一次覆盖潜行玩法、角色与 NPC 表现、道具建模、十展厅场景、HUD、反馈、无障碍、代码结构和渲染性能的大型更新。版本重点是把原先分散的侦测信息整合为可感知、可统计、可测试的潜行系统，同时提高角色与场景的视觉区分度。

## 主要更新

### 1. 潜行侦测与连携

- 新增 StealthTracker，集中管理全局侦测压力、平滑 HUD 警戒值、峰值警戒和危险进入事件。
- 新增 CLEAR、WATCHED、DANGER 三档潜行状态，并统一驱动 HUD、画面边缘、警告音和震动反馈。
- 新增潜行拾取连携、连携倒计时、最佳连携及奖励机制。
- 连续 4 次安全拾取可触发奖励；高威胁或超时会中断连携。
- 结算数据新增最佳连携和峰值警戒，便于玩家复盘潜行路线。

### 2. 玩家与 NPC 建模

- 玩家携带展品时显示分层挎包、宝石与数量提示。
- 守卫增加胸章、肩部电台和随行动变化的装备细节。
- 工作人员增加挂绳、工作证和夹板，使身份更容易辨认。
- 安保机器人增加天线、状态信标和与警戒级别联动的灯色。
- NPC 脚下增加调查/警报状态环，头顶状态标记统一为问号、感叹号与叉号。
- 玩家移动动画和 NPC 调查行为经过平滑与钳制处理。

### 3. 道具与拾取物

- 五类道具改为独立矢量符号，强化功能辨识度和轮廓层级。
- 干扰器、能量等符号由 IconRenderer 集中绘制，避免 GameView 重复实现。
- 金币、钥匙、普通道具和展品均接入潜行连携逻辑。
- 复用绘制所需的 Path、RectF 等对象，减少热路径中的逐帧内存分配。

### 4. 十展厅场景

- 十个展厅分别配置独立主色、辅助色和氛围色。
- 场景加入地面纹理、墙面灯带、馆标和动态光尘。
- 不同展厅在不改变操作规则的前提下拥有更清晰的视觉主题和空间层次。
- EnvironmentRenderer 负责场景表现，降低主视图的绘制复杂度。

### 5. HUD、交互与体验

- HUD 新增全局警戒条、状态文字、连携等级和倒计时。
- 危险状态提供画面边缘反馈、警告音和兼容版本的震动提示。
- 设置页新增减少动态和大字体选项，并由 ProgressStore 持久化。
- 大字体模式优化关键状态可读性；减少动态模式弱化非必要动画与闪烁。
- 设置页增加布局诊断，降低不同屏幕尺寸下的遮挡风险。
- 通关结算展示更完整的潜行表现统计。

### 6. 中文与编码安全

- JavaCompile 明确设置 <code>UTF-8</code> 编码。
- 中文界面继续使用 Android <code>sans-serif</code> / <code>sans-serif-medium</code> 字体族，避免依赖缺失字体。
- 对项目自有文本执行严格 UTF-8 解码、替换字符、连续问号、常见 mojibake 和 UTF-8 BOM 扫描。
- 受损的 README、发布说明和验证文档已重新以 UTF-8 无 BOM 写入。

### 7. 架构与可维护性

- StealthTracker 保持纯 Java 实现，可脱离 Android 运行时测试。
- EnvironmentRenderer 和 HudRenderer 分别承接场景与 HUD 绘制职责。
- ProgressStore 集中处理六项设置的持久化。
- GameFeedback 安全封装警告音和兼容震动。
- Guard、SecurityCamera、Player 的 delta、suspicion 和移动状态得到钳制与平滑。
- KeyItem、Coin 和 LevelResult 调整为更清晰、可维护的数据结构。

## 测试与构建

- JVM 测试类：7
- JVM 测试用例：45
- 失败：0；错误：0；跳过：0
- Debug 构建：成功
- Release 构建：成功（R8、资源压缩、Gradle 签名和 APK 打包完成）
- APK v1/v2 签名验证：成功
- 新旧版本签名证书 SHA-256 一致，可覆盖升级

## 发布信息

- 包名：<code>com.museumheist</code>
- 应用名：博物馆潜行盗宝
- minSdk：23
- targetSdk：36
- compileSdk：36.1
- Build Tools：36.1.0
- Java 源码兼容级别：17
- APK：<code>Non-project/MuseumHeist2D-v3.0.0-release.apk</code>
- APK 大小：108,604 字节
- APK SHA-256：<code>84915dcb373340c34e7a759410006ad61ce9fc6bed732ec36cf8ac5844ebcc62</code>

## 已知验证限制

- Google Maven 下载 Android Lint 32.2.0 的大型依赖持续超时，Lint 分析没有启动。Release 构建仅在本次命令中临时排除 Vital Lint 任务，项目配置未永久关闭 Lint。
- 构建时没有检测到已连接的 Android 设备，因此尚未进行真机安装、启动、触控和音频/震动回归。
