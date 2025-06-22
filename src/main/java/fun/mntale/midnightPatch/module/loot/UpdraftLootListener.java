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

public class UpdraftLootListener implements Listener {
    private static final NamespacedKey UPDRAFT_KEY = NamespacedKey.fromString("midnightpatch:updraft");
    private static final org.bukkit.enchantments.Enchantment UPDRAFT_ENCHANT = io.papermc.paper.registry.RegistryAccess.registryAccess()
        .getRegistry(io.papermc.paper.registry.RegistryKey.ENCHANTMENT)
        .get(UPDRAFT_KEY);
    private static final String JUNGLE_TEMPLE = "minecraft:chests/jungle_temple";
    private static final String STRONGHOLD_LIBRARY = "minecraft:chests/stronghold_library";
    private static final String ANCIENT_CITY = "minecraft:chests/ancient_city";
    private static final String WOODLAND_MANSION = "minecraft:chests/woodland_mansion";
    private final Set<Location> jungleTempleChests = new HashSet<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!MidnightPatch.instance.getConfig().getBoolean("enableUpdraftLoot", true)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        BlockState state = block.getState();
        if (!(state instanceof Lootable lootable)) return;
        if (lootable.getLootTable() == null) return;
        String lootTableKey = lootable.getLootTable().getKey().toString();
        if (!JUNGLE_TEMPLE.equals(lootTableKey) &&
            !STRONGHOLD_LIBRARY.equals(lootTableKey) &&
            !ANCIENT_CITY.equals(lootTableKey) &&
            !WOODLAND_MANSION.equals(lootTableKey)) return;
        // Mark this chest location
        jungleTempleChests.add(block.getLocation());
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!MidnightPatch.instance.getConfig().getBoolean("enableUpdraftLoot", true)) return;
        Location loc = event.getInventory().getLocation();
        if (loc == null) return;
        if (!jungleTempleChests.remove(loc)) return; // Only proceed if this was flagged
        if (UPDRAFT_ENCHANT == null) return;
        FoliaScheduler.getRegionScheduler().run(MidnightPatch.instance, loc, (task) -> {
            if (ThreadLocalRandom.current().nextDouble() < 0.20) {
                ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                book.addUnsafeEnchantment(UPDRAFT_ENCHANT, 1);
                event.getInventory().addItem(book);
            }
        });
    }
} 