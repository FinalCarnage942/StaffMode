package carnage.staffMode.listeners;

import carnage.staffMode.StaffMode;
import nl.marido.deluxecombat.api.DeluxeCombatAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class CombatListener implements Listener {

    private final DeluxeCombatAPI combatAPI;
    private final StaffMode plugin;

    public CombatListener(StaffMode plugin) {
        this.plugin = plugin;
        this.combatAPI = new DeluxeCombatAPI();
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player p)) return;
        if (!plugin.getStaffModePlayers().contains(p.getUniqueId())) return;

        // Cancel damage and tag in combat
        e.setCancelled(true);
        combatAPI.togglePvP(p, true);
    }
}
