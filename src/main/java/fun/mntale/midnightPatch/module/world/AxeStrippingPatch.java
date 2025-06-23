package fun.mntale.midnightPatch.module.world;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class AxeStrippingPatch implements Listener {
    private static final Map<Material, Material> STRIPPABLES = new HashMap<>();
    static {
        // LOGS
        STRIPPABLES.put(Material.OAK_LOG, Material.STRIPPED_OAK_LOG);
        STRIPPABLES.put(Material.SPRUCE_LOG, Material.STRIPPED_SPRUCE_LOG);
        STRIPPABLES.put(Material.BIRCH_LOG, Material.STRIPPED_BIRCH_LOG);
        STRIPPABLES.put(Material.JUNGLE_LOG, Material.STRIPPED_JUNGLE_LOG);
        STRIPPABLES.put(Material.ACACIA_LOG, Material.STRIPPED_ACACIA_LOG);
        STRIPPABLES.put(Material.DARK_OAK_LOG, Material.STRIPPED_DARK_OAK_LOG);
        STRIPPABLES.put(Material.MANGROVE_LOG, Material.STRIPPED_MANGROVE_LOG);
        STRIPPABLES.put(Material.CHERRY_LOG, Material.STRIPPED_CHERRY_LOG);
        STRIPPABLES.put(Material.BAMBOO_BLOCK, Material.STRIPPED_BAMBOO_BLOCK);
        STRIPPABLES.put(Material.CRIMSON_STEM, Material.STRIPPED_CRIMSON_STEM);
        STRIPPABLES.put(Material.WARPED_STEM, Material.STRIPPED_WARPED_STEM);
        STRIPPABLES.put(Material.PALE_OAK_LOG, Material.STRIPPED_PALE_OAK_LOG);
        STRIPPABLES.put(Material.OAK_WOOD, Material.STRIPPED_OAK_WOOD);
        STRIPPABLES.put(Material.SPRUCE_WOOD, Material.STRIPPED_SPRUCE_WOOD);
        STRIPPABLES.put(Material.BIRCH_WOOD, Material.STRIPPED_BIRCH_WOOD);
        STRIPPABLES.put(Material.JUNGLE_WOOD, Material.STRIPPED_JUNGLE_WOOD);
        STRIPPABLES.put(Material.ACACIA_WOOD, Material.STRIPPED_ACACIA_WOOD);
        STRIPPABLES.put(Material.DARK_OAK_WOOD, Material.STRIPPED_DARK_OAK_WOOD);
        STRIPPABLES.put(Material.MANGROVE_WOOD, Material.STRIPPED_MANGROVE_WOOD);
        STRIPPABLES.put(Material.CHERRY_WOOD, Material.STRIPPED_CHERRY_WOOD);
        STRIPPABLES.put(Material.CRIMSON_HYPHAE, Material.STRIPPED_CRIMSON_HYPHAE);
        STRIPPABLES.put(Material.WARPED_HYPHAE, Material.STRIPPED_WARPED_HYPHAE);
        STRIPPABLES.put(Material.PALE_OAK_WOOD, Material.STRIPPED_PALE_OAK_WOOD);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !item.getType().toString().endsWith("_AXE")) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        Material stripped = STRIPPABLES.get(block.getType());
        if (stripped == null) return;
        if (!event.isCancelled()) return; 
        BlockData oldData = block.getBlockData();
        block.setType(stripped);
        BlockData newData = block.getBlockData();
        if (oldData instanceof Orientable && newData instanceof Orientable) {
            ((Orientable) newData).setAxis(((Orientable) oldData).getAxis());
            block.setBlockData(newData);
        }
        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            item.setDurability((short) (item.getDurability() + 1));
        }
        player.getWorld().playSound(block.getLocation(), "block.wood.strip", 1.0f, 1.0f);
        event.setCancelled(false);
    }
} 