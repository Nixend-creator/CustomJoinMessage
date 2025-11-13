package com.yourname.yourplugin;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.plugin.java.JavaPlugin;

public final class YourPlugin extends JavaPlugin {

    private static YourPlugin instance;
    private boolean placeholderAPIEnabled = false;
    private boolean miniPlaceholdersEnabled = false;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig(); // Создаёт config.yml, если его нет

        // Проверяем, установлены ли PlaceholderAPI и MiniPlaceholders
        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIEnabled = true;
            getLogger().info("PlaceholderAPI интеграция включена.");
        } else {
            getLogger().info("PlaceholderAPI не найден. Интеграция отключена.");
        }

        if (getServer().getPluginManager().getPlugin("MiniPlaceholders") != null) {
            miniPlaceholdersEnabled = true;
            getLogger().info("MiniPlaceholders интеграция включена.");
        } else {
            getLogger().info("MiniPlaceholders не найден. Интеграция отключена.");
        }

        // Регистрируем слушатель событий
        getServer().getPluginManager().registerEvents(new JoinMessageListener(this), this);

        getLogger().info("CustomJoinMessage плагин включён!");
    }

    @Override
    public void onDisable() {
        getLogger().info("CustomJoinMessage плагин выключен!");
    }

    public static YourPlugin getInstance() {
        return instance;
    }

    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    public boolean isMiniPlaceholdersEnabled() {
        return miniPlaceholdersEnabled;
    }
}
