package com.anticheat;

import org.bukkit.plugin.java.JavaPlugin;
import com.anticheat.detection.MovementDetection;
import com.anticheat.detection.CombatDetection; 
import com.anticheat.detection.ClientVerification;

public class AntiCheatSystem {
    private final JavaPlugin plugin;
    private MovementDetection movementDetection;
    private CombatDetection combatDetection;
    private ClientVerification clientVerification;

    public AntiCheatSystem(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void init() {
        // 加载配置
        plugin.saveDefaultConfig();
        
        // 初始化各个检测模块
        this.movementDetection = new MovementDetection(plugin);
        this.combatDetection = new CombatDetection(plugin);
        this.clientVerification = new ClientVerification(plugin);
        
        // 注册事件监听器
        plugin.getServer().getPluginManager().registerEvents(movementDetection, plugin);
        plugin.getServer().getPluginManager().registerEvents(combatDetection, plugin);
        plugin.getServer().getPluginManager().registerEvents(clientVerification, plugin);
    }
    
    public MovementDetection getMovementDetection() {
        return movementDetection;
    }
    
    public CombatDetection getCombatDetection() {
        return combatDetection;
    }
    
    public ClientVerification getClientVerification() {
        return clientVerification;
    }
}
