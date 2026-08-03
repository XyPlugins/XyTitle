# XyTitle 更新说明

## 1.0.5 - 2026-08-03

- 称号仓库新增左右键操作：左键佩戴选中的称号，右键取消当前佩戴。
- 仓库称号 Lore 新增当前佩戴状态和左右键操作提示。
- 佩戴或取消后立即刷新仓库、玩家展示、称号属性及 `%xytitle_title%` 当前值。

## 1.0.4 - 2026-08-02

- 新增 XyTitle 自身 PlaceholderAPI 直连变量注册，提供 `%xytitle_*%` 系列变量。
- `%xytitle_title%` 可直接被支持 PlaceholderAPI 的聊天插件读取，显示玩家当前佩戴称号。
- 保留原有 XyCore PlaceholderRegistry 注册逻辑，XyCore 内部变量链不变。
- 文档补充聊天格式示例和 `/papi parse me %xytitle_title%` 测试方式。

## 1.0.3 - 2026-08-02

- 新增 `/xych` 缩写命令，作为 `/xytitle` 的推荐短入口。
- 新增 `/xych get <称号ID或显示名>`，玩家可在拥有 `xytitle.get` 权限时直接获取指定称号；默认 op，方便测试和受控发放。
- 普通称号配置从 `config.yml -> titles:` 拆分到独立 `titles.yml`，成长称号继续使用 `growth_titles.yml`。
- 首次加载新版本时，如果旧 `config.yml` 中存在 `titles:` 且 `titles.yml` 不存在，会自动迁移旧称号配置到 `titles.yml`。
- 新增 `display.player-display-name-prefix-enabled`，默认关闭，避免称号出现在 `/gamemode` 等系统提示中的玩家名前面。
- 默认配置继续保留中文注释，并在帮助信息中展示 `/xych` 用法。

## 1.0.2 - 2026-08-02

- 按服主最终确认调整前缀语义：获得/领取/佩戴/卸下/成长升级/过期等玩家玩法提示走 XyCore `messages.prefix`。
- `/xytitle help/list/attributes/reload/give/take/clear` 的用法、权限、玩家不在线、称号不存在和管理员反馈保留 XyTitle 自身前缀。
- 新增 `sendPlayer` 与 `sendLocal`，并修复此前半插入造成的 `prefixed(CommandSender, String)` 坏结构。
- 保持 `display.chat-prefix` 仅用于玩家聊天称号展示，不参与插件提示前缀切换。

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
