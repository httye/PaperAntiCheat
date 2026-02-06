package com.anticheat;

import org.bukkit.plugin.java.JavaPlugin;
import com.anticheat.database.DatabaseManager;

public final class Main extends JavaPlugin {
    private AntiCheatSystem antiCheatSystem;
    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        // 加载默认配置
        saveDefaultConfig();
        
        // 初始化数据库管理器
        databaseManager = new DatabaseManager(this);
        
        // 初始化反作弊系统
        antiCheatSystem = new AntiCheatSystem(this);
        
        getLogger().info("PaperAntiCheat 已启用！");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("PaperAntiCheat 已禁用。");
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
