# OB MiFi Manager

一款 Android 随身 WiFi 管理工具，替代路由器默认的网页管理界面，提供更美观、更便捷的管理体验。

## 支持设备

- LBF1A 系列 LTE 随身 WiFi（及其他使用相同固件的设备）
- 管理地址：`192.168.1.1`

## 功能特性

### 仪表盘
- 实时显示信号强度、电量、网速（每秒刷新）
- 套餐流量使用进度条（点击查看详情）
- 蜂窝网络一键开关
- 累计/本次流量统计
- 运行时间实时计时

### 信号监控
- RSRP / SINR / RSRQ 信号指标仪表盘
- 小区信息：MCC/MNC/TAC/PCI/eNB ID
- 频段、带宽、EARFCN、CQI、TX Power

### WiFi 设置
- 查看/修改 WiFi 名称和密码
- 安全模式切换（WPA2/WPA3/Mixed）
- 高级设置：信道、带宽、最大客户端数、AP 隔离

### 设备管理
- 在线设备列表（实时刷新）
- 设备屏蔽 / 解除屏蔽
- 设备信息：型号、固件版本、IMEI、ICCID
- SIM 卡管理与切换（eSIM1/eSIM2/智能选网）
- 网络模式切换（2G/3G/4G/自动）
- DHCP 设置查看
- 修改管理密码 / 重启设备 / 恢复出厂

### 套餐查询
- 自动查询套餐剩余流量、到期时间
- 充值号用户自行输入，支持多用户

### 上网认证
- 短信验证码认证设备上网权限

## 登录

- 输入路由器管理密码登录
- 支持记住密码，下次自动登录
- 退出登录后不清除已保存的密码（勾选记住密码时）

## 技术栈

- **语言**：Kotlin
- **UI 框架**：Jetpack Compose + Material Design 3
- **架构**：MVVM + Hilt 依赖注入
- **网络**：Retrofit2 + OkHttp + HTTP Digest Auth
- **最低版本**：Android 8.0（API 26）

## 构建

项目使用 GitHub Actions 自动构建。推送代码或创建 Tag 即可触发：

```bash
# 创建 Release
git tag v1.0.0
git push origin v1.0.0
```

也可以本地构建：

```bash
./gradlew assembleDebug    # Debug 版本
./gradlew assembleRelease  # Release 版本
```

## 下载

前往 [Releases](https://github.com/Flymero/OB-MiFiManager/releases) 页面下载最新 APK。

## 权限说明

| 权限 | 用途 |
|------|------|
| `INTERNET` | 访问路由器 API 和套餐查询接口 |
| `ACCESS_NETWORK_STATE` | 检测网络连接状态 |
| `ACCESS_WIFI_STATE` | 检测 WiFi 连接状态 |

## 许可证

MIT License
