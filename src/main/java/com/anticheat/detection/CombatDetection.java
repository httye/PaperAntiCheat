package com.anticheat.detection;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;

public class CombatDetection implements Listener {
    private final JavaPlugin plugin;
    private boolean killauraEnabled;
    private boolean reachEnabled;
    private int maxCps;
    private int minAttackCooldown;
    private double maxReach;
    private double tolerance;
    
    // 存储玩家最后攻击时间
    private java.util.Map<java.util.UUID, Long> lastHitTime = new java.util.HashMap<>();
    // 存储玩家攻击计数（用于CPS检测）
    private java.util.Map<java.util.UUID, Integer> hitCount = new java.util.HashMap<>();
    // 存储CPS检测周期开始时间
    private java.util.Map<java.util.UUID, Long> cpsStartTime = new java.util.HashMap<>();

    public CombatDetection(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        loadConfig();
    }
    
    private void loadConfig() {
        // 从配置文件加载设置
        killauraEnabled = plugin.getConfig().getBoolean("combat.killaura.enabled", true);
        reachEnabled = plugin.getConfig().getBoolean("combat.reach.enabled", true);
        maxCps = plugin.getConfig().getInt("combat.killaura.max-cps", 12);
        minAttackCooldown = plugin.getConfig().getInt("combat.killaura.min-attack-cooldown", 100);
        maxReach = plugin.getConfig().getDouble("combat.reach.max-reach", 3.5);
        tolerance = plugin.getConfig().getDouble("combat.reach.tolerance", 0.1);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        // 重新加载配置
        loadConfig();
        
        if (!(event.getDamager() instanceof Player)) {
            return; // 只检测玩家攻击
        }

        Player player = (Player) event.getDamager();

        // 检查KillAura（攻击速度/CPS）
        if (killauraEnabled && isKillAura(player)) {
            event.setCancelled(true);
            plugin.getLogger().warning(player.getName() + " 触发KillAura检测");
        }

        // 检查攻击范围（Reach）
        if (reachEnabled && isOutOfRange(event)) {
            event.setCancelled(true);
            plugin.getLogger().warning(player.getName() + " 触发攻击范围检测");
        }
    }

    private boolean isKillAura(Player player) {
        java.util.UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        
        // 初始化CPS检测
        if (!cpsStartTime.containsKey(playerId)) {
            cpsStartTime.put(playerId, currentTime);
            hitCount.put(playerId, 0);
        }
        
        // 更新攻击计数
        hitCount.put(playerId, hitCount.get(playerId) + 1);
        
        // 检查是否超过1秒，重置计数
        if (currentTime - cpsStartTime.get(playerId) >= 1000) {
            int currentCps = hitCount.get(playerId);
            cpsStartTime.put(playerId, currentTime);
            hitCount.put(playerId, 0);
            
            // 检查CPS是否超过限制
            if (currentCps > maxCps) {
                return true;
            }
        }
        
        // 检查最小攻击冷却
        Long lastHit = lastHitTime.get(playerId);
        if (lastHit != null) {
            long timeDiff = currentTime - lastHit;
            if (timeDiff < minAttackCooldown) {
                lastHitTime.put(playerId, currentTime);
                return true;
            }
        }
        
        lastHitTime.put(playerId, currentTime);
        return false;
    }

    private boolean isOutOfRange(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return false;
        }

        Player player = (Player) event.getDamager();
        double distance = player.getLocation().distance(event.getEntity().getLocation());

        // 考虑不同武器的攻击范围
        double adjustedMaxReach = maxReach + tolerance;
        
        // 如果玩家持有剑类武器，增加攻击范围
        org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();
        if (item != null && item.getType().name().contains("SWORD")) {
            adjustedMaxReach += 0.5;
        }
        
        // 如果玩家有力量效果，略微增加范围
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.INCREASE_DAMAGE)) {
            adjustedMaxReach += 0.2;
        }

        return distance > adjustedMaxReach;
    }
}
