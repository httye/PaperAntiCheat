package com.anticheat.detection;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public class ClientVerification implements Listener {
    private final JavaPlugin plugin;
    private boolean strictMode;
    private java.util.List<String> whitelistedMods;

    public ClientVerification(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loadConfig();
    }
    
    private void loadConfig() {
        // 从配置文件加载设置
        strictMode = plugin.getConfig().getBoolean("client.strict-mode", false);
        whitelistedMods = plugin.getConfig().getStringList("client.whitelist-mods");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // 重新加载配置
        loadConfig();
        
        // 客户端完整性检测
        if (!isClientValid(event)) {
            event.getPlayer().kickPlayer("客户端验证失败！请使用官方客户端");
            plugin.getLogger().warning(event.getPlayer().getName() + " 客户端验证失败");
            return; // 如果客户端验证失败，不需要再检查白名单
        }

        // 白名单检测
        if (!isWhitelisted(event)) {
            event.getPlayer().kickPlayer("您的Mod未被服务器允许");
            plugin.getLogger().warning(event.getPlayer().getName() + " 使用未授权的Mod");
        }
    }

    private boolean isClientValid(PlayerJoinEvent event) {
        // 客户端验证逻辑
        Player player = event.getPlayer();
        
        // 在严格模式下，阻止所有非官方客户端
        if (strictMode) {
            // 检查玩家是否使用了Mod（通过检查客户端信息）
            // 注意：Minecraft原版API无法直接检测Mod，这里提供一个基础实现
            // 实际应用中可能需要更复杂的检测方法
            
            // 检查玩家名称是否包含可疑字符（某些Mod会修改显示名称）
            String playerName = player.getName();
            if (playerName.contains("[") || playerName.contains("]")) {
                return false;
            }
            
            // 检查玩家是否在短时间内重复加入（可能是机器人）
            long joinTime = System.currentTimeMillis();
            Long lastJoin = playerJoinTimes.get(player.getUniqueId());
            if (lastJoin != null && (joinTime - lastJoin) < 5000) {
                return false; // 5秒内重复加入
            }
            playerJoinTimes.put(player.getUniqueId(), joinTime);
        }
        
        return true;
    }

    private boolean isWhitelisted(PlayerJoinEvent event) {
        // 白名单验证逻辑
        if (whitelistedMods.isEmpty()) {
            return true; // 如果没有设置白名单Mod，允许所有
        }
        
        // 在严格模式下，如果白名单为空，则阻止所有Mod
        if (strictMode && whitelistedMods.isEmpty()) {
            return false;
        }
        
        // 这里是一个简化实现，实际的Mod检测需要更复杂的方法
        // 例如通过自定义数据包、客户端握手协议等
        
        Player player = event.getPlayer();
        // 基础检查：如果玩家名称包含可疑模式，可能使用了未授权Mod
        String playerName = player.getName().toLowerCase();
        String[] suspiciousPatterns = {"hack", "cheat", "aura", "kill", "fly", "speed"};
        
        for (String pattern : suspiciousPatterns) {
            if (playerName.contains(pattern)) {
                return false;
            }
        }
        
        return true;
    }
    
    // 存储玩家最后加入时间，用于检测快速重连
    private java.util.Map<java.util.UUID, Long> playerJoinTimes = new java.util.HashMap<>();
}
