package org.xyplugin.xytitle;

import org.bukkit.command.PluginCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.xyplugin.xytitle.command.XyTitleCommand;
import org.xyplugin.xytitle.config.TitleRegistry;
import org.xyplugin.xytitle.data.YamlTitleRepository;
import org.xyplugin.xytitle.gui.TitleGui;
import org.xyplugin.xytitle.integration.XyCoreBridge;
import org.xyplugin.xytitle.listener.TitleListener;
import org.xyplugin.xytitle.placeholder.TitlePapiExpansion;
import org.xyplugin.xytitle.placeholder.TitlePlaceholderProvider;
import org.xyplugin.xytitle.service.TitleService;
import org.xyplugin.xytitle.util.Text;

public final class XyTitlePlugin extends JavaPlugin {
    private static final String DEFAULT_LOCAL_PREFIX = "&6[XyTitle] &r";

    private TitleRegistry registry;
    private YamlTitleRepository repository;
    private XyCoreBridge coreBridge;
    private TitleService titleService;
    private TitleGui titleGui;
    private BukkitTask timedTask;
    private TitlePlaceholderProvider placeholderProvider;
    private Object papiExpansion;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("growth_titles.yml", false);

        coreBridge = new XyCoreBridge(this);
        if (!coreBridge.connect()) {
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registry = new TitleRegistry(this);
        registry.reload();
        repository = new YamlTitleRepository(this);
        titleService = new TitleService(this, registry, repository, coreBridge);
        titleGui = new TitleGui(this, registry, titleService);

        XyTitleCommand command = new XyTitleCommand(this);
        PluginCommand pluginCommand = getCommand("xytitle");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }

        getServer().getPluginManager().registerEvents(new TitleListener(this, registry, titleService, titleGui), this);
        getServer().getPluginManager().registerEvents(titleGui, this);
        placeholderProvider = new TitlePlaceholderProvider(titleService);
        coreBridge.registerPlaceholders(placeholderProvider);
        refreshPapiExpansion();
        startTimedTask();

        getLogger().info("XyTitle " + getDescription().getVersion() + " enabled. XyCore attributes: "
                + coreBridge.attributeProviderName());
    }

    @Override
    public void onDisable() {
        if (timedTask != null) {
            timedTask.cancel();
            timedTask = null;
        }
        unregisterPapiExpansion();
        if (coreBridge != null) {
            coreBridge.unregisterPlaceholders("xytitle");
        }
        if (titleService != null) {
            for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                titleService.unload(player);
            }
            titleService.saveAll();
        }
    }

    public void reloadRuntime() {
        reloadConfig();
        registry.reload();
        for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
            titleService.refresh(player);
        }
        refreshPapiExpansion();
        startTimedTask();
    }

    private void refreshPapiExpansion() {
        unregisterPapiExpansion();
        if (placeholderProvider == null) {
            return;
        }
        org.bukkit.plugin.Plugin papi = getServer().getPluginManager().getPlugin("PlaceholderAPI");
        if (papi == null || !papi.isEnabled()) {
            getLogger().info("未检测到PlaceholderAPI，%xytitle_*% 保留为XyCore内部变量。");
            return;
        }
        try {
            TitlePapiExpansion expansion = new TitlePapiExpansion(this, placeholderProvider);
            if (expansion.register()) {
                papiExpansion = expansion;
                getLogger().info("已直接注册 PlaceholderAPI 变量: %xytitle_*%。");
            } else {
                getLogger().info("%xytitle_*% 变量已由其他桥接注册，保留现有注册。");
            }
        } catch (Throwable failure) {
            getLogger().warning("直接注册 PlaceholderAPI 变量失败，将仅使用XyCore内部变量: " + failure.getMessage());
            papiExpansion = null;
        }
    }

    private void unregisterPapiExpansion() {
        if (papiExpansion == null) {
            return;
        }
        try {
            papiExpansion.getClass().getMethod("unregister").invoke(papiExpansion);
        } catch (Throwable ignored) {
        }
        papiExpansion = null;
    }

    private void startTimedTask() {
        if (timedTask != null) {
            timedTask.cancel();
        }
        long period = Math.max(20L, getConfig().getLong("settings.timed-check-ticks", 1200L));
        timedTask = getServer().getScheduler().runTaskTimer(this, new Runnable() {
            @Override
            public void run() {
                for (org.bukkit.entity.Player player : getServer().getOnlinePlayers()) {
                    titleService.refresh(player);
                }
            }
        }, period, period);
    }

    public TitleRegistry registry() {
        return registry;
    }

    public TitleService titles() {
        return titleService;
    }

    public TitleGui gui() {
        return titleGui;
    }

    /** 玩家玩法提示：有 XyCore 时使用 XyCore 统一前缀。 */
    public String messagePrefix() {
        return playerPrefix();
    }

    public String playerPrefix() {
        return coreBridge == null ? localPrefix() : coreBridge.getMessagePrefix();
    }

    public String localPrefix() {
        return getConfig().getString("messages.prefix", DEFAULT_LOCAL_PREFIX);
    }

    public String prefixed(String text) {
        return prefixedPlayer(text);
    }

    public String prefixedPlayer(String text) {
        return Text.color(playerPrefix() + (text == null ? "" : text));
    }

    public String prefixedLocal(String text) {
        return Text.color(localPrefix() + (text == null ? "" : text));
    }

    public String message(String path) {
        return getConfig().getString(path, "");
    }

    public void sendPlayer(org.bukkit.entity.Player player, String text) {
        if (player != null) {
            player.sendMessage(prefixedPlayer(text));
        }
    }

    public void sendLocal(CommandSender sender, String text) {
        if (sender != null) {
            sender.sendMessage(prefixedLocal(text));
        }
    }

    /** 默认用于玩家玩法提示，旧调用保持兼容。 */
    public void send(CommandSender sender, String text) {
        if (sender != null) {
            sender.sendMessage(prefixedPlayer(text));
        }
    }
}
