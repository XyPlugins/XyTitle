package org.xyplugin.xytitle.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.xyplugin.xytitle.config.AttributeAmount;
import org.xyplugin.xytitle.config.TitleDefinition;
import org.xyplugin.xytitle.config.TitleRegistry;
import org.xyplugin.xytitle.data.OwnedTitle;
import org.xyplugin.xytitle.data.PlayerTitleData;
import org.xyplugin.xytitle.service.TitleService;
import org.xyplugin.xytitle.util.Durations;
import org.xyplugin.xytitle.util.Text;

public final class TitleGui implements Listener {

    private static final String MAIN_TITLE = "§6XyTitle";
    private static final String STORAGE_TITLE = "§6称号仓库";
    private static final String ATTRIBUTE_TITLE = "§6称号属性";

    private final JavaPlugin plugin;
    private final TitleRegistry registry;
    private final TitleService service;
    private final Map<UUID, Integer> pages = new HashMap<UUID, Integer>();

    public TitleGui(JavaPlugin plugin, TitleRegistry registry, TitleService service) {
        this.plugin = plugin;
        this.registry = registry;
        this.service = service;
    }

    public void openMain(Player player) {
        int size = normalizeSize(plugin.getConfig().getInt("gui.main-menu-size", 9), 9);
        Inventory inventory = Bukkit.createInventory(player, size, MAIN_TITLE);
        inventory.setItem(2, button(Material.CHEST, "&a称号仓库", "&7查看并佩戴已拥有称号"));
        inventory.setItem(6, button(Material.BOOK, "&e属性总览", "&7查看当前生效的称号属性"));
        fill(inventory);
        player.openInventory(inventory);
    }

    public void openStorage(Player player, int page) {
        PlayerTitleData data = service.data(player);
        List<OwnedTitle> owned = new ArrayList<OwnedTitle>(data.ownedTitles());
        Collections.sort(owned, new Comparator<OwnedTitle>() {
            @Override
            public int compare(OwnedTitle left, OwnedTitle right) {
                return left.titleId().compareTo(right.titleId());
            }
        });

        int size = normalizeSize(plugin.getConfig().getInt("gui.title-storage-size", 54), 54);
        int perPage = Math.max(9, Math.min(size - 9, plugin.getConfig().getInt("gui.storage-items-per-page", 45)));
        int maxPage = Math.max(0, (int) Math.ceil(owned.size() / (double) perPage) - 1);
        page = Math.max(0, Math.min(page, maxPage));
        pages.put(player.getUniqueId(), page);

        Inventory inventory = Bukkit.createInventory(player, size, STORAGE_TITLE + " - " + (page + 1));
        int start = page * perPage;
        int end = Math.min(owned.size(), start + perPage);
        for (int index = start; index < end; index++) {
            OwnedTitle ownedTitle = owned.get(index);
            TitleDefinition title = registry.get(ownedTitle.titleId());
            if (title == null) {
                continue;
            }
            List<String> extra = new ArrayList<String>();
            extra.add("&8ID: " + title.id());
            extra.add("&7有效期: &f" + Durations.formatRemaining(ownedTitle.expiresAtMillis()));
            extra.add(title.id().equals(data.equippedTitle()) ? "&a当前佩戴中" : "&e点击佩戴");
            inventory.setItem(index - start, title.createItem(extra));
        }
        addNavigation(inventory, page, maxPage);
        player.openInventory(inventory);
    }

    public void openAttributes(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 27, ATTRIBUTE_TITLE);
        Map<String, AttributeAmount> attributes = service.calculateAttributes(player);
        int slot = 10;
        for (AttributeAmount amount : attributes.values()) {
            if (slot >= 17) {
                break;
            }
            inventory.setItem(slot++, button(Material.PAPER, "&a" + amount.name(), "&7" + amount.toAttributeLine()));
        }
        if (attributes.isEmpty()) {
            inventory.setItem(13, button(Material.BARRIER, "&c暂无属性", "&7获得称号后会在这里显示。"));
        }
        inventory.setItem(22, button(Material.ARROW, "&e返回", "&7返回主菜单"));
        fill(inventory);
        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        String title = event.getView().getTitle();
        if (!title.startsWith(MAIN_TITLE) && !title.startsWith(STORAGE_TITLE) && !title.startsWith(ATTRIBUTE_TITLE)) {
            return;
        }
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return;
        }
        String name = item.getItemMeta().getDisplayName();
        if (title.startsWith(MAIN_TITLE)) {
            if (name.contains("称号仓库")) {
                openStorage(player, 0);
            } else if (name.contains("属性总览")) {
                openAttributes(player);
            }
            return;
        }
        if (title.startsWith(ATTRIBUTE_TITLE)) {
            if (name.contains("返回")) {
                openMain(player);
            }
            return;
        }
        if (title.startsWith(STORAGE_TITLE)) {
            handleStorageClick(player, item, name);
        }
    }

    private void handleStorageClick(Player player, ItemStack item, String name) {
        int page = pages.containsKey(player.getUniqueId()) ? pages.get(player.getUniqueId()) : 0;
        if (name.contains("上一页")) {
            openStorage(player, page - 1);
            return;
        }
        if (name.contains("下一页")) {
            openStorage(player, page + 1);
            return;
        }
        if (name.contains("返回")) {
            openMain(player);
            return;
        }
        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) {
            return;
        }
        String id = readId(item.getItemMeta().getLore());
        if (id == null) {
            return;
        }
        if (service.equip(player, id)) {
            player.sendMessage(Text.color(plugin.getConfig().getString("messages.prefix", "")
                    + plugin.getConfig().getString("messages.equipped", "").replace("{title}", service.displayName(id))));
            openStorage(player, page);
        }
    }

    private String readId(List<String> lore) {
        for (String line : lore) {
            String plain = Text.plain(line);
            if (plain.startsWith("ID: ")) {
                return plain.substring(4);
            }
        }
        return null;
    }

    private void addNavigation(Inventory inventory, int page, int maxPage) {
        int size = inventory.getSize();
        if (page > 0) {
            inventory.setItem(size - 9, button(Material.ARROW, "&a上一页", "&7查看上一页"));
        }
        inventory.setItem(size - 5, button(Material.BARRIER, "&c返回", "&7返回主菜单"));
        if (page < maxPage) {
            inventory.setItem(size - 1, button(Material.ARROW, "&a下一页", "&7查看下一页"));
        }
        inventory.setItem(size - 6, button(Material.PAPER, "&6页码", "&7当前: &f" + (page + 1) + "/" + (maxPage + 1)));
        fill(inventory);
    }

    private ItemStack button(Material material, String name, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(name));
            meta.setLore(Collections.singletonList(Text.color(lore)));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void fill(Inventory inventory) {
        ItemStack filler = button(Material.STAINED_GLASS_PANE, " ", " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    private int normalizeSize(int value, int fallback) {
        if (value < 9 || value > 54 || value % 9 != 0) {
            return fallback;
        }
        return value;
    }
}
