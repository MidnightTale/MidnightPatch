package fun.mntale.midnightPatch.module.world.loot;

import fun.mntale.midnightPatch.MidnightPatch;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.loot.Lootable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.io.File;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

public abstract class AbstractLootHandler implements Listener {
    private final Set<Location> flaggedChests = new HashSet<>();
    private final File dataDir;
    private final ChestLootGenDataManager lootGenDataManager = new ChestLootGenDataManager();

    public AbstractLootHandler() {
        File pluginDir = MidnightPatch.instance.getDataFolder();
        dataDir = new File(pluginDir, "lootgen");
        if (!dataDir.exists()) dataDir.mkdirs();
    }

    protected abstract String[] getLootTableKeys();
    protected abstract ItemStack createLootItem();
    protected boolean shouldAddLoot(Inventory inv) { return true; }
    protected double getChance() { return 0.07; }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        BlockState state = block.getState();
        if (!(state instanceof Lootable lootable)) return;
        if (lootable.getLootTable() == null) return;
        String lootTableKey = lootable.getLootTable().getKey().toString();
        for (String key : getLootTableKeys()) {
            if (key.equals(lootTableKey)) {
                flaggedChests.add(block.getLocation());
                break;
            }
        }
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        Location loc = event.getInventory().getLocation();
        if (loc == null) return;
        if (!flaggedChests.remove(loc)) return;
        if (lootGenDataManager.isChestGenerated(loc)) return; // Already generated
        if (!shouldAddLoot(event.getInventory())) return;
        FoliaScheduler.getRegionScheduler().run(MidnightPatch.instance, loc, (task) -> {
            if (ThreadLocalRandom.current().nextDouble() < getChance()) {
                event.getInventory().addItem(createLootItem());
                lootGenDataManager.markChestGenerated(loc);
            }
        });
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        BlockState state = block.getState();
        if (state instanceof Chest) {
            lootGenDataManager.removeChest(block.getLocation());
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            BlockState state = block.getState();
            if (state instanceof Chest) {
                lootGenDataManager.removeChest(block.getLocation());
            }
        }
    }


    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        for (Block block : event.blockList()) {
            BlockState state = block.getState();
            if (state instanceof Chest) {
                lootGenDataManager.removeChest(block.getLocation());
            }
        }
    }

} 