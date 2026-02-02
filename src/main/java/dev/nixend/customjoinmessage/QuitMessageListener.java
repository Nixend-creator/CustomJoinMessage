package dev.nixend.customjoinmessage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class QuitMessageListener implements Listener {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final CustomJoinMessagePlugin plugin;
    private final LanguageManager languageManager;

    public QuitMessageListener(CustomJoinMessagePlugin plugin, LanguageManager languageManager) {
        this.plugin = plugin;
        this.languageManager = languageManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        List<String> rawMessages = plugin.getConfig().getStringList("quit-message");

        if (rawMessages.isEmpty()) {
            event.quitMessage(Component.empty());
            return;
        }

        String serverName = plugin.getConfig().getString("server-name", "MyServer");
        String discordUrl = plugin.getConfig().getString("discord-url", "");
        String telegramUrl = plugin.getConfig().getString("telegram-url", "");

        StringBuilder fullMessage = new StringBuilder();
        for (int i = 0; i < rawMessages.size(); i++) {
            String processed = JoinMessageListener.processPlaceholders(
                    rawMessages.get(i), player, serverName, discordUrl, telegramUrl, plugin.isPlaceholderAPIEnabled()
            );
            fullMessage.append(processed);
            if (i < rawMessages.size() - 1) fullMessage.append("\n");
        }

        try {
            Component component = miniMessage.deserialize(
                    fullMessage.toString(),
                    TagResolver.resolver(
                            Placeholder.component("player", Component.text(player.getName())),
                            Placeholder.component("server", Component.text(serverName)),
                            Placeholder.component("online", Component.text(String.valueOf(Bukkit.getOnlinePlayers().size() - 1))),
                            Placeholder.component("max", Component.text(String.valueOf(Bukkit.getMaxPlayers())))
                    )
            );
            event.quitMessage(component);
        } catch (Exception e) {
            plugin.getLogger().warning(languageManager.getMessage("minimessage-error-quit"));
        }
    }
}