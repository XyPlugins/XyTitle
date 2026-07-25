# AI Usage Record

## 2026-07-25

- 阅读并分析 `pl-Title-1.0.jar` 的 `plugin.yml`、`config.yml`、`growth_titles.yml`。
- 使用 `javap` 检查旧插件核心类结构，包括指令、玩家数据、属性管理、限时称号、成长称号、GUI、Placeholder 和 DragonCore 槽位。
- 克隆并阅读 `XyCore`，确认其已提供 `AttributeService` 和 `PlaceholderRegistry`。
- 根据用户要求将插件重构为 `XyTitle`，强制依赖 XyCore。
- 移除旧插件中直接对接 AttributePlus 的逻辑，改为通过 XyCore 写入属性源。
- 移除旧插件硬编码 GM 玩家名功能。
- 将属性从 lore 解析优化为结构化配置。
- 保留本地 YAML 玩家称号数据，便于当前未启用 SQL 的服务器直接使用。
- 添加 README、更新记录和构建说明。
