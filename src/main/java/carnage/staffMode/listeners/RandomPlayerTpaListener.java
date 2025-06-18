package carnage.staffMode.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class RandomPlayerTpaListener implements Listener {

    private final Map<UUID, LinkedList<UUID>> lastTeleports = new HashMap<>();
    private final int TELEPORT_HISTORY_LIMIT = 3;

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.getType() == Material.NETHER_STAR &&
                item.hasItemMeta() &&
                item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().equals("Random Teleport")) {

            List<Player> candidates = new ArrayList<>();

            for (Player target : Bukkit.getOnlinePlayers()) {
                if (target.equals(player)) continue; // skip self

                LinkedList<UUID> recent = lastTeleports.getOrDefault(player.getUniqueId(), new LinkedList<>());
                if (!recent.contains(target.getUniqueId())) {
                    candidates.add(target);
                }
            }

            if (candidates.isEmpty()) {
                player.sendMessage("§cNo valid players to teleport to (you've recently teleported to all of them).");
                return;
            }

            Player chosen = candidates.get(new Random().nextInt(candidates.size()));
            player.teleport(chosen.getLocation());
            player.sendMessage("§aYou have been teleported to §e" + chosen.getName());

            // Store in teleport history
            lastTeleports.computeIfAbsent(player.getUniqueId(), k -> new LinkedList<>());
            LinkedList<UUID> history = lastTeleports.get(player.getUniqueId());
            history.addLast(chosen.getUniqueId());

            if (history.size() > TELEPORT_HISTORY_LIMIT) {
                history.removeFirst(); // maintain size
            }
        }
    }
}
