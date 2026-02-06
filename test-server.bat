@echo off
echo 正在设置Minecraft Paper反作弊测试服务器...

REM 创建必要的目录结构
if not exist "test-server" mkdir "test-server"
if not exist "test-server\plugins" mkdir "test-server\plugins"

REM 复制插件到测试服务器
copy "build\libs\PaperAntiCheat-1.0.0.jar" "test-server\plugins\" 2>nul
if errorlevel 1 (
    echo 构建插件...
    gradlew shadowJar
    if errorlevel 1 (
        echo 插件构建失败！
        pause
        exit /b 1
    )
    copy "build\libs\PaperAntiCheat-1.0.0.jar" "test-server\plugins\"
)

REM 下载Paper服务器（如果不存在）
if not exist "test-server\paper-1.20.4.jar" (
    echo 下载Paper 1.20.4服务器...
    curl -o "test-server\paper-1.20.4.jar" "https://api.papermc.io/v2/projects/paper/versions/1.20.4/builds/497/downloads/paper-1.20.4-497.jar"
    if errorlevel 1 (
        echo 下载Paper服务器失败！
        echo 请手动下载Paper 1.20.4服务器并放置在test-server目录中
        pause
        exit /b 1
    )
)

REM 创建eula.txt
echo eula=true > "test-server\eula.txt"

REM 创建server.properties（启用RCON用于测试）
(
echo server-port=25565
echo rcon.port=25575
echo rcon.password=testpassword
echo enable-rcon=true
echo online-mode=false
echo max-players=10
) > "test-server\server.properties"

REM 启动服务器
cd test-server
echo 启动Minecraft服务器...
java -Xmx2G -jar paper-1.20.4.jar nogui

pause
