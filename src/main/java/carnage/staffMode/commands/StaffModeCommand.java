package carnage.staffMode.commands;

import carnage.staffMode.StaffMode;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class StaffModeCommand implements CommandExecutor {

    private final StaffMode plugin;

    public StaffModeCommand(StaffMode plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        UUID uuid = player.getUniqueId();
        if (!player.hasPermission("staffmode.use")) {
            player.sendMessage(Component.text("No permission.", NamedTextColor.RED));
            return true;
        }

        Set<UUID> staff = plugin.getStaffModePlayers();
        Set<UUID> vanished = plugin.getVanishedPlayers();
        Map<UUID, ItemStack[]> invs = plugin.getSavedInventories();
        Map<UUID, ItemStack[]> armors = plugin.getSavedArmor();
        Map<UUID, GameMode> gms = plugin.getSavedGameModes();

        if (staff.contains(uuid)) {
            // Disable staff
            staff.remove(uuid);
            vanished.remove(uuid);

            player.sendMessage(Component.text("Staff mode disabled.", NamedTextColor.RED));
            if (invs.containsKey(uuid)) player.getInventory().setContents(invs.remove(uuid));
            if (armors.containsKey(uuid)) player.getInventory().setArmorContents(armors.remove(uuid));
            GameMode last = gms.remove(uuid);
            player.setGameMode(last != null ? last : GameMode.SURVIVAL);

            Bukkit.getOnlinePlayers().forEach(o -> o.showPlayer(plugin, player));
            TabPlayer tp = TabAPI.getInstance().getPlayer(uuid);
            if (tp != null) tp.setTemporaryGroup(null);

        } else {
            // Enable staff
            staff.add(uuid);

            invs.put(uuid, player.getInventory().getContents());
            armors.put(uuid, player.getInventory().getArmorContents());
            gms.put(uuid, player.getGameMode());

            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
            player.setGameMode(GameMode.CREATIVE);
            player.sendMessage(Component.text("Staff mode enabled.", NamedTextColor.GREEN));

            // Vanish tool
            ItemStack vanish = makeTool(Material.ENDER_EYE, "Vanish Tool",
                    "Right click to vanish", "Left click to unvanish");

            // Freeze tool (iron shovel)
            ItemStack freeze = makeTool(Material.IRON_SHOVEL, "Freeze Tool",
                    "Right click a player to freeze", "Right click again to unfreeze");

            player.getInventory().setItem(0, vanish);
            player.getInventory().setItem(1, freeze);

            // Vanish on start
            vanished.add(uuid);
            Bukkit.getOnlinePlayers().stream()
                    .filter(o -> !o.hasPermission("staffmode.use"))
                    .forEach(o -> o.hidePlayer(plugin, player));

            TabPlayer tp = TabAPI.getInstance().getPlayer(uuid);
            if (tp != null) tp.setTemporaryGroup("invisible");
        }

        return true;
    }

    private ItemStack makeTool(Material mat, String name, String lore1, String lore2) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text(lore1, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text(lore2, NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false)
        ));
        item.setItemMeta(meta);
        return item;
    }
}
