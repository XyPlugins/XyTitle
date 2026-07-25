package org.xyplugin.xytitle.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.xyplugin.xytitle.util.Text;

public final class TitleDefinition {

    private final String id;
    private final String displayName;
    private final Material material;
    private final List<String> lore;
    private final List<AttributeAmount> attributes;
    private final String duration;
    private final boolean growth;

    public TitleDefinition(String id, String displayName, Material material, List<String> lore,
                           List<AttributeAmount> attributes, String duration, boolean growth) {
        this.id = id;
        this.displayName = displayName;
        this.material = material;
        this.lore = new ArrayList<String>(lore);
        this.attributes = new ArrayList<AttributeAmount>(attributes);
        this.duration = duration;
        this.growth = growth;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public List<String> lore() {
        return Collections.unmodifiableList(lore);
    }

    public List<AttributeAmount> attributes() {
        return Collections.unmodifiableList(attributes);
    }

    public String duration() {
        return duration;
    }

    public boolean growth() {
        return growth;
    }

    public ItemStack createItem(List<String> extraLore) {
        ItemStack item = new ItemStack(material == null ? Material.PAPER : material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Text.color(displayName));
            List<String> lines = Text.colorList(lore);
            if (extraLore != null && !extraLore.isEmpty()) {
                lines.add("");
                lines.addAll(Text.colorList(extraLore));
            }
            meta.setLore(lines);
            item.setItemMeta(meta);
        }
        return item;
    }
}
