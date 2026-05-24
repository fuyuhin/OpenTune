# OpenTune

<div align="center">
  <img src="https://github.com/fuyuhin/OpenTune/blob/master/fastlane/metadata/android/en-US/images/featureGraphic.png" alt="OpenTune Banner" width="100%"/>
  
  ### 基于 Material Design 3 的 Android YouTube Music 高级客户端
  
  [![最新版本](https://img.shields.io/github/v/release/fuyuhin/OpenTune?style=flat-square&logo=github&color=0D1117&labelColor=161B22)](https://github.com/fuyuhin/OpenTune/releases)
  [![许可证](https://img.shields.io/github/license/fuyuhin/OpenTune?style=flat-square&logo=gnu&color=2B3137&labelColor=161B22)](https://github.com/fuyuhin/OpenTune/blob/main/LICENSE)
  [![Android](https://img.shields.io/badge/平台-Android%206.0+-3DDC84.svg?style=flat-square&logo=android&logoColor=white&labelColor=161B22)](https://www.android.com)
  [![Stars](https://img.shields.io/github/stars/fuyuhin/OpenTune?style=flat-square&logo=github&color=yellow&labelColor=161B22)](https://github.com/fuyuhin/OpenTune/stargazers)
  [![Forks](https://img.shields.io/github/forks/fuyuhin/OpenTune?style=flat-square&logo=github&color=blue&labelColor=161B22)](https://github.com/fuyuhin/OpenTune/network/members)
</div>

---

## 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [主要功能](#主要功能)
- [文档](#文档)
- [安装方式](#安装方式)
- [从源码编译](#从源码编译)
- [贡献指南](#贡献指南)
- [致谢](#致谢)
- [许可证](#许可证)

---

## 项目简介

**OpenTune** 是一款开源的 Android YouTube Music 客户端。它采用 Material Design 3 现代界面，提供超越官方应用的高级功能，让你自由探索、播放和管理音乐内容，无广告干扰。

### 核心优势

- **无广告体验**：畅享音乐，零广告打扰
- **性能优化**：播放流畅，界面响应迅速
- **隐私保护**：不收集用户数据，不跟踪行为
- **界面可定制**：按照个人喜好调整音乐体验
- **离线播放**：下载音乐，随时随地畅听

> **说明**：OpenTune 是独立开源项目，与 YouTube 和 Google 无任何关联。

---

## 技术栈

<div align="center">
  
| 前端 | 工具 |
|:----:|:----:|
| ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white) | ![Android Studio](https://img.shields.io/badge/Android%20Studio-3DDC84?style=for-the-badge&logo=androidstudio&logoColor=white) |
| ![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white) | ![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white) |
| ![Material Design 3](https://img.shields.io/badge/Material%20Design%203-757575?style=for-the-badge&logo=materialdesign&logoColor=white) | ![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white) |

</div>

---

## 主要功能

### 核心功能
<table>
<tr>
<th width="30%">功能</th>
<th width="70%">说明</th>
</tr>
<tr>
<td><strong>🎵 无广告播放</strong></td>
<td>畅享音乐，零广告打扰</td>
</tr>
<tr>
<td><strong>🔄 后台播放</strong></td>
<td>使用其他应用的同时继续播放音乐</td>
</tr>
<tr>
<td><strong>🔍 高级搜索</strong></td>
<td>快速查找歌曲、视频、专辑和播放列表</td>
</tr>
<tr>
<td><strong>👤 账户同步</strong></td>
<td>登录账户以同步偏好设置和收藏内容</td>
</tr>
<tr>
<td><strong>📚 音乐库管理</strong></td>
<td>完整组织和管理你的音乐收藏</td>
</tr>
<tr>
<td><strong>📱 离线模式</strong></td>
<td>下载内容，无需网络也能收听</td>
</tr>
<tr>
<td><strong>🖼️ 桌面小组件</strong></td>
<td>主屏幕显示当前播放歌曲，支持播放/暂停/上下曲控制</td>
</tr>
</table>

### 音频增强
<table>
<tr>
<th width="30%">功能</th>
<th width="70%">说明</th>
</tr>
<tr>
<td><strong>🎤 同步歌词</strong></td>
<td>实时显示与音乐完美同步的歌词</td>
</tr>
<tr>
<td><strong>⚡ 智能跳过静音</strong></td>
<td>自动跳过无声片段</td>
</tr>
<tr>
<td><strong>🔊 音量均衡</strong></td>
<td>均衡不同曲目间的音量差异</td>
</tr>
<tr>
<td><strong>🎛️ 速度与音调调节</strong></td>
<td>自由调整播放速度和音调</td>
</tr>
</table>

### 个性化与集成
<table>
<tr>
<th width="30%">功能</th>
<th width="70%">说明</th>
</tr>
<tr>
<td><strong>🎨 动态主题</strong></td>
<td>界面色彩自动跟随专辑封面变化</td>
</tr>
<tr>
<td><strong>🌐 多语言支持</strong></td>
<td>支持多种语言，服务全球用户</td>
</tr>
<tr>
<td><strong>🚗 Android Auto 支持</strong></td>
<td>与车载娱乐系统无缝集成</td>
</tr>
<tr>
<td><strong>🎯 Material Design 3</strong></td>
<td>遵循 Google 最新设计规范</td>
</tr>
<tr>
<td><strong>🖼️ 导出专辑封面</strong></td>
<td>保存高分辨率专辑封面图片</td>
</tr>
</table>

---

## 文档

详细的配置说明、高级功能介绍和使用指南，请查阅官方文档：

<div align="center">
  
[![文档](https://img.shields.io/badge/文档-GitBook-4285F4?style=for-the-badge&logo=gitbook&logoColor=white)](https://opentune.gitbook.io/)

</div>

---

## 安装方式

### 系统要求

| 组件 | 最低要求 |
|:-----|:---------|
| 操作系统 | Android 6.0（棉花糖）或更高版本 |
| 存储空间 | 至少 10 MB 可用空间 |
| 网络 | 在线播放需要网络连接 |
| 内存 | 建议 2 GB 以上 RAM |

### 安装方法

#### 方式一：GitHub Releases（推荐）

1. 前往本项目的 [Releases 页面](https://github.com/fuyuhin/OpenTune/releases)
2. 下载最新版本的 APK 文件
3. 在设备安全设置中开启"允许安装未知来源应用"
4. 打开下载的 APK 文件完成安装

---

## 从源码编译

### 前置条件

<table>
<tr>
<th>工具</th>
<th>推荐版本</th>
<th>用途</th>
</tr>
<tr>
<td>Gradle</td>
<td>8.0 或更高</td>
<td>构建自动化</td>
</tr>
<tr>
<td>Kotlin</td>
<td>2.0 或更高</td>
<td>编程语言</td>
</tr>
<tr>
<td>Android Studio</td>
<td>2024.1 或更高</td>
<td>IDE 及开发环境</td>
</tr>
<tr>
<td>JDK</td>
<td>21 或更高</td>
<td>Java 运行环境</td>
</tr>
<tr>
<td>Android SDK</td>
<td>API 级别 36（Android 16）</td>
<td>Android 开发工具</td>
</tr>
</table>

### 环境配置

```bash
# 克隆仓库
git clone https://github.com/fuyuhin/OpenTune.git

# 进入项目目录
cd OpenTune

# 更新子模块（如有）
git submodule update --init --recursive
```

### 编译方法

#### 使用 Android Studio

1. 打开 Android Studio
2. 选择"打开现有 Android Studio 项目"
3. 浏览并选择 OpenTune 目录
4. 等待项目同步和索引完成
5. 选择"构建 → 构建 Bundle/APK → 构建 APK"

#### 命令行编译

```bash
# 编译 Release 版本（ARM64）
./gradlew assembleArm64Release

# 编译 Debug 版本（ARM64）
./gradlew assembleArm64Debug

# 完整构建含测试
./gradlew build

# 运行单元测试
./gradlew test

# 清理构建
./gradlew clean
```

> **说明**：编译完成的 APK 位于 `app/build/outputs/apk/` 目录下。

---

## 贡献指南

### 行为准则

所有参与者须遵守[行为准则](https://github.com/fuyuhin/OpenTune/blob/master/CODE_OF_CONDUCT.md)，共同维护包容、尊重、建设性的社区环境。

### 开发流程

1. **查看 Issues**：检查[现有 Issues](https://github.com/fuyuhin/OpenTune/issues) 或新建一个描述问题或功能需求
2. **Fork 仓库**：创建个人 Fork
3. **创建分支**：为新功能创建分支（`git checkout -b feature/new-feature`）
4. **实现功能**：遵循项目代码规范进行开发
5. **测试**：确保代码通过所有测试（`./gradlew test`）
6. **提交**：使用描述性提交信息（`git commit -m 'feat: 添加新功能'`）
7. **推送**：将更改推送到你的 Fork（`git push origin feature/new-feature`）
8. **Pull Request**：提交 PR 并详细说明更改内容

---

## 致谢

特别感谢以下贡献者和协作者：

- **ArchiveTune** — 初始代码及灵感来源
- **Vivi Music** — 设计灵感
- **Fabito02** — 从一开始的全力支持
- **社区翻译者** — 让 OpenTune 走向全球
- **Beta 测试者** — 帮助提升稳定性和可用性

---

## 许可证

**Copyright © 2025 Arturo Cervantes**

本程序为自由软件：你可以在自由软件基金会发布的 GNU 通用公共许可证条款下重新分发和/或修改它，版本 3 或（根据你的选择）任何更高版本。

本程序的发布是希望它有用，但**不附带任何保证**；甚至不包含对适销性或特定目的适用性的隐含保证。详情请参阅 [GNU 通用公共许可证](https://github.com/fuyuhin/OpenTune/blob/main/LICENSE)。

<div align="center">
  
[![GPL v3](https://img.shields.io/badge/许可证-GPLv3-blue.svg?style=for-the-badge&logo=gnu&logoColor=white)](https://www.gnu.org/licenses/gpl-3.0)

</div>

---

<div align="center">
  <p><strong>© 2023-2025 OpenTune 开源项目</strong></p>
  
  [![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/fuyuhin/OpenTune)
</div>
