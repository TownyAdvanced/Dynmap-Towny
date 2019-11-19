package org.dynmap.towny;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class CommandReload implements CommandExecutor, TabCompleter {
    private Plugin plugin;
    private String prefix;
    private String usage;

    public CommandReload(Plugin plugin) {
        this.plugin = plugin;
        this.prefix = this.plugin.getConfig().getString("prefix");

        if (prefix == null) {
            prefix = ChatColor.translateAlternateColorCodes('&', "[&bynmap Towny&r]");
        } else
            prefix = ChatColor.translateAlternateColorCodes('&', prefix);

        this.usage = "%prefix%" + "Dynmap-Towny v" + plugin.getDescription().getVersion() + "\n"
                + "%prefix%" + "Usage: /dmt reload";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = null;

        if (sender instanceof Player) {
            player = (Player) sender;
        }

        if (args.length != 1 || args[0].equalsIgnoreCase("help")) {
            if (player != null && !player.hasPermission("dynmaptowny.command")) {
                return false;
            }

            sender.sendMessage(getUsage());
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (player != null && !player.hasPermission("dynmaptowny.reload")) {
                return false;
            }

            reload();
            sender.sendMessage(this.prefix + "Dynmap-Towny reloaded!");
            return true;
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("dynmaptowny") || !(sender instanceof Player))
            return null;

        List<String> options = new ArrayList<>();
        Player player = (Player) sender;

        if (player.hasPermission("dynmaptowny.command")) {
            addOption("help", options, args);

            if (player.hasPermission("dynmaptowny.reload")) {
                addOption("reload", options, args);
            }

            return options;
        }

        return null;
    }

    private String getUsage() {
        return this.usage.replaceAll("%prefix%", this.prefix);
    }

    private void updatePrefix() {
        String newPrefix = this.plugin.getConfig().getString("prefix");

        if (newPrefix != null)
            this.prefix = ChatColor.translateAlternateColorCodes('&', newPrefix);
    }

    private void reload() {
        this.plugin.reloadConfig();
        this.updatePrefix();
    }

    private void addOption(String commandName, List<String> options, String[] args) {
        if (args.length != 1) return;

        if (commandName.startsWith(args[0]))
            options.add((commandName));
    }
}
