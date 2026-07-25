package org.xyplugin.xytitle.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.xyplugin.xytitle.XyTitlePlugin;
import org.xyplugin.xytitle.config.AttributeAmount;
import org.xyplugin.xytitle.config.TitleDefinition;
import org.xyplugin.xytitle.util.Text;

public final class XyTitleCommand implements CommandExecutor, TabCompleter {

    private final XyTitlePlugin plugin;

    public XyTitleCommand(XyTitlePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            help(sender);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        if ("open".equals(sub)) {
            return open(sender);
        }
        if ("equip".equals(sub)) {
            return equip(sender, args);
        }
        if ("unequip".equals(sub) || "clearwear".equals(sub)) {
            return unequip(sender);
        }
        if ("attributes".equals(sub) || "attr".equals(sub)) {
            return attributes(sender);
        }
        if ("list".equals(sub)) {
            return list(sender);
        }
        if ("give".equals(sub) || "grant".equals(sub)) {
            return give(sender, args);
        }
        if ("giveitem".equals(sub)) {
            return giveItem(sender, args);
        }
        if ("take".equals(sub) || "remove".equals(sub)) {
            return take(sender, args);
        }
        if ("clear".equals(sub)) {
            return clear(sender, args);
        }
        if ("reload".equals(sub)) {
            return reload(sender);
        }
        help(sender);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> base = new ArrayList<String>(Arrays.asList("open", "equip", "unequip", "attributes", "list", "help"));
            if (sender.hasPermission("xytitle.admin")) {
                base.addAll(Arrays.asList("give", "giveitem", "take", "clear"));
            }
            if (sender.hasPermission("xytitle.reload")) {
                base.add("reload");
            }
            return starts(base, args[0]);
        }
        if (args.length == 2 && Arrays.asList("equip").contains(args[0].toLowerCase(Locale.ROOT))) {
            return starts(plugin.registry().ids(), args[1]);
        }
        if (args.length == 2 && Arrays.asList("give", "grant", "giveitem", "take", "remove", "clear").contains(args[0].toLowerCase(Locale.ROOT))) {
            List<String> players = new ArrayList<String>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                players.add(player.getName());
            }
            return starts(players, args[1]);
        }
        if (args.length == 3 && Arrays.asList("give", "grant", "giveitem", "take", "remove").contains(args[0].toLowerCase(Locale.ROOT))) {
            return starts(plugin.registry().ids(), args[2]);
        }
        return new ArrayList<String>();
    }

    private boolean open(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }
        if (!allowed(sender, "xytitle.use")) {
            return true;
        }
        plugin.gui().openMain(player);
        return true;
    }

    private boolean equip(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }
        if (!allowed(sender, "xytitle.use")) {
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(color("&c用法: /xytitle equip <称号ID>"));
            return true;
        }
        String titleId = args[1];
        if (!plugin.registry().exists(titleId)) {
            missingTitle(sender, titleId);
            return true;
        }
        if (!plugin.titles().equip(player, titleId)) {
            sender.sendMessage(color(plugin.getConfig().getString("messages.not-owned", "")));
            return true;
        }
        sender.sendMessage(color(plugin.getConfig().getString("messages.equipped", "").replace("{title}", plugin.titles().displayName(titleId))));
        return true;
    }

    private boolean unequip(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }
        if (!allowed(sender, "xytitle.use")) {
            return true;
        }
        plugin.titles().unequip(player);
        sender.sendMessage(color(plugin.getConfig().getString("messages.unequipped", "")));
        return true;
    }

    private boolean attributes(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }
        if (!allowed(sender, "xytitle.use")) {
            return true;
        }
        Map<String, AttributeAmount> attributes = plugin.titles().calculateAttributes(player);
        sender.sendMessage(color("&6&m==========&6[ &eXyTitle 属性 &6]&m=========="));
        if (attributes.isEmpty()) {
            sender.sendMessage(color("&7暂无称号属性。"));
        } else {
            for (AttributeAmount amount : attributes.values()) {
                sender.sendMessage(color("&a" + amount.toAttributeLine()));
            }
        }
        return true;
    }

    private boolean list(CommandSender sender) {
        if (!allowed(sender, "xytitle.use")) {
            return true;
        }
        sender.sendMessage(color("&6&m==========&6[ &eXyTitle 称号 &6]&m=========="));
        for (TitleDefinition title : plugin.registry().all()) {
            sender.sendMessage(color("&a" + title.id() + " &7- " + title.displayName()));
        }
        return true;
    }

    private boolean give(CommandSender sender, String[] args) {
        if (!allowed(sender, "xytitle.admin")) {
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(color("&c用法: /xytitle give <玩家> <称号ID> [时长]"));
            return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            missingPlayer(sender, args[1]);
            return true;
        }
        String titleId = args[2];
        if (!plugin.registry().exists(titleId)) {
            missingTitle(sender, titleId);
            return true;
        }
        String duration = args.length >= 4 ? args[3] : null;
        plugin.titles().grant(target, titleId, duration);
        sender.sendMessage(color(plugin.getConfig().getString("messages.granted", "")
                .replace("{player}", target.getName()).replace("{title}", plugin.titles().displayName(titleId))));
        target.sendMessage(color(plugin.getConfig().getString("messages.received", "")
                .replace("{title}", plugin.titles().displayName(titleId))));
        return true;
    }

    private boolean giveItem(CommandSender sender, String[] args) {
        if (!allowed(sender, "xytitle.admin")) {
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(color("&c用法: /xytitle giveitem <玩家> <称号ID>"));
            return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            missingPlayer(sender, args[1]);
            return true;
        }
        TitleDefinition title = plugin.registry().get(args[2]);
        if (title == null) {
            missingTitle(sender, args[2]);
            return true;
        }
        ItemStack item = title.createItem(Arrays.asList("&e右键领取该称号", "&8ID: " + title.id()));
        target.getInventory().addItem(item);
        sender.sendMessage(color(plugin.getConfig().getString("messages.granted", "")
                .replace("{player}", target.getName()).replace("{title}", plugin.titles().displayName(title.id()))));
        return true;
    }

    private boolean take(CommandSender sender, String[] args) {
        if (!allowed(sender, "xytitle.admin")) {
            return true;
        }
        if (args.length < 3) {
            sender.sendMessage(color("&c用法: /xytitle take <玩家> <称号ID>"));
            return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            missingPlayer(sender, args[1]);
            return true;
        }
        if (!plugin.titles().revoke(target, args[2])) {
            sender.sendMessage(color("&c该玩家没有这个称号。"));
            return true;
        }
        sender.sendMessage(color(plugin.getConfig().getString("messages.taken", "")
                .replace("{player}", target.getName()).replace("{title}", plugin.titles().displayName(args[2]))));
        return true;
    }

    private boolean clear(CommandSender sender, String[] args) {
        if (!allowed(sender, "xytitle.admin")) {
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(color("&c用法: /xytitle clear <玩家>"));
            return true;
        }
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            missingPlayer(sender, args[1]);
            return true;
        }
        plugin.titles().clear(target);
        sender.sendMessage(color(plugin.getConfig().getString("messages.cleared", "").replace("{player}", target.getName())));
        return true;
    }

    private boolean reload(CommandSender sender) {
        if (!allowed(sender, "xytitle.reload")) {
            return true;
        }
        plugin.reloadRuntime();
        sender.sendMessage(color(plugin.getConfig().getString("messages.reloaded", "")));
        return true;
    }

    private void help(CommandSender sender) {
        for (String line : plugin.getConfig().getStringList("messages.help")) {
            sender.sendMessage(color(line));
        }
    }

    private Player requirePlayer(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(color(plugin.getConfig().getString("messages.players-only", "")));
            return null;
        }
        return (Player) sender;
    }

    private boolean allowed(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }
        sender.sendMessage(color(plugin.getConfig().getString("messages.no-permission", "")));
        return false;
    }

    private void missingTitle(CommandSender sender, String titleId) {
        sender.sendMessage(color(plugin.getConfig().getString("messages.title-not-found", "").replace("{title}", titleId)));
    }

    private void missingPlayer(CommandSender sender, String playerName) {
        sender.sendMessage(color(plugin.getConfig().getString("messages.player-not-found", "").replace("{player}", playerName)));
    }

    private String color(String text) {
        return Text.color(plugin.getConfig().getString("messages.prefix", "") + text);
    }

    private List<String> starts(List<String> values, String prefix) {
        List<String> result = new ArrayList<String>();
        String normalized = prefix.toLowerCase(Locale.ROOT);
        for (String value : values) {
            if (value.toLowerCase(Locale.ROOT).startsWith(normalized)) {
                result.add(value);
            }
        }
        return result;
    }
}
