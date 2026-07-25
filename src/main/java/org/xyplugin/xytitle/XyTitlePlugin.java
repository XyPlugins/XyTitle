package org.xyplugin.xytitle;

import org.bukkit.command.PluginCommand;
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

public final class XyTitlePlugin extends JavaPlugin {

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
}
