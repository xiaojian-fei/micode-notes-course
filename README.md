# 小米便签软件工程课程实践

第一次操作请阅读 [详细使用指南](docs/使用指南.md)，包含便签保存、菜单入口、置顶、筛选、统计和提醒的完整步骤。

菜单默认隐藏。点击模拟器画面后按 **Ctrl＋M**，打开首页或普通便签编辑页的功能菜单。备用命令为 `adb shell input keyevent 82`。实际菜单名称为英文，指南采用英文名称并附中文释义。多设备命令及常见问题见使用指南第2节。

基于 [MiCode/Notes](https://github.com/MiCode/Notes) 的课程维护工程，包含原有代码标注、4项缺陷修复、3项新增功能及设备组件测试。原项目说明保留在 [README](README)，版权与 Apache License 2.0 许可文本保留在 [NOTICE](NOTICE)。

## 当前功能

| 编号 | 内容 |
| --- | --- |
| S1 | 修正搜索建议 Provider 授权名，恢复建议入口，并完善直接提交搜索路径 |
| B1 | 以字面文本匹配搜索关键字，处理非法正则、零长度匹配、关键字传递、ASCII高亮及两个入口的LIKE转义一致性 |
| B2 | 修复12/24小时提醒换算，回归跨日、跨月和闰年边界 |
| B4 | 拒绝全空白文件夹名，统一前后空白规范化及重复名校验 |
| F1 | 便签置顶及取消置顶、状态持久化、数据库4→5升级 |
| F2 | 按文件夹、颜色、修改日期组合筛选，支持清除及返回刷新 |
| F3 | 当前未保存正文的字符、行数及清单完成情况统计 |

代码沿用原有 `src/`、`res/` 目录结构。`data` 提供数据库与Provider，`model` 维护编辑状态，`ui` 负责界面，`tool` 包含筛选、统计和输入校验逻辑。

## 环境

- JDK 17；已验证版本17.0.12。
- Gradle 8.7，已附Gradle Wrapper；Android Gradle Plugin 8.4.0。
- Android SDK Platform 34、Build Tools 34.0.0、Platform Tools。
- 应用minSdk 16、targetSdk 22；已有运行记录主要来自Android 9 / API 28的x86_64模拟器。

当前仓库地址为 <https://github.com/xiaojian-fei/micode-notes-course>。

## 克隆和构建

```powershell
git clone https://github.com/xiaojian-fei/micode-notes-course.git
cd micode-notes-course
```

将 `JAVA_HOME` 设置为本机JDK 17路径。使用以下方式之一定位SDK：

1. 设置 `ANDROID_HOME` 或 `ANDROID_SDK_ROOT`；
2. 在工程根目录创建不提交到Git的 `local.properties`，内容如 `sdk.dir=C:/Android/Sdk`，替换为实际目录。

启动自己的模拟器。下列 `adb` 命令需要先将**自己 SDK 目录下的 platform-tools** 加入 PATH，可在 PowerShell 按本机 SDK 路径配置。SDK 也可以通过 Android Studio 的 SDK Manager 查看。

```powershell
# 按上一节先设置 ANDROID_HOME 为本机 SDK 路径。
$env:Path = (Join-Path $env:ANDROID_HOME 'platform-tools') + [IO.Path]::PathSeparator + $env:Path
adb version
```

仅使用 local.properties 或 ANDROID_SDK_ROOT 时，可以按[使用指南第1节](docs/使用指南.md#1-先打开应用)定位 ADB。`local.properties` 供 Gradle 查找 SDK，终端还需要单独找到 adb。以下示例适用于只有一台在线设备的情况，多设备时在每条 adb 命令加 `-s 实际设备号`。

```powershell
.\gradlew.bat assembleDebug assembleDebugAndroidTest
adb devices
adb install -r build/outputs/apk/debug/MiCodeNotes-debug.apk
adb shell am start -n net.micode.notes/.ui.NotesListActivity
```

首次运行Wrapper会下载Gradle，构建还需要下载Android插件和依赖。Linux/macOS使用 `bash ./gradlew`；该平台的本项目运行情况尚未实测。保留Gradle对实验性路径设置的警告，它不等于构建失败。

**已有数据的设备请注意签名：** 新电脑默认生成的调试密钥可能不同于原设备已安装版本；出现签名冲突时，请向项目负责人取得合法的本地签名配置，或使用另一个干净的测试模拟器。保留原设备及旧数据用于迁移验证。签名密钥不上传到公开仓库。

## 设备组件测试

构建并安装主APK后，安装测试APK：

```powershell
adb install -r build/outputs/apk/androidTest/debug/MiCodeNotes-debug-androidTest.apk
adb shell am instrument -w -e class net.micode.notes.ContentStatsTests,net.micode.notes.FilterTests,net.micode.notes.FolderNameTests,net.micode.notes.MaintenanceTests,net.micode.notes.SearchQueryTests net.micode.notes.test/android.test.InstrumentationTestRunner
```

以上选择不依赖私有旧库副本的10个测试方法。`MigrationTests` 另需一份真实版本4数据库副本，路径固定为 `/sdcard/Download/course-migration-v4.db`；没有该文件时该测试会失败。旧数据库和现场日志通过小组私下交接，不随公开仓库发布。取得合法副本后单独运行：

```powershell
adb push course-migration-v4.db /sdcard/Download/course-migration-v4.db
adb shell am instrument -w -e class net.micode.notes.MigrationTests net.micode.notes.test/android.test.InstrumentationTestRunner
```

组件测试不能代替自然UI验收。建议点击与直接提交搜索是两条独立路径；核对提醒数据库时间时使用设备时区。部分测试期望来自已验证的API 28系统镜像，在其他版本上须核实实际行为。

## 版本和交接

- `b706753`：第3章标注后源码。
- `8ea1751`：4项缺陷和3项新功能完成快照。
- `1e836d0`、`dfbcd5b`：后续搜索关键字传递和规则一致性修复。
- main保留上述真实提交历史；后续提交用于公开仓库构建与协作说明。

已执行测试范围及限制见 [验证状态](docs/VERIFICATION_STATUS.md)。两位队友可直接使用本仓库的源码、脚本和用例；具体工作、提交方式和本地材料清单见 [队友接手清单](docs/TEAM_HANDOFF.md)。

课程报告、带个人信息的封面、原始运行日志、数据库和演示视频在本地材料包中另行管理。本仓库不代表这些收尾材料已经全部提交。
