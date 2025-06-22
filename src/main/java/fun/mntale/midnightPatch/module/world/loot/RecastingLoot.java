package fun.mntale.midnightPatch.module.world.loot;

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

public class RecastingLoot implements Listener {
    private static final NamespacedKey RECASTING_KEY = NamespacedKey.fromString("midnightpatch:recasting");
    private static final org.bukkit.enchantments.Enchantment RECASTING_ENCHANT = io.papermc.paper.registry.RegistryAccess.registryAccess()
        .getRegistry(io.papermc.paper.registry.RegistryKey.ENCHANTMENT)
        .get(RECASTING_KEY);
    private static final String UNDERWATER_RUIN_BIG = "minecraft:chests/underwater_ruin_big";
    private static final String UNDERWATER_RUIN_SMALL = "minecraft:chests/underwater_ruin_small";
    private static final String BURIED_TREASURE = "minecraft:chests/buried_treasure";
    private static final String SHIPWRECK_TREASURE = "minecraft:chests/shipwreck_treasure";
    private static final String SHIPWRECK_SUPPLY = "minecraft:chests/shipwreck_supply";
    private static final String SHIPWRECK_MAP = "minecraft:chests/shipwreck_map";
    private final Set<Location> recastingChests = new HashSet<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!fun.mntale.midnightPatch.MidnightPatch.instance.getConfig().getBoolean("enableRecastingLoot", true)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        BlockState state = block.getState();
        if (!(state instanceof Lootable lootable)) return;
        if (lootable.getLootTable() == null) return;
        String lootTableKey = lootable.getLootTable().getKey().toString();
        if (!UNDERWATER_RUIN_BIG.equals(lootTableKey) &&
            !UNDERWATER_RUIN_SMALL.equals(lootTableKey) &&
            !BURIED_TREASURE.equals(lootTableKey) &&
            !SHIPWRECK_TREASURE.equals(lootTableKey) &&
            !SHIPWRECK_SUPPLY.equals(lootTableKey) &&
            !SHIPWRECK_MAP.equals(lootTableKey)) return;
        recastingChests.add(block.getLocation());
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!fun.mntale.midnightPatch.MidnightPatch.instance.getConfig().getBoolean("enableRecastingLoot", true)) return;
        Location loc = event.getInventory().getLocation();
        if (loc == null) return;
        if (!recastingChests.remove(loc)) return;
        if (RECASTING_ENCHANT == null) return;
        FoliaScheduler.getRegionScheduler().run(MidnightPatch.instance, loc, (task) -> {
            if (ThreadLocalRandom.current().nextDouble() < 0.20) {
                ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                book.addUnsafeEnchantment(RECASTING_ENCHANT, 1);
                event.getInventory().addItem(book);
            }
        });
    }
} 