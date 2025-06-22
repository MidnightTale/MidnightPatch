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

public class FrostbiteLoot implements Listener {
    private static final NamespacedKey FROSTBITE_KEY = NamespacedKey.fromString("midnightpatch:frostbite");
    private static final org.bukkit.enchantments.Enchantment FROSTBITE_ENCHANT = io.papermc.paper.registry.RegistryAccess.registryAccess()
        .getRegistry(io.papermc.paper.registry.RegistryKey.ENCHANTMENT)
        .get(FROSTBITE_KEY);
    private static final String ANCIENT_CITY = "minecraft:chests/ancient_city";
    private static final String IGLOO_CHEST = "minecraft:chests/igloo_chest";
    private static final String ICE_SPIKES = "minecraft:chests/ice_spikes";
    private final Set<Location> frostbiteChests = new HashSet<>();

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!fun.mntale.midnightPatch.MidnightPatch.instance.getConfig().getBoolean("enableFrostbiteLoot", true)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        BlockState state = block.getState();
        if (!(state instanceof Lootable lootable)) return;
        if (lootable.getLootTable() == null) return;
        String lootTableKey = lootable.getLootTable().getKey().toString();
        if (!ANCIENT_CITY.equals(lootTableKey) &&
            !IGLOO_CHEST.equals(lootTableKey) &&
            !ICE_SPIKES.equals(lootTableKey)) return;
        frostbiteChests.add(block.getLocation());
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!fun.mntale.midnightPatch.MidnightPatch.instance.getConfig().getBoolean("enableFrostbiteLoot", true)) return;
        Location loc = event.getInventory().getLocation();
        if (loc == null) return;
        if (!frostbiteChests.remove(loc)) return;
        if (FROSTBITE_ENCHANT == null) return;
        FoliaScheduler.getRegionScheduler().run(MidnightPatch.instance, loc, (task) -> {
            if (ThreadLocalRandom.current().nextDouble() < 0.20) {
                ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
                book.addUnsafeEnchantment(FROSTBITE_ENCHANT, 1);
                event.getInventory().addItem(book);
            }
        });
    }
} 