# 项目约定与长期笔记

## 构建环境（Windows / 国内网络）

- Minecraft 26.3 + NeoForge，构建走 NeoGradle userdev（`net.neoforged.gradle.userdev` 7.1.39），Gradle 9.2.1，Java 25。
- **maven.neoforged.net 直连不通，必须走本机 v2rayN 代理 127.0.0.1:10808**；
  而 piston-meta.mojang.com 反而**不能走代理**（走代理 000，直连 200）。
  因此命令行形式固定为：
  ```
  JAVA_HOME="/c/Program Files/Java/jdk-25.0.4" ./gradlew <task> --no-daemon --console=plain \
    -Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=10808 \
    -Dhttp.proxyHost=127.0.0.1 -Dhttp.proxyPort=10808 \
    "-Dhttp.nonProxyHosts=localhost|127.0.0.1|*.mojang.com|*.minecraft.net|*.mojang.net"
  ```
  （`~/.gradle/gradle.properties` 里的代理项是注释掉的历史配置，不要改它。）
- 首次切换 NeoForge 版本后 `neoFormTransformSource` / `neoFormRecompile` 要跑 15 分钟左右，属正常。

## 运行期自检

- `./gradlew runServer` 可做无头冒烟测试（`run/server/eula.txt` 写 `eula=true`），
  看日志出现 `Done (` + `[ElementalCraft] Config Loaded:` 即代表配置注册通路正常。
- dev 环境会启动后 60 秒自动暂停，需手动 taskkill。

## 版本口径

- NeoForge 26.3 从 `26.3.0.76` 起不带 `-beta` 后缀。
- `ModConfig.Type.COMMON/SERVER` 在 **26.3.0.37-beta** 改名为 `LOCAL/SYNCED`（PR #3384）。
- 从 26.3 某版本起 NeoForge 禁止「整条注释全是空白」，dev 直接抛
  `IllegalStateException`（prod 先降级为 warn）。项目里原本用 `.comment(" ")` 当空行分隔，
  已全部移除——**以后写配置不要再加纯空白注释**。
- 26.3 已切换到 SDL，按键常量用 `InputConstants.KEY_*`（scancode 值），
  `org.lwjgl.glfw.GLFW` 不在编译类路径上。
