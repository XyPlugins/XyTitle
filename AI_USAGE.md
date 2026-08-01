# AI Usage Record

## 2026-08-02 / 1.0.2

- 根据服主最终确认，AI 将 XyTitle 从“玩家命令反馈统一 XyCore”改为语义分流。
- 玩家玩法结果使用 XyCore 前缀：获得称号、领取称号物品、佩戴、卸下、成长升级、过期提醒。
- 管理/帮助/报错使用 XyTitle 本地前缀：help、list、attributes、reload、give/take/clear 的用法和管理员反馈。
- 修复前一轮自动脚本造成的 `localPrefix` 与 `prefixed(CommandSender, String)` 方法结构错误，并通过 `compileJava` 验证。

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

## 2026-08-02 / 1.0.1

- 根据服主确认的Xy系列聊天前缀统一规则，AI辅助将XyTitle插件提示改为读取 `XyCoreApi#getMessagePrefix()`。
- XyTitle是强依赖XyCore的插件，因此启动阶段新增XyCore 0.3.11+ API检查；旧Core会明确拒绝启用。
- 本次只统一插件提示消息；`display.chat-prefix` 继续作为玩家聊天称号展示格式，不与XyCore消息前缀混用。
- 控制台日志继续保留XyTitle插件名，便于排查称号配置、重载和属性刷新问题。
