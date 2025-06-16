package carnage.staffMode.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;

import java.util.Random;

public class RandomPlayerTpaListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        // Check if the item is the one you want to use for random teleportation
        if (item != null && item.getType() == Material.NETHER_STAR && item.getItemMeta().getDisplayName().equals("Random Teleport")) {
            Player[] onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[0]);

            if (onlinePlayers.length > 0) {
                Random random = new Random();
                Player randomPlayer = onlinePlayers[random.nextInt(onlinePlayers.length)];

                // Teleport the player
                player.teleport(randomPlayer.getLocation());
                player.sendMessage("You have been teleported to " + randomPlayer.getName());
            } else {
                player.sendMessage("There are no players online to teleport to.");
            }
        }
    }
}
