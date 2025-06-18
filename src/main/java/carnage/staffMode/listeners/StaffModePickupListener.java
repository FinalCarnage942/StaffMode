package carnage.staffMode.listeners;

import carnage.staffMode.StaffMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;

public class StaffModePickupListener implements Listener {

    private final StaffMode plugin;

    public StaffModePickupListener(StaffMode plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (plugin.getStaffModePlayers().contains(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendActionBar(net.kyori.adventure.text.Component.text("You cannot pick up items in Staff Mode", net.kyori.adventure.text.format.NamedTextColor.RED));
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (plugin.getStaffModePlayers().contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);

        }
    }

}
