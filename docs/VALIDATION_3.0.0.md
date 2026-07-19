# Museum Heist 2D 3.0.0 验证记录

验证日期：2026-07-18  
工作区：<code>D:\4Test-Folder\codex-Test-one\Preliminary-app-one</code>

## 构建环境

- Gradle：9.4.1
- Android Gradle Plugin：9.2.0
- compileSdk / targetSdk：36.1 / 36
- Build Tools：36.1.0
- Java 源码兼容级别：17
- 本次构建 JDK：21

## Debug 验证

### 1. Debug 编译与打包

执行：

~~~powershell
.\gradlew.bat --offline --no-configuration-cache assembleDebug
~~~

结果：<code>BUILD SUCCESSFUL</code>。Java 编译、Android 资源处理、Dex、清单合并和 Debug APK 打包均成功。

### 2. JVM 单元测试

执行：

~~~powershell
.\gradlew.bat --offline --no-configuration-cache testDebugUnitTest
~~~

结果：

- 测试类：7
- 测试用例：45
- 失败：0
- 错误：0
- 跳过：0

其中 StealthTrackerTest 覆盖 7 项核心行为：

- 状态重置
- 四次安全拾取奖励
- 高威胁断开连携
- 危险进入事件单次消费
- 峰值警戒保持
- delta / threat 输入钳制
- 连携超时

### 3. 中文与 UTF-8

扫描范围为项目自有文本区域，包括 <code>app/src</code>、<code>docs</code>、根目录构建配置和说明文件；构建缓存、APK、签名文件及第三方二进制不参与文本扫描。

检查项目：

- 严格 UTF-8 解码：通过
- Unicode 替换字符 U+FFFD：0
- 常见错误转码特征：0
- 非源码文档中的异常连续问号：0
- UTF-8 BOM：0
- Java 编译编码：UTF-8

### 4. 代码结构与渲染检查

- 侦测与连携逻辑抽离为不依赖 Android 运行时的 StealthTracker。
- 十展厅场景绘制集中到 EnvironmentRenderer，HUD 集中到 HudRenderer。
- 五类道具符号集中到 IconRenderer，复用高频绘制对象。
- 玩家、守卫、工作人员和机器人模型增加可辨识细节与状态反馈。
- Guard、SecurityCamera 和 Player 的 delta、警戒与移动状态均增加边界处理。
- 设置持久化覆盖音效、震动、控制、大字体和减少动态等六项配置。

## Android Lint 限制

尝试执行：

~~~powershell
.\gradlew.bat --no-configuration-cache lintDebug
~~~

AGP 9.2 需要下载 Android Lint 32.2.0 的大型依赖，包括：

- <code>com.android.tools.lint:lint-checks:32.2.0</code>
- <code>com.android.tools.external.com-intellij:kotlin-compiler:32.2.0</code>

Google Maven 多次出现 <code>Read timed out</code>。即使把读取超时提高到 15 分钟，相关 JAR 仍未完成下载，因此 Lint 分析阶段没有启动。该限制不代表 Lint 已发现源码问题；项目构建配置也未永久关闭 Lint。

## Release 发布校验

### 1. Release 构建

Release 构建已成功完成 R8、资源压缩、Gradle 签名和 APK 打包。由于上述网络问题，本次命令临时排除以下 Vital Lint 任务：

- <code>lintVitalAnalyzeRelease</code>
- <code>lintVitalReportRelease</code>
- <code>lintVitalRelease</code>

该排除仅作用于本次构建命令，没有写入项目配置。

### 2. 发布文件

- APK：<code>Non-project/MuseumHeist2D-v3.0.0-release.apk</code>
- 文件大小：108,604 字节
- SHA-256：<code>84915dcb373340c34e7a759410006ad61ce9fc6bed732ec36cf8ac5844ebcc62</code>
- 摘要文件：<code>Non-project/MuseumHeist2D-v3.0.0-release.sha256.txt</code>
- 摘要文件内容与实际 APK 哈希一致：是

### 3. APK 清单

通过 <code>aapt dump badging</code> 验证：

- package：<code>com.museumheist</code>
- versionCode：30
- versionName：3.0.0
- minSdk：23
- targetSdk：36
- application label：博物馆潜行盗宝
- launchable activity：<code>com.museumheist.MainActivity</code>

### 4. APK 签名

通过 <code>apksigner verify --verbose --print-certs</code> 验证：

- APK verifies：成功
- v1 签名：true
- v2 签名：true
- v3 / v3.1 / v4：false
- signer count：1
- 签名算法：RSA 4096
- 证书 SHA-256：<code>fc25846aaa9f6a003ec7174b1e08bf0da136fc1004b5dfbb5b12d636e43ba0bc</code>

v2 已覆盖 minSdk 23 所需的整包完整性验证。apksigner 对 AGP 元数据给出 v1 元数据兼容性警告，但不影响 v2 签名验证结果。

### 5. 升级连续性

旧版 <code>2.3.0</code> 与新版 <code>3.0.0</code> 的证书 SHA-256 完全一致；旧版 versionCode 为 25，新版为 30。验证结果 <code>SIGNER_MATCH=True</code>，具备从旧版覆盖升级的签名条件。

### 6. 设备验证

<code>adb devices -l</code> 未检测到已连接设备，因此未执行真机安装、启动、触控、音频和震动验证。发布前如有设备，建议补做：

1. 从 2.3.0 覆盖安装 3.0.0。
2. 冷启动并检查十展厅中文、HUD、大字体和减少动态设置。
3. 验证守卫、摄像头、机器人警戒，潜行连携及通关结算。
4. 验证音效、震动、暂停恢复和横竖屏/后台切换行为。

## 验证结论

在无连接设备且 Lint 依赖下载受阻的条件下，3.0.0 已完成可执行的本地验证：45/45 JVM 测试通过，Debug 与 Release 构建成功，项目自有文本通过 UTF-8/乱码检查，正式 APK 的清单、哈希、v1/v2 签名及新旧签名连续性均已确认。
