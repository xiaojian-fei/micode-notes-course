# 验证状态

更新时间：2026年10月16日。以下为已有真实记录的摘要，原始材料由项目负责人本地保存。创建公开仓库没有重新执行整套UI测试。

## 仓库准备时的检查

2026年10月16日生成Gradle 8.7 Wrapper，并使用本机Gradle 8.7执行 `assembleDebug assembleDebugAndroidTest`，主APK和测试APK构建成功。本次没有安装到模拟器或重做运行验收。原有应用Java和资源文件保持不变。

| 批次 | 对应源码 | 实际记录 |
| --- | --- | --- |
| 未修复基线 | 标注后基线 | B1异常及ANR为组件级复现；S1自然搜索入口受阻。B2时间错误、B4创建全空白文件夹来自实际UI与数据库 |
| 4+3分项验收及综合UI | 8ea1751 | 功能分项验收；综合UI回归54个流程检查点；真实版本4数据库副本由同一Helper升级并核对旧字段 |
| 搜索高亮收尾 | 1e836d0 | 20次自然UI链路检查；修复CharSequence关键字读取和结果页传递 |
| 搜索规则一致性 | dfbcd5b | 12种输入分别走建议和直接提交，共24次打开及跨度核对；两入口无匹配检查；累计设备组件11个方法通过 |

## 提醒的实际触发

自然UI保存的提醒在2026年10月14日05:00 GMT（北京时间13:00）真实到期，`alert_date=1791954000000`。日志记录提醒页面自动启动、显示，正文正确，Got it可关闭。未通过手动广播冒充到期。

## 已知验证范围

- 主要运行设备为Android 9 / API 28模拟器。其他API及真机未完成全面回归。
- 原版B4改名路径未验证；维护版改名另有回归。不能将其改写成原版改名缺陷已复现。
- 高亮折叠ASCII大小写，不宣称覆盖完整Unicode大小写对应关系。
- 提醒播放服务状态有记录；原测试模拟器无音频输出，人耳可闻声音未试听。Take a look按钮未纳入原到期验收。
- 11个组件测试方法、54个流程检查点和24次搜索检查属于不同统计口径，不合并为一次最终版本全量测试。
- 历史失败尝试保留。文件名带highlight不足以证明早期截图存在正确高亮，应核对其版本及后续验收记录。

## 本地证据索引

路径均相对于项目负责人本地的“小米便签”材料目录；这些文件位于本Git仓库之外，需私下共享。

- `docs/maintenance/baseline/`：B1、B2、B4基线。
- `docs/maintenance/search_entry_investigation/`：S1补充调查。
- `docs/maintenance/implementation/F1/`、`F2/`、`F3/`：新增功能验收。
- `docs/maintenance/implementation/Final/`：综合UI及提醒到期证据。
- `docs/maintenance/implementation/Highlight/`、`SearchConsistency/`：最终搜索修复验收。
- `_android_setup/chapter5/`、`chapter6/`：报告编辑备份与排版核对记录。
