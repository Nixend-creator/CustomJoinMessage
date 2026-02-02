package dev.nixend.customjoinmessage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomJoinMessagePlugin extends JavaPlugin {

    private boolean placeholderAPIEnabled = false;
    private LanguageManager languageManager;

    @Override
    public void onEnable() {
        // Инициализация менеджера языков
        languageManager = new LanguageManager(this);

        // Загрузка языка из конфига
        saveDefaultConfig();
        getConfig().options().copyDefaults(true);
        saveConfig();

        String lang = getConfig().getString("language", "ru").toLowerCase();
        if (!languageManager.loadLanguage(lang)) {
            getLogger().warning(languageManager.getMessage("language-load-failed", "{lang}", lang));
        }

        // Проверка Paper
        if (!isPaperServer()) {
            getLogger().severe(languageManager.getMessage("paper-required-title"));
            getLogger().severe(languageManager.getMessage("paper-required-line1"));
            getLogger().severe(languageManager.getMessage("paper-required-line2"));
            getLogger().severe(languageManager.getMessage("paper-required-line3"));
            getLogger().severe(languageManager.getMessage("paper-required-footer"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Интеграция с PlaceholderAPI
        placeholderAPIEnabled = getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
        if (placeholderAPIEnabled) {
            getLogger().info(languageManager.getMessage("papi-enabled"));
        } else {
            getLogger().warning(languageManager.getMessage("papi-disabled"));
        }

        // Регистрация листенеров
        getServer().getPluginManager().registerEvents(new JoinMessageListener(this, languageManager), this);
        getServer().getPluginManager().registerEvents(new QuitMessageListener(this, languageManager), this);

        // Регистрация команды
        getCommand("cjm").setExecutor(this);

        getLogger().info(languageManager.getMessage("plugin-enabled", "{version}", getDescription().getVersion()));
        getLogger().info(languageManager.getMessage("author-line"));
    }

    @Override
    public void onDisable() {
        getLogger().info(languageManager.getMessage("plugin-disabled"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("customjoinmessage.reload")) {
            sender.sendMessage(languageManager.getMessage("no-permission"));
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            reloadConfig();
            languageManager.reload();
            sender.sendMessage(languageManager.getMessage("config-reloaded"));
            return true;
        }

        sender.sendMessage(languageManager.getMessage("command-usage"));
        return true;
    }

    public boolean isPlaceholderAPIEnabled() {
        return placeholderAPIEnabled;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
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