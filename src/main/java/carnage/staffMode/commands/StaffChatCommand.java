package carnage.staffMode.commands;

import carnage.staffMode.StaffMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class StaffChatCommand implements CommandExecutor {

    private final StaffMode plugin;
    private final Set<UUID> staffChatEnabled = new HashSet<>();

    public StaffChatCommand(StaffMode plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        UUID uuid = player.getUniqueId();

        if (!player.hasPermission("staffmode.use")) {
            player.sendMessage("You do not have permission to use this command.");
            return true;
        }

        if (staffChatEnabled.contains(uuid)) {
            staffChatEnabled.remove(uuid);
            player.sendMessage("Staff chat disabled.");
        } else {
            staffChatEnabled.add(uuid);
            player.sendMessage("Staff chat enabled.");
        }

        return true;
    }

    public boolean isStaffChatEnabled(UUID uuid) {
        return staffChatEnabled.contains(uuid);
    }
}
