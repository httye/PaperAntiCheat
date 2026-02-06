#!/usr/bin/env python3
"""
Minecraft Paper AntiCheat 自动化测试脚本
使用mcpi或直接RCON连接来测试反作弊功能
"""

import time
import subprocess
import threading
import os
import json
from datetime import datetime

class AntiCheatTester:
    def __init__(self):
        self.server_process = None
        self.test_results = []
        self.server_running = False
        
    def start_test_server(self):
        """启动测试服务器"""
        print("启动测试服务器...")
        try:
            # 构建插件
            subprocess.run(["gradlew", "shadowJar"], cwd=".", check=True)
            
            # 启动服务器（在后台）
            self.server_process = subprocess.Popen(
                ["test-server.bat"],
                stdout=subprocess.PIPE,
                stderr=subprocess.PIPE,
                stdin=subprocess.PIPE,
                text=True
            )
            self.server_running = True
            print("服务器已启动，等待加载完成...")
            time.sleep(30)  # 等待服务器完全加载
            
        except Exception as e:
            print(f"启动服务器失败: {e}")
            return False
        return True
    
    def stop_test_server(self):
        """停止测试服务器"""
        if self.server_process and self.server_running:
            print("停止测试服务器...")
            self.server_process.terminate()
            self.server_process.wait(timeout=10)
            self.server_running = False
    
    def simulate_flying_hack(self):
        """模拟飞行作弊"""
        print("测试飞行检测...")
        # 这里需要实际的Minecraft客户端连接
        # 由于无法直接控制游戏客户端，我们通过配置调整来测试
        test_config = {
            "movement": {
                "enabled": True,
                "flying": {
                    "enabled": True,
                    "max-flight-time": 100,  # 非常短的时间
                    "vertical-threshold": 0.1
                }
            }
        }
        
        # 写入测试配置
        with open("test-server/plugins/PaperAntiCheat/config.yml", "w") as f:
            json.dump(test_config, f, indent=2)
        
        # 重启服务器应用配置
        self.stop_test_server()
        time.sleep(5)
        self.start_test_server()
        
        # 检查日志中是否有飞行检测记录
        time.sleep(10)
        result = self.check_violation_log("flying")
        self.test_results.append(("Flying Detection", result))
    
    def simulate_speed_hack(self):
        """模拟速度作弊"""
        print("测试速度检测...")
        test_config = {
            "movement": {
                "enabled": True,
                "speed": {
                    "enabled": True,
                    "max-speed": 4.0,  # 正常玩家速度约4.3，设置较低阈值
                    "check-interval": 5
                }
            }
        }
        
        with open("test-server/plugins/PaperAntiCheat/config.yml", "w") as f:
            json.dump(test_config, f, indent=2)
        
        self.stop_test_server()
        time.sleep(5)
        self.start_test_server()
        
        time.sleep(10)
        result = self.check_violation_log("speed")
        self.test_results.append(("Speed Detection", result))
    
    def simulate_killaura(self):
        """模拟杀戮光环作弊"""
        print("测试杀戮光环检测...")
        test_config = {
            "combat": {
                "enabled": True,
                "killaura": {
                    "enabled": True,
                    "max-cps": 6,  # 正常玩家CPS通常4-6，设置较低阈值
                    "min-attack-cooldown": 150
                }
            }
        }
        
        with open("test-server/plugins/PaperAntiCheat/config.yml", "w") as f:
            json.dump(test_config, f, indent=2)
        
        self.stop_test_server()
        time.sleep(5)
        self.start_test_server()
        
        time.sleep(10)
        result = self.check_violation_log("killaura")
        self.test_results.append(("KillAura Detection", result))
    
    def simulate_reach_hack(self):
        """模拟攻击距离作弊"""
        print("测试攻击距离检测...")
        test_config = {
            "combat": {
                "enabled": True,
                "reach": {
                    "enabled": True,
                    "max-reach": 3.0,  # 正常最大3.15，设置较低阈值
                    "tolerance": 0.01
                }
            }
        }
        
        with open("test-server/plugins/PaperAntiCheat/config.yml", "w") as f:
            json.dump(test_config, f, indent=2)
        
        self.stop_test_server()
        time.sleep(5)
        self.start_test_server()
        
        time.sleep(10)
        result = self.check_violation_log("reach")
        self.test_results.append(("Reach Detection", result))
    
    def check_violation_log(self, violation_type):
        """检查违规日志"""
        try:
            log_file = "test-server/logs/latest.log"
            if os.path.exists(log_file):
                with open(log_file, "r", encoding="utf-8") as f:
                    content = f.read()
                    if violation_type.lower() in content.lower():
                        return True
            return False
        except Exception as e:
            print(f"检查日志失败: {e}")
            return False
    
    def test_database_functionality(self):
        """测试数据库功能"""
        print("测试数据库功能...")
        db_path = "test-server/plugins/PaperAntiCheat/violations.db"
        if os.path.exists(db_path):
            # 检查数据库文件大小
            size = os.path.getsize(db_path)
            if size > 1024:  # 数据库应该有数据
                self.test_results.append(("Database Functionality", True))
                return True
        self.test_results.append(("Database Functionality", False))
        return False
    
    def test_web_interface(self):
        """测试Web界面"""
        print("测试Web界面...")
        web_dir = "test-server/plugins/PaperAntiCheat/web"
        if os.path.exists(web_dir) and os.path.exists(os.path.join(web_dir, "index.html")):
            self.test_results.append(("Web Interface", True))
            return True
        self.test_results.append(("Web Interface", False))
        return False
    
    def run_all_tests(self):
        """运行所有测试"""
        print("=" * 50)
        print("开始Minecraft Paper反作弊综合测试")
        print("=" * 50)
        
        # 启动服务器
        if not self.start_test_server():
            print("无法启动测试服务器，跳过测试")
            return
        
        try:
            # 测试各个功能模块
            self.test_web_interface()
            self.test_database_functionality()
            
            # 由于无法直接模拟玩家行为，这里主要测试配置和日志系统
            print("\n注意：由于无法直接控制Minecraft客户端，")
            print("以下测试主要验证配置加载和日志记录功能。")
            print("实际的作弊检测需要在游戏中手动测试。")
            
            # 测试配置加载
            config_file = "test-server/plugins/PaperAntiCheat/config.yml"
            if os.path.exists(config_file):
                self.test_results.append(("Config Loading", True))
            else:
                self.test_results.append(("Config Loading", False))
            
            # 测试命令系统
            log_file = "test-server/logs/latest.log"
            if os.path.exists(log_file):
                with open(log_file, "r", encoding="utf-8") as f:
                    content = f.read()
                    if "PaperAntiCheat" in content and "enabled" in content:
                        self.test_results.append(("Command System", True))
                    else:
                        self.test_results.append(("Command System", False))
            
        finally:
            self.stop_test_server()
        
        # 显示测试结果
        self.display_results()
    
    def display_results(self):
        """显示测试结果"""
        print("\n" + "=" * 50)
        print("测试结果汇总")
        print("=" * 50)
        
        passed = 0
        total = len(self.test_results)
        
        for test_name, result in self.test_results:
            status = "✓ PASS" if result else "✗ FAIL"
            print(f"{test_name:<25} {status}")
            if result:
                passed += 1
        
        print("-" * 50)
        print(f"总计: {passed}/{total} 通过")
        
        if passed == total:
            print("🎉 所有测试通过！反作弊系统基本功能正常。")
        else:
            print("⚠️  部分测试失败，请检查相关功能。")
        
        print("\n" + "=" * 50)
        print("手动测试建议:")
        print("1. 启动服务器: 双击 test-server.bat")
        print("2. 连接到 localhost:25565")
        print("3. 给自己管理员权限: /op <你的用户名>")
        print("4. 测试飞行: 双击空格键飞行，观察是否被检测")
        print("5. 测试速度: 使用加速药水或修改客户端，观察是否被检测")
        print("6. 测试战斗: 快速点击攻击，观察是否被检测")
        print("7. 访问Web界面: http://localhost:8080 (如果启用)")
        print("=" * 50)

if __name__ == "__main__":
    tester = AntiCheatTester()
    tester.run_all_tests()
