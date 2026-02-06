package com.anticheat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import com.anticheat.AntiCheatSystem;

public class AntiCheatCommand implements CommandExecutor {
    private final AntiCheatSystem antiCheatSystem;
    private final JavaPlugin plugin;

    public AntiCheatCommand(JavaPlugin plugin, AntiCheatSystem antiCheatSystem) {
        this.plugin = plugin;
        this.antiCheatSystem = antiCheatSystem;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("anticheat.admin")) {
            sender.sendMessage("§c你没有权限使用此命令");
            return true;
        }

        if (args.length == 0) {
            showUsage(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.reloadConfig();
                antiCheatSystem.init();
                sender.sendMessage("§a反作弊系统已重载");
                break;
            case "status":
                showStatus(sender);
                break;
            default:
                showUsage(sender);
        }
        return true;
    }

    private void showUsage(CommandSender sender) {
        sender.sendMessage("§6反作弊系统命令用法:");
        sender.sendMessage("§e/anticheat reload §7- 重载配置");
        sender.sendMessage("§e/anticheat status §7- 查看系统状态");
    }

    private void showStatus(CommandSender sender) {
        sender.sendMessage("§6反作弊系统状态:");
        sender.sendMessage("§7- 移动检测: §a启用");
        sender.sendMessage("§7- 战斗检测: §a启用"); 
        sender.sendMessage("§7- 客户端验证: §a启用");
    }
}
