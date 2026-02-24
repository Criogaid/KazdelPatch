# EntityTracker 运行时修复补丁（1.7.10 / GTNH）

这个项目用于将 `EntityTracker` 并发修改问题做成独立可装载 mod（与调试 mod 分离）。

## 功能

- 在 `EntityTracker` 构造后，将 `trackedEntities` 替换为 `ConcurrentHashMap` 支撑的并发 Set。
- 覆盖 `func_72788_a` 和 `func_85172_a` 的遍历路径，改为快照遍历，降低 `ConcurrentModificationException` 风险。
- 保持仅服务端必装（客户端可不装）。

## 构建

```bash
./gradlew build
```

Windows:

```powershell
.\gradlew.bat build
```

产物路径：`build/libs/trackerpatch-<version>.jar`

## 服务器使用（修复版）

1. 停服。
2. 将 jar 放入服务器 `mods/`。
3. 开服复现原先易崩场景（登录/传送/高频实体更新）。
4. 观察是否不再出现 `EntityTracker.func_72788_a` / `func_85172_a` 的并发修改崩溃。

## 注意

- 这是“缓解型运行时补丁”，不是上游源码永久修复。
- 建议与原 `tracker-debug-mod` 二选一装载，避免排查阶段日志噪声过大。
