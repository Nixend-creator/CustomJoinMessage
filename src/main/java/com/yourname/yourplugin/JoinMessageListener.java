package com.yourname.yourplugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class JoinMessageListener implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final YourPlugin plugin;

    public JoinMessageListener(YourPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();

        // Проверяем, новый ли это игрок (по количеству входов)
        boolean isNewPlayer = player.getFirstPlayed() == player.getLastPlayed(); // Упрощённая проверка

        String configKey = isNewPlayer ? "welcome-message-new" : "welcome-message-existing";
        List<String> rawMessages = config.getStringList(configKey);

        if (rawMessages.isEmpty()) {
            // Если список сообщений пуст, не отправляем ничего
            return;
        }

        String discordUrl = config.getString("discord-url", "");
        String telegramUrl = config.getString("telegram-url", "");

        boolean useAnimation = config.getBoolean("use-animation", true);
        int delayTicks = config.getInt("animation-delay-ticks", 5);
        boolean broadcastToServer = config.getBoolean("broadcast-to-server", false);

        if (useAnimation) {
            // Запускаем анимацию
            startAnimatedMessage(player, rawMessages, discordUrl, telegramUrl, delayTicks, broadcastToServer);
        } else {
            // Отправляем всё сразу
            sendFullMessage(player, rawMessages, discordUrl, telegramUrl, broadcastToServer);
        }

        // Отключаем стандартное сообщение о входе
        event.joinMessage(Component.empty());
    }

    private void startAnimatedMessage(Player player, List<String> messages, String discordUrl, String telegramUrl, int delayTicks, boolean broadcast) {
        new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (index < messages.size()) {
                    String rawLine = messages.get(index);
                    // Обрабатываем плейсхолдеры перед десериализацией
                    String processedLine = processPlaceholders(rawLine, player, discordUrl, telegramUrl);
                    Component lineComponent = miniMessage.deserialize(processedLine);
                    if (broadcast) {
                        Bukkit.broadcast(lineComponent);
                    } else {
                        player.sendMessage(lineComponent);
                    }
                    index++;
                } else {
                    cancel(); // Останавливаем таймер после отправки всех строк
                }
            }
        }.runTaskTimer(plugin, 0L, delayTicks); // Запускаем с задержкой
    }

    private void sendFullMessage(Player player, List<String> messages, String discordUrl, String telegramUrl, boolean broadcast) {
        TextComponent.Builder fullMessage = Component.text();
        for (int i = 0; i < messages.size(); i++) {
            String rawLine = messages.get(i);
            // Обрабатываем плейсхолдеры перед десериализацией
            String processedLine = processPlaceholders(rawLine, player, discordUrl, telegramUrl);
            Component lineComponent = miniMessage.deserialize(processedLine);
            fullMessage.append(lineComponent);
            if (i < messages.size() - 1) {
                fullMessage.append(Component.newline()); // Добавляем перенос строки между сообщениями
            }
        }

        if (broadcast) {
            Bukkit.broadcast(fullMessage.build());
        } else {
            player.sendMessage(fullMessage.build());
        }
    }

    // Метод для обработки плейсхолдеров
    private String processPlaceholders(String message, Player player, String discordUrl, String telegramUrl) {
        // Заменяем кастомные плейсхолдеры (например, {SERVER})
        String processedMessage = message
                .replace("{PLAYER}", player.getName())
                .replace("{SERVER}", player.getServer().getName())
                .replace("{DISCORD_URL}", discordUrl)
                .replace("{TELEGRAM_URL}", telegramUrl);

        // Обработка PlaceholderAPI
        if (plugin.isPlaceholderAPIEnabled()) {
            processedMessage = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, processedMessage);
        }

        // MiniPlaceholders обычно интегрируется через PlaceholderAPI, поэтому повторный вызов не нужен,
        // если только не требуется специфическая обработка.

        return processedMessage;
    }
}
