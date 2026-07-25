# XyTitle Changelog

## 1.0 - 2026-07-25

- 初始化 XyTitle Gradle 插件工程。
- 主类和包名改为 `org.xyplugin.xytitle`。
- 指令入口改为 `/xytitle`，并提供 `/xyt`、`/title` 别名。
- 强制依赖 XyCore，启动时必须连接 XyCore API。
- 新增 XyCore AttributeService 属性源写入，属性源格式为 `xytitle:<uuid>`。
- 新增 XyCore PlaceholderRegistry 变量注册，命名空间为 `xytitle`。
- 新增称号仓库 GUI、属性总览 GUI 和称号物品右键领取。
- 新增普通称号、限时称号、成长称号配置加载。
- 新增玩家称号本地 YAML 存储。
- 新增 `owned-all` 与 `equipped-only` 两种属性计算模式。
- 移除旧版硬编码 GM 玩家名逻辑。
- 移除旧版直接 AttributePlus 桥接，避免与 XyCore 重复写属性。
- 新增 README 使用说明和 AI 使用记录。
