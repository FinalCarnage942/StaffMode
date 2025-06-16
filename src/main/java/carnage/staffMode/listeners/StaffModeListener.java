package carnage.staffMode.listeners;

import carnage.staffMode.StaffMode;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class StaffModeListener implements Listener {

    private final StaffMode plugin;

    public StaffModeListener(StaffMode plugin) {
        this.plugin = plugin;

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (plugin.getStaffModePlayers().contains(p.getUniqueId())) {
                        boolean vanished = plugin.getVanishedPlayers().contains(p.getUniqueId());
                        Component status = Component.text("Vanish: ", NamedTextColor.YELLOW)
                                .append(Component.text(vanished ? "✅" : "❌", vanished ? NamedTextColor.GREEN : NamedTextColor.RED));
                        p.sendActionBar(status);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;

        Player player = e.getPlayer();
        if (!plugin.getStaffModePlayers().contains(player.getUniqueId())) return;

        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.ENDER_EYE) return;

        switch (e.getAction()) {
            case RIGHT_CLICK_AIR, RIGHT_CLICK_BLOCK -> vanish(player);
            case LEFT_CLICK_AIR, LEFT_CLICK_BLOCK -> unvanish(player);
        }

        e.setCancelled(true);
    }

    private void vanish(Player player) {
        UUID uuid = player.getUniqueId();
        if (plugin.getVanishedPlayers().add(uuid)) {
            for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                if (!otherPlayer.hasPermission("staffmode.use")) {
                    otherPlayer.hidePlayer(plugin, player);
                }
            }
            player.setInvisible(true);
            player.sendMessage(Component.text("You are now vanished.", NamedTextColor.GREEN));
        }
    }

    private void unvanish(Player player) {
        UUID uuid = player.getUniqueId();
        if (plugin.getVanishedPlayers().remove(uuid)) {
            for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
                otherPlayer.showPlayer(plugin, player);
            }
            player.setInvisible(false);
            player.sendMessage(Component.text("You are now visible.", NamedTextColor.RED));
        }
    }
}
