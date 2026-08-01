# XyTitle Changelog

## 1.0.2 - 2026-08-02

- 按语义分流消息前缀：玩家玩法结果走 XyCore，管理/帮助/报错保留 XyTitle。
- 增加 `playerPrefix/localPrefix`、`prefixedPlayer/prefixedLocal`、`sendPlayer/sendLocal`，保留旧 `send` 兼容玩家玩法调用。
- 命令类逐项改为本地或玩家发送入口，避免玩家执行 help 或参数错误时显示为系统提示。
- 本次不改变称号数据、属性源、GUI结构、Placeholder 或聊天称号格式。

## 1.0.1 - 2026-08-02

- 玩家聊天提示前缀改为读取 `XyCoreApi#getMessagePrefix()`，与XyCore主配置保持一致。
- 命令类、GUI提示、称号服务提示和监听器领取提示都改为经由 `XyTitlePlugin` 或 `XyCoreBridge` 的统一前缀入口。
- 启动接入Core时检查 `getMessagePrefix` API，明确要求XyCore 0.3.11+。
- 保持控制台日志与玩家聊天称号展示格式不受统一消息前缀影响。

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
