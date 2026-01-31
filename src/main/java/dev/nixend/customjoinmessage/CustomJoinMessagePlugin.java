package dev.nixend.customjoinmessage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomJoinMessagePlugin extends JavaPlugin {

    private boolean placeholderAPIEnabled = false;
    private JoinMessageListener joinListener;

    @Override
    public void onEnable() {
        // Проверка окружения
        if (!isPaperServer()) {
            getLogger().severe("========================================");
            getLogger().severe("CustomJoinMessage требует сервер Paper 1.21+");
            getLogger().severe("Spigot/Vanilla не поддерживаются из-за MiniMessage API");
            getLogger().severe("Скачайте Paper: https://papermc.io");
            getLogger().severe("========================================");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Интеграция с PlaceholderAPI
        placeholderAPIEnabled = getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (placeholderAPIEnabled) {
            getLogger().info("✓ PlaceholderAPI интеграция активна");
        } else {
            getLogger().warning("⚠️  PlaceholderAPI не найден. Плейсхолдеры {placeholder_x} работать не будут.");
        }

        // Генерация конфига с дефолтами
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        // Регистрация листенеров
        joinListener = new JoinMessageListener(this);
        getServer().getPluginManager().registerEvents(joinListener, this);
        getServer().getPluginManager().registerEvents(new QuitMessageListener(this), this);

        // Регистрация команды
        getCommand("cjm").setExecutor(this);

        getLogger().info("✅ CustomJoinMessage v" + getDescription().getVersion() + " загружен!");
        getLogger().info("   Автор: Nixend | Версия сборки: " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomJoinMessage выключен.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customjoinmessage.reload")) {
            sender.sendMessage("§cУ вас нет прав для выполнения этой команды.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            sender.sendMessage("§aКонфигурация CustomJoinMessage перезагружена!");
            return true;
        }

        sender.sendMessage("§6/cjm reload §7- перезагрузить конфигурацию");
        return true;
    }

    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    private boolean isPaperServer() {
        try {
            Class.forName("io.papermc.paper.adventure.PaperAdventure");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}