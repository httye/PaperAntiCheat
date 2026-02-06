package com.anticheat.detection;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Player;

public class MovementDetection implements Listener {
    private final JavaPlugin plugin;
    private boolean flyingEnabled;
    private boolean speedEnabled;
    private int maxFlightTime;
    private double verticalThreshold;
    private double maxSpeed;
    private int checkInterval;
    
    // 存储玩家飞行开始时间
    private java.util.Map<java.util.UUID, Long> flightStartTime = new java.util.HashMap<>();
    // 存储玩家最后移动时间，用于速度检测
    private java.util.Map<java.util.UUID, Long> lastMoveTime = new java.util.HashMap<>();
    // 存储玩家最后位置
    private java.util.Map<java.util.UUID, org.bukkit.Location> lastLocation = new java.util.HashMap<>();

    public MovementDetection(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loadConfig();
    }
    
    private void loadConfig() {
        // 从配置文件加载设置
        flyingEnabled = plugin.getConfig().getBoolean("movement.flying.enabled", true);
        speedEnabled = plugin.getConfig().getBoolean("movement.speed.enabled", true);
        maxFlightTime = plugin.getConfig().getInt("movement.flying.max-flight-time", 1000);
        verticalThreshold = plugin.getConfig().getDouble("movement.flying.vertical-threshold", 0.5);
        maxSpeed = plugin.getConfig().getDouble("movement.speed.max-speed", 6.0);
        checkInterval = plugin.getConfig().getInt("movement.speed.check-interval", 20);
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        // 重新加载配置
        loadConfig();
        
        Player player = event.getPlayer();
        
        // 飞行检测
        if (flyingEnabled && isFlyingIllegally(event)) {
            event.setCancelled(true);
            plugin.getLogger().warning(player.getName() + " 触发飞行检测");
        }

        // 加速检测
        if (speedEnabled && isSpeeding(event)) {
            event.setCancelled(true);
            plugin.getLogger().warning(player.getName() + " 触发加速检测");
        }
    }

    private boolean isFlyingIllegally(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // 如果玩家有飞行权限，不检测
        if (player.getAllowFlight()) {
            flightStartTime.remove(player.getUniqueId());
            return false;
        }
        
        // 检查是否在飞行
        if (!player.isOnGround() && !player.isInWater() && !player.isInLava()) {
            java.util.UUID playerId = player.getUniqueId();
            long currentTime = System.currentTimeMillis();
            
            // 记录飞行开始时间
            if (!flightStartTime.containsKey(playerId)) {
                flightStartTime.put(playerId, currentTime);
            }
            
            // 检查飞行时间是否超过阈值
            long flightDuration = currentTime - flightStartTime.get(playerId);
            if (flightDuration > maxFlightTime) {
                return true;
            }
            
            // 检查垂直移动速度（防止跳高作弊）
            double deltaY = event.getTo().getY() - event.getFrom().getY();
            if (deltaY > verticalThreshold && deltaY > 0) {
                return true;
            }
        } else {
            // 玩家在地面、水中或岩浆中，清除飞行记录
            flightStartTime.remove(player.getUniqueId());
        }
        
        return false;
    }

    private boolean isSpeeding(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        java.util.UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // 获取上次移动时间
        Long lastTime = lastMoveTime.get(playerId);
        if (lastTime == null) {
            // 第一次移动，记录时间和位置
            lastMoveTime.put(playerId, currentTime);
            lastLocation.put(playerId, event.getTo().clone());
            return false;
        }
        
        // 检查是否达到检查间隔
        if (currentTime - lastTime < (checkInterval * 50)) { // ticks to milliseconds
            return false;
        }
        
        // 计算移动距离
        org.bukkit.Location lastLoc = lastLocation.get(playerId);
        if (lastLoc == null) {
            lastLocation.put(playerId, event.getTo().clone());
            lastMoveTime.put(playerId, currentTime);
            return false;
        }
        
        double distance = lastLoc.distance(event.getTo());
        double timeDiff = (currentTime - lastTime) / 1000.0; // 转换为秒
        double speed = distance / timeDiff;
        
        // 考虑玩家状态和效果
        double adjustedMaxSpeed = maxSpeed;
        
        // 如果玩家在冲刺，增加速度限制
        if (player.isSprinting()) {
            adjustedMaxSpeed *= 1.3;
        }
        
        // 如果玩家有速度效果，增加速度限制
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED)) {
            org.bukkit.potion.PotionEffect speedEffect = player.getPotionEffect(org.bukkit.potion.PotionEffectType.SPEED);
            if (speedEffect != null) {
                adjustedMaxSpeed += speedEffect.getAmplifier() + 1;
            }
        }
        
        // 在特殊方块上增加速度限制
        org.bukkit.block.Block block = player.getLocation().getBlock().getRelative(org.bukkit.block.BlockFace.DOWN);
        if (block.getType() == org.bukkit.Material.ICE || 
            block.getType() == org.bukkit.Material.PACKED_ICE ||
            block.getType() == org.bukkit.Material.FROSTED_ICE ||
            block.getType() == org.bukkit.Material.SLIME_BLOCK) {
            adjustedMaxSpeed *= 1.5;
        }
        
        // 更新记录
        lastMoveTime.put(playerId, currentTime);
        lastLocation.put(playerId, event.getTo().clone());
        
        return speed > adjustedMaxSpeed && player.isOnGround();
    }
}
