package fun.mntale.midnightPatch.entity;

import fun.mntale.midnightPatch.MidnightPatch;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.loot.Lootable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.ItemStack;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Location;

public class HarvestingLootListener implements Listener {
    private static final NamespacedKey HARVESTING_KEY = NamespacedKey.fromString("midnightpatch:harvesting");
    private static final org.bukkit.enchantments.Enchantment HARVESTING_ENCHANT = io.papermc.paper.registry.RegistryAccess.registryAccess()
        .getRegistry(io.papermc.paper.registry.RegistryKey.ENCHANTMENT)
        .get(HARVESTING_KEY);
    private static final String MINESHAFT = "minecraft:chests/abandoned_mineshaft";
    private static final String WOODLAND_MANSION = "minecraft:chests/woodland_mansion";
    private static final String STRONGHOLD_LIBRARY = "minecraft:chests/stronghold_library";
    private final Set<Location> harvestingChests = new HashSet<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!fun.mntale.midnightPatch.MidnightPatch.instance.getConfig().getBoolean("enableHarvestingLoot", true)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        BlockState state = block.getState();
        if (!(state instanceof Lootable lootable)) return;
        if (lootable.getLootTable() == null) return;
        String lootTableKey = lootable.getLootTable().getKey().toString();
        if (!MINESHAFT.equals(lootTableKey) &&
            !WOODLAND_MANSION.equals(lootTableKey) &&
            !STRONGHOLD_LIBRARY.equals(lootTableKey)) return;
        harvestingChests.add(block.getLocation());
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!fun.mntale.midnightPatch.MidnightPatch.instance.getConfig().getBoolean("enableHarvestingLoot", true)) return;
        Location loc = event.getInventory().getLocation();
        if (loc == null) return;
        if (!harvestingChests.remove(loc)) return;
        if (HARVESTING_ENCHANT == null) return;
        FoliaScheduler.getRegionScheduler().run(MidnightPatch.instance, loc, (task) -> {
            if (ThreadLocalRandom.current().nextDouble() < 0.20) {
                ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                int level = ThreadLocalRandom.current().nextInt(1, 4); // 1 to 3 inclusive
                book.addUnsafeEnchantment(HARVESTING_ENCHANT, level);
                event.getInventory().addItem(book);
            }
        });
    }
} 