# XyTitle 更新说明

## 1.0.1 - 2026-08-02

- 玩家聊天提示前缀统一读取 `XyCoreApi#getMessagePrefix()`，即 XyCore `config.yml -> messages.prefix`。
- 命令反馈、称号领取提示、成长称号提示和过期提醒统一走同一玩家消息前缀入口。
- 启动时检查XyCore 0.3.11+的前缀API，避免旧Core版本运行期报错。
- 保持控制台日志使用XyTitle自身插件名；`display.chat-prefix` 仍只控制玩家聊天称号展示，不作为插件消息前缀。

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
