package com.anticheat.database;

import org.bukkit.plugin.java.JavaPlugin;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class DatabaseManager {
    private final JavaPlugin plugin;
    private Connection connection;
    private boolean enabled;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.enabled = plugin.getConfig().getBoolean("database.enabled", false);
        
        if (enabled) {
            initializeDatabase();
        }
    }

    private void initializeDatabase() {
        try {
            // 加载SQLite驱动
            Class.forName("org.sqlite.JDBC");
            
            // 创建数据库连接
            String dbPath = plugin.getDataFolder().getAbsolutePath() + "/anticheat.db";
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            
            // 创建表
            createTables();
            
            plugin.getLogger().info("数据库初始化成功");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "数据库初始化失败", e);
            this.enabled = false;
        }
    }

    private void createTables() throws SQLException {
        if (!enabled || connection == null) return;
        
        Statement stmt = connection.createStatement();
        
        // 创建违规记录表
        String violationsTable = """
            CREATE TABLE IF NOT EXISTS violations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_uuid TEXT NOT NULL,
                player_name TEXT NOT NULL,
                violation_type TEXT NOT NULL,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                details TEXT,
                server_ip TEXT
            )
            """;
        
        // 创建玩家统计表
        String statsTable = """
            CREATE TABLE IF NOT EXISTS player_stats (
                player_uuid TEXT PRIMARY KEY,
                player_name TEXT NOT NULL,
                total_violations INTEGER DEFAULT 0,
                last_violation DATETIME,
                first_join DATETIME DEFAULT CURRENT_TIMESTAMP,
                last_join DATETIME DEFAULT CURRENT_TIMESTAMP
            )
            """;
        
        stmt.execute(violationsTable);
        stmt.execute(statsTable);
        stmt.close();
    }

    public void logViolation(String playerUUID, String playerName, String violationType, String details) {
        if (!enabled || connection == null) return;
        
        try {
            String sql = "INSERT INTO violations (player_uuid, player_name, violation_type, details, server_ip) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            pstmt.setString(1, playerUUID);
            pstmt.setString(2, playerName);
            pstmt.setString(3, violationType);
            pstmt.setString(4, details);
            pstmt.setString(5, getServerIP());
            pstmt.executeUpdate();
            pstmt.close();
            
            // 更新玩家统计
            updatePlayerStats(playerUUID, playerName);
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "记录违规失败", e);
        }
    }

    private void updatePlayerStats(String playerUUID, String playerName) throws SQLException {
        String checkSql = "SELECT total_violations FROM player_stats WHERE player_uuid = ?";
        PreparedStatement checkStmt = connection.prepareStatement(checkSql);
        checkStmt.setString(1, playerUUID);
        ResultSet rs = checkStmt.executeQuery();
        
        if (rs.next()) {
            // 更新现有记录
            int currentViolations = rs.getInt("total_violations");
            String updateSql = "UPDATE player_stats SET total_violations = ?, last_violation = CURRENT_TIMESTAMP, player_name = ? WHERE player_uuid = ?";
            PreparedStatement updateStmt = connection.prepareStatement(updateSql);
            updateStmt.setInt(1, currentViolations + 1);
            updateStmt.setString(2, playerName);
            updateStmt.setString(3, playerUUID);
            updateStmt.executeUpdate();
            updateStmt.close();
        } else {
            // 插入新记录
            String insertSql = "INSERT INTO player_stats (player_uuid, player_name, total_violations, last_violation) VALUES (?, ?, 1, CURRENT_TIMESTAMP)";
            PreparedStatement insertStmt = connection.prepareStatement(insertSql);
            insertStmt.setString(1, playerUUID);
            insertStmt.setString(2, playerName);
            insertStmt.executeUpdate();
            insertStmt.close();
        }
        
        rs.close();
        checkStmt.close();
    }

    public int getTotalViolationsToday() {
        if (!enabled || connection == null) return 0;
        
        try {
            String sql = "SELECT COUNT(*) FROM violations WHERE date(timestamp) = date('now')";
            PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            int count = rs.next() ? rs.getInt(1) : 0;
            rs.close();
            pstmt.close();
            return count;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "获取今日违规数量失败", e);
            return 0;
        }
    }

    public int getOnlinePlayersCount() {
        // 这个方法应该在主插件类中调用Bukkit API
        // 这里返回0，实际使用时通过API获取
        return 0;
    }

    private String getServerIP() {
        try {
            return plugin.getServer().getIp();
        } catch (Exception e) {
            return "localhost";
        }
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                plugin.getLogger().log(Level.WARNING, "关闭数据库连接失败", e);
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
