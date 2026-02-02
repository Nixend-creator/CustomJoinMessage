package dev.nixend.customjoinmessage;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

public class LanguageManager {

    private final CustomJoinMessagePlugin plugin;
    private FileConfiguration messages;
    private String currentLanguage;

    public LanguageManager(CustomJoinMessagePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean loadLanguage(String lang) {
        currentLanguage = lang.toLowerCase();
        File langFile = new File(plugin.getDataFolder(), "messages_" + currentLanguage + ".yml");

        // Копируем дефолтный файл если не существует
        if (!langFile.exists()) {
            plugin.saveResource("messages_" + currentLanguage + ".yml", false);
        }

        // Загружаем конфиг
        messages = YamlConfiguration.loadConfiguration(langFile);

        // Загружаем дефолты из ресурсов
        InputStream defStream = plugin.getResource("messages_" + currentLanguage + ".yml");
        if (defStream != null) {
            messages.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defStream)));
        }

        // Валидация обязательных ключей
        if (!messages.contains("plugin-enabled")) {
            plugin.getLogger().log(Level.WARNING, "Language file missing required keys. Using fallback...");
            return loadFallback();
        }

        plugin.getLogger().info("Loaded language: " + currentLanguage.toUpperCase());
        return true;
    }

    private boolean loadFallback() {
        // Пытаемся загрузить русский как фолбэк
        try {
            currentLanguage = "ru";
            InputStream defStream = plugin.getResource("messages_ru.yml");
            if (defStream != null) {
                messages = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream));
                plugin.getLogger().warning("Using Russian as fallback language");
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to load fallback language", e);
        }
        return false;
    }

    public String getMessage(String key, String... replacements) {
        String msg = messages.getString(key, "§cMISSING: " + key);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                msg = msg.replace(replacements[i], replacements[i + 1]);
            }
        }
        return msg;
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public void reload() {
        loadLanguage(currentLanguage);
    }
}