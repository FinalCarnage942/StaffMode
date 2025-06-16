package carnage.staffMode.listeners;

import carnage.staffMode.StaffMode;
import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class VanishListener implements Listener {

    private final StaffMode plugin;

    public VanishListener(StaffMode plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player joined = e.getPlayer();
        for (UUID vanished : plugin.getVanishedPlayers()) {
            Player vanishedPlayer = plugin.getServer().getPlayer(vanished);
            if (vanishedPlayer != null && !joined.hasPermission("staffmode.use")) {
                joined.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        plugin.getVanishedPlayers().remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onAsyncChat(AsyncChatEvent e) {
        Player sender = e.getPlayer();
        if (plugin.getVanishedPlayers().contains(sender.getUniqueId())) {
            e.viewers().removeIf(audience -> {
                if (audience instanceof Player player) {
                    return !player.hasPermission("staffmode.use");
                }
                return false;
            });
        }
    }
}
