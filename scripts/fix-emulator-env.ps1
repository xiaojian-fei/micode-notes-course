# 模拟器环境修复脚本（未维护基线）
#
# 处理两个在实测中遇到的问题：
#   1. 设备上残留维护版数据库（version 5）导致基线（version 4）无法保存便签
#   2. 模拟器默认只有英文输入法，无法输入中文
#
# 说明文档：docs/环境修复与复现说明.md
#
# 用法示例：
#   .\scripts\fix-emulator-env.ps1
#       仅启用中文拼音输入法（安全，不改动任何数据）
#
#   .\scripts\fix-emulator-env.ps1 -ClearAppData
#       额外清除应用数据（会删除模拟器内该应用的全部便签，需自行确认）
#
#   .\scripts\fix-emulator-env.ps1 -Serial emulator-5556
#       多设备在线时指定设备号
#
#   .\scripts\fix-emulator-env.ps1 -PinyinPackage com.osfans.trime/.ime.core.TrimeInputMethodService
#       镜像内没有谷歌拼音时，改用其它已安装的输入法

[CmdletBinding()]
param(
    # 设备号。留空则自动选择唯一一台状态为 device 的在线设备。
    [string]$Serial = '',

    # 清除应用数据以重建 version 4 数据库。会删除模拟器内该应用的全部便签。
    [switch]$ClearAppData,

    # 应用包名，一般无需修改。
    [string]$PackageName = 'net.micode.notes',

    # 输入法组件名。默认使用 API 28 google_apis 镜像自带的谷歌拼音。
    [string]$PinyinPackage = 'com.google.android.inputmethod.pinyin/.PinyinIME',

    # 跳过输入法配置。
    [switch]$SkipPinyin
)

$ErrorActionPreference = 'Stop'

function Resolve-Adb {
    $cmd = Get-Command adb -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }

    $sdk = $env:ANDROID_HOME
    if (-not $sdk) { $sdk = $env:ANDROID_SDK_ROOT }
    if (-not $sdk) {
        throw '未找到 adb：请把本机 SDK 的 platform-tools 加入 PATH，或设置 ANDROID_HOME / ANDROID_SDK_ROOT。'
    }
    $path = Join-Path $sdk 'platform-tools/adb.exe'
    if (-not (Test-Path -LiteralPath $path)) {
        throw "在 '$sdk' 下找不到 platform-tools/adb.exe，请检查 SDK 路径与 Platform-Tools 是否安装。"
    }
    return $path
}

function Read-Lines {
    param([string]$Exe, [string[]]$Arguments)
    $out = & $Exe @Arguments 2>&1
    return @($out | ForEach-Object { "$_" })
}

$adb = Resolve-Adb
Write-Host "adb: $adb"

if (-not $Serial) {
    $lines = Read-Lines -Exe $adb -Arguments @('devices')
    $online = @()
    foreach ($line in $lines) {
        if ($line -match '^(\S+)\s+device\s*$') { $online += $Matches[1] }
    }
    if ($online.Count -eq 0) {
        throw '没有状态为 device 的在线设备。请先启动模拟器并等待进入系统，再执行 adb devices 确认。'
    }
    if ($online.Count -gt 1) {
        throw "检测到多台设备（$($online -join ', ')），请用 -Serial 指定，例如 -Serial emulator-5554。"
    }
    $Serial = $online[0]
}
Write-Host "device: $Serial"

# --- 当前状态速览 -------------------------------------------------------
$sdkVersion = (Read-Lines -Exe $adb -Arguments @('-s', $Serial, 'shell', 'getprop', 'ro.build.version.sdk')) -join ''
$release = (Read-Lines -Exe $adb -Arguments @('-s', $Serial, 'shell', 'getprop', 'ro.build.version.release')) -join ''
Write-Host "Android: $release (API $($sdkVersion.Trim()))"

$installed = (Read-Lines -Exe $adb -Arguments @('-s', $Serial, 'shell', 'pm', 'path', $PackageName)) -join ''
if ($installed -notmatch 'package:') {
    Write-Warning "设备上未安装 $PackageName，请先安装自己编译的 APK 再执行本脚本。"
} else {
    Write-Host "已安装: $($installed.Trim())"
}

# --- 1. 数据库版本冲突 --------------------------------------------------
# 只在显式传入 -ClearAppData 时执行，避免误删测试数据。
if ($ClearAppData) {
    Write-Warning "即将执行 pm clear $PackageName：该应用在模拟器内的全部便签数据将被删除。"
    $answer = Read-Host '确认清除请输入 YES'
    if ($answer -ne 'YES') {
        Write-Host '已取消清除应用数据。'
    } else {
        $result = (Read-Lines -Exe $adb -Arguments @('-s', $Serial, 'shell', 'pm', 'clear', $PackageName)) -join ''
        Write-Host "pm clear: $($result.Trim())"
        Write-Host '应用数据已清除，基线下次启动会重新创建 version 4 数据库。'
    }
} else {
    Write-Host ''
    Write-Host '提示：若新建便签后无法保存，且 logcat 出现'
    Write-Host "  SQLiteException: Can't downgrade database from version 5 to 4"
    Write-Host '说明设备上残留了维护版（version 5）的数据库。'
    Write-Host '可加 -ClearAppData 参数重建数据库（会删除模拟器内便签），或改用全新的模拟器。'
}

# --- 2. 中文输入法 ------------------------------------------------------
if (-not $SkipPinyin) {
    Write-Host ''
    Write-Host "启用输入法: $PinyinPackage"
    $imeList = (Read-Lines -Exe $adb -Arguments @('-s', $Serial, 'shell', 'ime', 'list', '-s')) -join "`n"
    if ($imeList -notmatch [regex]::Escape($PinyinPackage)) {
        Write-Warning "当前系统中未找到该输入法组件：$PinyinPackage"
        Write-Host '可用输入法：'
        Write-Host $imeList
        Write-Host '可安装开源输入法 Trime（发布页提供 x86_64 独立包），再用 -PinyinPackage 指定其服务名。'
    } else {
        $enableOut = (Read-Lines -Exe $adb -Arguments @('-s', $Serial, 'shell', 'ime', 'enable', $PinyinPackage)) -join ''
        Write-Host "ime enable: $($enableOut.Trim())"

        $setOut = (Read-Lines -Exe $adb -Arguments @('-s', $Serial, 'shell', 'ime', 'set', $PinyinPackage)) -join ''
        Write-Host "ime set: $($setOut.Trim())"

        $current = (Read-Lines -Exe $adb -Arguments @('-s', $Serial, 'shell', 'settings', 'get', 'secure', 'default_input_method')) -join ''
        Write-Host "当前默认输入法: $($current.Trim())"

        Write-Host '首次在编辑框输入时会弹出通讯录权限对话框，按需允许或拒绝即可。'
        Write-Host '验证：输入 bizijian，候选栏应出现中文候选词。'
    }
}

Write-Host ''
Write-Host '完成。后续操作步骤见 docs/使用指南.md 第3节（新建、编辑与保存）。'