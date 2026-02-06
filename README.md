# PaperAntiCheat - Minecraft Paper 服务器反作弊插件
[![Build PaperAntiCheat](https://github.com/httye/PaperAntiCheat/actions/workflows/build.yml/badge.svg?event=check_run)](https://github.com/httye/PaperAntiCheat/actions/workflows/build.yml)

一个基于AI和行为分析的Minecraft Paper服务器反作弊插件，提供全面的作弊检测功能。

## 功能特性

### 🚀 移动检测
- **飞行检测**: 检测玩家非法飞行行为
- **速度检测**: 检测超速移动（包括加速药水滥用）
- **传送检测**: 检测瞬移和位置跳跃
- **蜘蛛人检测**: 检测墙上行走等异常行为

### ⚔️ 战斗检测  
- **杀戮光环(KillAura)**: 检测自动攻击和超高CPS
- **攻击距离(Reach)**: 检测超出正常范围的攻击
- **临界点(Criticals)**: 检测强制临界攻击
- **连击检测**: 检测不合理的攻击频率

### 🔍 客户端验证
- **客户端指纹**: 识别已知作弊客户端
- **Mod检测**: 检测常见的作弊Mod
- **版本验证**: 验证客户端版本合法性

### 📊 数据管理
- **SQLite数据库**: 存储违规记录和玩家数据
- **Web控制面板**: 实时监控和管理界面
- **详细日志**: 完整的违规记录和证据

### ⚙️ 配置管理
- **模块化设计**: 可单独启用/禁用各检测模块
- **阈值调整**: 灵活的检测灵敏度配置
- **权限系统**: 基于权限的命令访问控制

## 安装指南

### 1. 构建插件
```bash
# 克隆仓库后，在项目根目录执行
./gradlew shadowJar
# 或在Windows上
gradlew.bat shadowJar
```

构建完成后，插件JAR文件位于 `build/libs/PaperAntiCheat-1.0.0.jar`

### 2. 安装到服务器
1. 将生成的JAR文件复制到Paper服务器的 `plugins` 文件夹
2. 启动服务器以生成配置文件
3. 停止服务器并根据需要编辑 `plugins/PaperAntiCheat/config.yml`
4. 重新启动服务器

### 3. 使用测试脚本（可选）
项目包含自动化测试脚本，可以快速验证基本功能：
```bash
python test-anticheat.py
```

## 配置说明

### 主要配置文件
- `config.yml`: 主要检测配置
- `test-config.yml`: 测试专用配置

### 配置示例
```yaml
# 移动检测配置
movement:
  enabled: true
  flying:
    enabled: true
    max-flight-time: 500
    vertical-threshold: 0.08
  speed:
    enabled: true
    max-speed: 6.0
    check-interval: 10

# 战斗检测配置
combat:
  enabled: true
  killaura:
    enabled: true
    max-cps: 12
    min-attack-cooldown: 100
  reach:
    enabled: true
    max-reach: 3.15
    tolerance: 0.05
```

## 命令列表

| 命令 | 权限 | 描述 |
|------|------|------|
| `/anticheat reload` | anticheat.admin | 重载配置文件 |
| `/anticheat toggle` | anticheat.admin | 启用/禁用反作弊 |
| `/anticheat info` | anticheat.admin | 显示插件信息 |
| `/anticheat violations <player>` | anticheat.admin | 查看玩家违规记录 |

## Web控制面板

插件内置Web界面，提供实时监控功能：
- 违规记录查看
- 玩家行为分析
- 系统状态监控

**注意**: Web服务器默认禁用，需要在配置文件中启用。

## 测试方法

### 自动化测试
运行Python测试脚本进行基本功能验证：
```bash
python test-anticheat.py
```

### 手动测试
1. 启动测试服务器：双击 `test-server.bat`
2. 连接到 `localhost:25565`
3. 给自己OP权限：`/op <你的用户名>`
4. 测试各种作弊行为观察检测效果

## 技术架构

- **核心框架**: Paper API + Bukkit
- **数据库**: SQLite (轻量级，无需额外配置)
- **Web框架**: 内置HTTP服务器
- **检测算法**: 基于行为分析和统计模型
- **构建工具**: Gradle

## GitHub 自动编译

本项目配置了GitHub Actions工作流，支持自动编译和测试：

### 自动编译触发条件
- 推送代码到 `main` 或 `master` 分支
- 提交Pull Request到主分支

### 编译环境
- **操作系统**: Ubuntu Linux
- **Java版本**: JDK 17
- **构建工具**: Gradle

### 自动化流程
1. 自动检出代码
2. 设置JDK 17环境
3. 执行Gradle构建 (`./gradlew shadowJar`)
4. 上传编译后的JAR文件作为构件
5. 运行自动化测试脚本（如果存在）

### 获取编译产物
编译完成后，您可以在GitHub Actions页面的"Artifacts"部分下载编译好的插件JAR文件。

### 本地编译命令
如果您想在本地编译，使用以下命令：
```bash
# Linux/Mac
./gradlew shadowJar

# Windows  
gradlew.bat shadowJar
```

## 性能优化

- 异步数据处理，避免主线程阻塞
- 智能缓存机制，减少重复计算
- 可配置的检测频率，平衡性能与准确性
- 内存优化，适合长期运行

## 贡献指南

欢迎提交Issue和Pull Request！请确保：
1. 遵循现有代码风格
2. 添加适当的单元测试
3. 更新相关文档
4. 保持向后兼容性

## 许可证

MIT License - 免费用于个人和商业用途。

## 支持

如遇到问题，请检查：
1. 服务器日志中的错误信息
2. 配置文件语法是否正确
3. Paper服务器版本兼容性（推荐1.20.4）

---

**注意**: 这是一个演示版本的反作弊系统。在生产环境中使用前，请进行充分测试并根据实际需求调整配置参数。
