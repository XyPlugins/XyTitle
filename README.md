# XyTitle

XyTitle 是 XY 系列称号插件，由旧版 `pl-Title` 重构而来。插件负责称号仓库、称号展示、限时称号、成长称号和称号属性计算，属性写入统一交给 XyCore。

## 依赖

必须安装并启用：

- Java 8+
- Spigot/Paper 1.12.2
- XyCore 0.3.3+

XyTitle 的 `plugin.yml` 使用 `depend: [XyCore]`，没有 XyCore 时插件不会启动。XyTitle 不直接依赖 AttributePlus，也不会自己写 AP 属性源。

## 指令

主入口：

```text
/xytitle
```

| 指令 | 说明 | 权限 |
| --- | --- | --- |
| `/xytitle open` | 打开称号菜单 | `xytitle.use` |
| `/xytitle equip <称号ID>` | 佩戴称号 | `xytitle.use` |
| `/xytitle unequip` | 取消佩戴 | `xytitle.use` |
| `/xytitle attributes` | 查看当前称号属性 | `xytitle.use` |
| `/xytitle list` | 查看配置称号 | `xytitle.use` |
| `/xytitle give <玩家> <称号ID> [时长]` | 直接给予玩家称号 | `xytitle.admin` |
| `/xytitle giveitem <玩家> <称号ID>` | 给予称号物品，玩家右键领取 | `xytitle.admin` |
| `/xytitle take <玩家> <称号ID>` | 移除玩家称号 | `xytitle.admin` |
| `/xytitle clear <玩家>` | 清空玩家称号 | `xytitle.admin` |
| `/xytitle reload` | 重载配置并刷新在线玩家属性 | `xytitle.reload` |

别名：`/xyt`、`/title`。

## 配置称号

普通称号配置在 `config.yml`：

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

是否能映射到 PlaceholderAPI 取决于 XyCore 的 PlaceholderAPI 桥接状态。

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
build/libs/XyTitle-1.0.jar
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
