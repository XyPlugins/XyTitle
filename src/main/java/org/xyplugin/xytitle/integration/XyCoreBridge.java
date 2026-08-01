package org.xyplugin.xytitle.integration;

import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.xyplugin.xycore.api.XyCore;
import org.xyplugin.xycore.api.XyCoreApi;
import org.xyplugin.xycore.api.attribute.AttributeService;
import org.xyplugin.xycore.api.placeholder.PlaceholderProvider;

public final class XyCoreBridge {

    private final JavaPlugin plugin;
    private XyCoreApi api;
    private boolean placeholderRegistered;

    public XyCoreBridge(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean connect() {
        Plugin core = Bukkit.getPluginManager().getPlugin("XyCore");
        if (core == null || !core.isEnabled()) {
            plugin.getLogger().severe("XyCore is required. Please install and enable XyCore before XyTitle.");
            return false;
        }
        try {
            api = XyCore.get();
            api.getClass().getMethod("getMessagePrefix");
            plugin.getLogger().info("Connected to XyCore " + api.getVersion() + ".");
            return true;
        } catch (NoSuchMethodException exception) {
            plugin.getLogger().severe("XyTitle requires XyCore 0.3.12 or newer for unified message prefix behavior.");
            api = null;
            return false;
        } catch (RuntimeException exception) {
            plugin.getLogger().severe("Could not access XyCore API: " + exception.getMessage());
            api = null;
            return false;
        }
    }

    public boolean applyAttributes(Player player, String source, List<String> lines) {
        AttributeService attributes = attributes();
        if (attributes == null || !attributes.isAvailable()) {
            return false;
        }
        attributes.removeSource(player, source);
        if (lines == null || lines.isEmpty()) {
            return true;
        }
        return attributes.addSource(player, source, lines);
    }

    public void clearAttributes(Player player, String source) {
        AttributeService attributes = attributes();
        if (attributes != null && attributes.isAvailable()) {
            attributes.removeSource(player, source);
        }
    }

    public boolean attributesAvailable() {
        AttributeService attributes = attributes();
        return attributes != null && attributes.isAvailable();
    }

    public String attributeProviderName() {
        AttributeService attributes = attributes();
        return attributes == null ? "unavailable" : attributes.getProviderName();
    }

    public String getMessagePrefix() {
        return api == null ? plugin.getConfig().getString("messages.prefix", "") : api.getMessagePrefix();
    }

    public void registerPlaceholders(PlaceholderProvider provider) {
        if (api == null || provider == null || placeholderRegistered) {
            return;
        }
        api.getPlaceholders().register(provider);
        placeholderRegistered = true;
    }

    public void unregisterPlaceholders(String namespace) {
        if (api == null || !placeholderRegistered) {
            return;
        }
        api.getPlaceholders().unregister(namespace);
        placeholderRegistered = false;
    }

    private AttributeService attributes() {
        return api == null ? null : api.getAttributes();
    }
}
