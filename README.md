# KazdelPatch（Minecraft 1.7.10 / GTNH）

`KazdelPatch` 是一个面向 GTNH 服务端场景的运行时补丁模组，目标是以最小侵入方式修复已知崩溃点和可利用漏洞。

作者：`Criogaid`

## 当前功能

- `EntityTracker` 并发访问缓解：将关键遍历路径改为快照遍历，降低并发修改导致的崩溃风险。
- 异步线程访问 TileEntity 保护：对非主线程访问世界方块实体做防护。
- 容器后端失效保护：检测容器后端无效时主动关闭，避免异常状态传播。
- 发射器堆叠净化：清理 `stackSize <= 0` 或无效物品堆叠，阻断 0/-1 触发的刷物链路。
- `/stop` 扩展：支持附加关服原因，并在异常情况下兜底回退到 vanilla stop。
- `/hat` 命令：将手持物与头盔槽快速互换（最简静默实现）。

## 环境与兼容

- Minecraft: `1.7.10`
- Forge: `10.13.4.1614`
- 主要目标：`GT New Horizons` 服务端

## 构建

Linux/macOS:

```bash
./gradlew build
```

Windows:

```powershell
.\gradlew.bat build
```

产物默认位于：`build/libs/kazdelpatch-<version>.jar`

## 部署

1. 关闭服务器。
2. 将构建产物放入服务器 `mods/` 目录。
3. 启动服务器并确认日志无 mixin 注入失败。
4. 按需验证功能（例如发射器 0/负数堆叠场景、`/stop`、`/hat`）。

## 公开仓库建议

- 通过 `.gitignore` 排除本地工具目录、构建目录和 IDE 文件。
- 通过 `.gitattributes` 统一行尾规则，减少跨平台差异。
- 发布前执行一次 `./gradlew.bat build`，并确认 `logs/`、`run/`、`build/` 无需提交文件。
