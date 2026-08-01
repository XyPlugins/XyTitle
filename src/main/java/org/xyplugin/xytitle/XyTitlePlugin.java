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
        coreBridge.registerPlaceholders(new TitlePlaceholderProvider(titleService));
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
        startTimedTask();
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
