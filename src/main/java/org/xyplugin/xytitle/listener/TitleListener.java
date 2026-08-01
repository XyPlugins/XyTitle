package org.xyplugin.xytitle.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.xyplugin.xytitle.XyTitlePlugin;
import org.xyplugin.xytitle.config.TitleDefinition;
import org.xyplugin.xytitle.config.TitleRegistry;
import org.xyplugin.xytitle.gui.TitleGui;
import org.xyplugin.xytitle.service.TitleService;
import org.xyplugin.xytitle.util.Text;

public final class TitleListener implements Listener {

    private final XyTitlePlugin plugin;
    private final TitleRegistry registry;
    private final TitleService service;
    private final TitleGui gui;

    public TitleListener(XyTitlePlugin plugin, TitleRegistry registry, TitleService service, TitleGui gui) {
        this.plugin = plugin;
        this.registry = registry;
        this.service = service;
        this.gui = gui;
    }

    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                service.refresh(event.getPlayer());
            }
        }, 20L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        service.unload(event.getPlayer());
    }

    @EventHandler
    public void onLevelChange(PlayerLevelChangeEvent event) {
        service.updateGrowth(event.getPlayer(), service.data(event.getPlayer()), true);
        service.refresh(event.getPlayer());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!plugin.getConfig().getBoolean("display.chat-prefix-enabled", true)) {
            return;
        }
        String title = service.displayName(service.data(event.getPlayer()).equippedTitle());
        if (title.isEmpty()) {
            return;
        }
        String prefix = plugin.getConfig().getString("display.chat-prefix", "&f[&6{title}&f] ")
                .replace("{title}", title)
                .replace("{player}", event.getPlayer().getName());
        event.setFormat(Text.color(prefix) + event.getFormat());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }
        String displayName = meta.getDisplayName();
        for (TitleDefinition title : registry.all()) {
            if (!Text.color(title.displayName()).equals(displayName)) {
                continue;
            }
            event.setCancelled(true);
            if (service.grant(event.getPlayer(), title.id(), null)) {
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    event.getPlayer().setItemInHand(null);
                }
                event.getPlayer().sendMessage(Text.color(message("messages.received").replace("{title}", service.displayName(title.id()))));
                gui.openStorage(event.getPlayer(), 0);
            }
            return;
        }
    }

    private String message(String path) {
        return plugin.messagePrefix() + plugin.getConfig().getString(path, "");
    }
}
