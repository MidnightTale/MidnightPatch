package fun.mntale.midnightPatch.module.world.enchantment;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.view.AnvilView;
import java.util.HashMap;
import java.util.Map;

public class RelimitAnvil implements Listener {
    private final Map<AnvilInventory, Integer> realMaxRepairCosts = new HashMap<>();
    private final boolean forceMax = true;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPrepareAnvil(PrepareAnvilEvent e) {
        InventoryView view = e.getView();
        if (!(view instanceof AnvilView anvilView)) {
            return;
        }
        AnvilInventory inv = anvilView.getTopInventory();
        if (anvilView.getMaximumRepairCost() != Integer.MAX_VALUE) {
            realMaxRepairCosts.put(inv, anvilView.getMaximumRepairCost());
        }
        Integer realMax = realMaxRepairCosts.get(inv);
        if (realMax != null && anvilView.getRepairCost() < realMax) {
            realMaxRepairCosts.remove(inv);
            anvilView.setMaximumRepairCost(realMax);
            return;
        }
        anvilView.setMaximumRepairCost(Integer.MAX_VALUE);
        if (realMax != null) {
            anvilView.setRepairCost(forceMax ? 39 : realMax - 1);
        }
    }
} 