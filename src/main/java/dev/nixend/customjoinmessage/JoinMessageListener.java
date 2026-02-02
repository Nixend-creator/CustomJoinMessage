package dev.nixend.customjoinmessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JoinMessageListener implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();
    private final CustomJoinMessagePlugin plugin;
    private final LanguageManager languageManager;
    private final Map<UUID, BukkitTask> animationTasks = new HashMap<>();

    public JoinMessageListener(CustomJoinMessagePlugin plugin, LanguageManager languageManager) {
        this.plugin = plugin;
        this.languageManager = languageManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        boolean isNewPlayer = !player.hasPlayedBefore();

        // === 1. Персональное сообщение игроку ===
        String configKey = isNewPlayer ? "welcome-message-new" : "welcome-message-existing";
        List<String> rawMessages = plugin.getConfig().getStringList(configKey);

        if (!rawMessages.isEmpty()) {
            String serverName = plugin.getConfig().getString("server-name", "MyServer");
            String discordUrl = plugin.getConfig().getString("discord-url", "");
            String telegramUrl = plugin.getConfig().getString("telegram-url", "");
            boolean useAnimation = plugin.getConfig().getBoolean("use-animation", true);
            int delayTicks = plugin.getConfig().getInt("animation-delay-ticks", 5);
            boolean broadcastToServer = plugin.getConfig().getBoolean("broadcast-to-server", false);

            event.joinMessage(Component.empty());

            if (useAnimation) {
                startAnimatedMessage(player, rawMessages, serverName, discordUrl, telegramUrl, delayTicks, broadcastToServer);
            } else {
                sendFullMessage(player, rawMessages, serverName, discordUrl, telegramUrl, broadcastToServer);
            }
        } else {
            event.joinMessage(Component.empty());
        }

        // === 2. Служебное сообщение в чат (только для админов) ===
        if (isNewPlayer && plugin.getConfig().getBoolean("EnableFirstJoinMessage", true)) {
            String messageTemplate = plugin.getConfig().getString("FirstJoinMessage", "&a&l%player%&r &7joined the server for the first time! &e✨");
            sendAdminChatMessage(player, messageTemplate, true);
        } else if (!isNewPlayer && plugin.getConfig().getBoolean("EnableJoinMessage", true)) {
            String messageTemplate = plugin.getConfig().getString("JoinMessage", "&a+ &f%player%&r &7joined the server.");
            sendAdminChatMessage(player, messageTemplate, false);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Отмена анимаций при выходе
        BukkitTask task = animationTasks.remove(event.getPlayer().getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }

    private void startAnimatedMessage(Player player, List<String> messages, String serverName, String discordUrl, String telegramUrl, int delayTicks, boolean broadcast) {
        BukkitRunnable task = new BukkitRunnable() {
            private int index = 0;

            @Override
            public void run() {
                if (!player.isOnline() || index >= messages.size()) {
                    animationTasks.remove(player.getUniqueId());
                    this.cancel();
                    return;
                }

                String rawLine = messages.get(index);
                String processedLine = processPlaceholders(rawLine, player, serverName, discordUrl, telegramUrl, plugin.isPlaceholderAPIEnabled());

                try {
                    Component component = miniMessage.deserialize(processedLine, buildTagResolver(player, serverName, discordUrl, telegramUrl));
                    if (broadcast) {
                        Bukkit.broadcast(component);
                    } else {
                        player.sendMessage(component);
                    }
                    index++;
                } catch (Exception e) {
                    plugin.getLogger().warning(languageManager.getMessage("minimessage-error-line", "{line}", rawLine));
                }
            }
        };

        BukkitTask bukkitTask = task.runTaskTimer(plugin, 0L, delayTicks);
        animationTasks.put(player.getUniqueId(), bukkitTask);
    }

    private void sendFullMessage(Player player, List<String> messages, String serverName, String discordUrl, String telegramUrl, boolean broadcast) {
        for (String rawLine : messages) {
            String processedLine = processPlaceholders(rawLine, player, serverName, discordUrl, telegramUrl, plugin.isPlaceholderAPIEnabled());

            try {
                Component component = miniMessage.deserialize(processedLine, buildTagResolver(player, serverName, discordUrl, telegramUrl));
                if (broadcast) {
                    Bukkit.broadcast(component);
                } else {
                    player.sendMessage(component);
                }
            } catch (Exception e) {
                plugin.getLogger().warning(languageManager.getMessage("minimessage-error-line", "{line}", rawLine));
            }
        }
    }

    // Отправка служебного сообщения ТОЛЬКО админам/OP
    private void sendAdminChatMessage(Player player, String template, boolean isFirstJoin) {
        String processed = template
                .replace("%player%", player.getName())
                .replace("%PLAYER%", player.getName())
                .replace("%NICK%", player.getName())
                .replace("{player}", player.getName())
                .replace("{displayname}", player.getDisplayName())
                .replace("%displayname%", player.getDisplayName());

        if (plugin.isPlaceholderAPIEnabled()) {
            try {
                processed = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, processed);
            } catch (NoClassDefFoundError ignored) {
            }
        }

        Component messageComponent;
        if (processed.contains("<") && processed.contains(">")) {
            // MiniMessage формат
            try {
                messageComponent = miniMessage.deserialize(processed, TagResolver.resolver(
                        Placeholder.component("player", Component.text(player.getName())),
                        Placeholder.component("displayname", player.displayName())
                ));
            } catch (Exception e) {
                plugin.getLogger().warning(languageManager.getMessage("minimessage-error-line", "{line}", template));
                messageComponent = Component.text(processed);
            }
        } else {
            // Legacy &-коды
            messageComponent = legacySerializer.deserialize(processed);
        }

        boolean adminOnlyMode = plugin.getConfig().getBoolean("admin-only-mode", true);
        String adminPermission = plugin.getConfig().getString("admin-permission", "customjoinmessage.see");

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (adminOnlyMode) {
                if (online.isOp()) {
                    online.sendMessage(messageComponent);
                }
            } else {
                if (online.isOp() || online.hasPermission(adminPermission)) {
                    online.sendMessage(messageComponent);
                }
            }
        }
    }

    public static String processPlaceholders(String message, Player player, String serverName, String discordUrl, String telegramUrl, boolean usePAPI) {
        String processed = message
                .replace("{PLAYER}", player.getName())
                .replace("{player}", player.getName())
                .replace("{DISPLAYNAME}", player.getDisplayName())
                .replace("{displayname}", player.getDisplayName())
                .replace("{SERVER}", serverName)
                .replace("{server}", serverName)
                .replace("{ONLINE}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("{MAX}", String.valueOf(Bukkit.getMaxPlayers()))
                .replace("{max}", String.valueOf(Bukkit.getMaxPlayers()))
                .replace("{DISCORD_URL}", discordUrl)
                .replace("{discord_url}", discordUrl)
                .replace("{TELEGRAM_URL}", telegramUrl)
                .replace("{telegram_url}", telegramUrl);

        if (usePAPI) {
            try {
                processed = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, processed);
            } catch (NoClassDefFoundError ignored) {
            }
        }

        return processed;
    }

    private TagResolver buildTagResolver(Player player, String serverName, String discordUrl, String telegramUrl) {
        return TagResolver.resolver(
                Placeholder.component("player", Component.text(player.getName())),
                Placeholder.component("displayname", player.displayName()),
                Placeholder.component("server", Component.text(serverName)),
                Placeholder.component("online", Component.text(String.valueOf(Bukkit.getOnlinePlayers().size()))),
                Placeholder.component("max", Component.text(String.valueOf(Bukkit.getMaxPlayers()))),
                Placeholder.component("discord_url", Component.text(discordUrl)),
                Placeholder.component("telegram_url", Component.text(telegramUrl))
        );
    }
}