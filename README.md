# XyTitle

XyTitle 是 XY 系列称号插件，由旧版 `pl-Title` 重构而来。插件负责称号仓库、称号展示、限时称号、成长称号和称号属性计算，属性写入统一交给 XyCore。

## 依赖

必须安装并启用：

- Java 8+
- Spigot/Paper 1.12.2
- XyCore 0.3.12+
- PlaceholderAPI 可选；如果聊天插件要直接使用 `%xytitle_title%` 这类变量，建议安装并启用。

XyTitle 的 `plugin.yml` 使用 `depend: [XyCore]`，没有 XyCore 时插件不会启动。XyTitle 不直接依赖 AttributePlus，也不会自己写 AP 属性源。

## 玩家消息前缀

XyTitle 将玩家玩法结果和管理提示分开处理。领取称号、获得称号、佩戴称号、卸下称号、成长称号升级和称号过期提醒读取 `plugins/XyCore/config.yml -> messages.prefix`；`/xytitle` 或 `/xych` 的 help/list/attributes/reload/give/take/clear、权限不足、参数错误、玩家不在线、称号不存在和管理员反馈保留 XyTitle 自身前缀。

注意：`display.chat-prefix` 是玩家聊天中的称号展示格式，例如 `[称号] 玩家名`，它不是插件提示前缀，不会被XyCore消息前缀替换。

如果不希望称号出现在 `/gamemode`、死亡提示、部分系统提示或其他插件消息中的玩家名前面，请保持：

```yaml
display:
  player-display-name-prefix-enabled: false
```

推荐让聊天称号交给聊天格式里的 `%xytitle_title%` 显示，不再把称号写进 Bukkit 全局玩家显示名。

## 指令

主入口：

```text
/xytitle
/xych
```

| 指令 | 说明 | 权限 |
| --- | --- | --- |
| `/xytitle open` | 打开称号菜单 | `xytitle.use` |
| `/xych get <称号ID或显示名>` | 给自己直接获取称号，适合测试或受控奖励 | `xytitle.get` |
| `/xytitle equip <称号ID>` | 佩戴称号 | `xytitle.use` |
| `/xytitle unequip` | 取消佩戴 | `xytitle.use` |
| `/xytitle attributes` | 查看当前称号属性 | `xytitle.use` |
| `/xytitle list` | 查看配置称号 | `xytitle.use` |
| `/xytitle give <玩家> <称号ID> [时长]` | 直接给予玩家称号 | `xytitle.admin` |
| `/xytitle giveitem <玩家> <称号ID>` | 给予称号物品，玩家右键领取 | `xytitle.admin` |
| `/xytitle take <玩家> <称号ID>` | 移除玩家称号 | `xytitle.admin` |
| `/xytitle clear <玩家>` | 清空玩家称号 | `xytitle.admin` |
| `/xytitle reload` | 重载配置并刷新在线玩家属性 | `xytitle.reload` |

别名：`/xych`、`/xyt`、`/title`。推荐日常使用 `/xych`。

在称号仓库中，左键点击已拥有的称号即可佩戴；右键点击任意已拥有称号即可取消当前佩戴。操作完成后仓库会立即刷新当前状态，`%xytitle_title%` 也会同步更新。

## 配置称号

普通称号配置在 `titles.yml`：

如果服务器已经有旧版 `config.yml -> titles:`，并且 `titles.yml` 还不存在，新版本首次启动时会自动复制旧称号配置到 `titles.yml`。

```yaml
titles:
  newbie:
    display-name: "&a新手称号"
    item-material: "PAPER"
    lore:
      - "&7新手专属称号"
      - "&6生命上限: +200"
    attributes:
      生命上限: 200
      暴击几率: "5%"
```

限时称号可以添加 `duration`：

```yaml
titles:
  pioneer_7d:
    display-name: "&a开荒者"
    duration: "7d"
    attributes:
      生命加成: "75%"
```

支持时长单位：

- `d` 天
- `h` 小时
- `m` 分钟
- `s` 秒

## 属性模式

```yaml
settings:
  attribute-mode: owned-all
```

可选值：

- `owned-all`：玩家已拥有的所有称号属性累加，兼容旧版 `pl-Title`。
- `equipped-only`：只计算当前佩戴称号的属性。

属性刷新时，XyTitle 会通过 XyCore 调用：

```text
XyCore.get().getAttributes().removeSource(player, "xytitle:<uuid>")
XyCore.get().getAttributes().addSource(player, "xytitle:<uuid>", lines)
```

因此不会直接访问 AttributePlus，也不会和其他 XY 插件重复写 AP。

## 成长称号

成长称号配置在 `growth_titles.yml`：

```yaml
growth-titles:
  newbie_growth:
    display-name: "&a新手成长称号"
    base-attributes:
      生命上限: 500
    levels:
      1:
        required-level: 10
        attributes:
          攻击力: 100
```

玩家等级变化时会自动检测可成长等级，并刷新称号属性。

## 变量

XyTitle 通过 XyCore 的 PlaceholderRegistry 注册命名空间 `xytitle`。

从 1.0.4 开始，如果服务器安装了 PlaceholderAPI，XyTitle 也会直接注册 `%xytitle_*%`，聊天插件只要支持 PlaceholderAPI 就能引用，不需要聊天插件专门适配 XyTitle。

常用变量：

```text
%xytitle_title%
%xytitle_current_title%
%xytitle_title_id%
%xytitle_has_title%
%xytitle_owned_count%
%xytitle_attributes%
%xytitle_has_<称号ID>%
%xytitle_attr_<属性名>%
```

聊天格式示例：

```yaml
format: '&7[%multiverse_world_alias%&7][&aLv:%AkariLevel_Default_Level%&7][%xytitle_title%][player]&f: '
```

测试变量可以在游戏内执行：

```text
/papi parse me %xytitle_title%
```

注意：`%xytitle_title%` 显示的是当前佩戴称号；只拥有但没有佩戴时会显示为空。

如果 PlaceholderAPI 未安装，`%xytitle_title%` 不能被普通聊天插件解析，但 XyTitle 仍会保留 XyCore 内部变量注册。

## 数据

当前版本使用本地 YAML 保存玩家称号数据：

```text
plugins/XyTitle/playerdata/<uuid>.yml
```

保存内容包括：

- 已拥有称号
- 限时称号过期时间
- 当前佩戴称号
- 成长称号等级

后续如果要切换到 XyCore SQL，可以替换仓储层，不影响称号业务和属性桥接。

## 构建

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
.\gradlew.bat clean build --no-daemon
```

产物：

```text
build/libs/XyTitle-1.0.5.jar
```

## 相比旧版优化

- 包名改为 `org.xyplugin.xytitle`。
- 指令入口改为 `/xytitle`。
- 强依赖 XyCore，属性写入不再直连 AttributePlus。
- 移除硬编码 GM 玩家名分支。
- 属性配置从 lore 文案解析改为结构化 `attributes`。
- 保留旧版“已拥有称号属性累加”逻辑，并可配置为只算佩戴称号。
- 权限节点统一为 `xytitle.*`。
- Placeholder 统一交给 XyCore 注册。
- 本地数据结构更清晰，便于后续迁移到 Core SQL。
