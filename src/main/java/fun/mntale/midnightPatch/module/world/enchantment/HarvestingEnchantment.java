package fun.mntale.midnightPatch.module.world.enchantment;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import fun.mntale.midnightPatch.MidnightPatch;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Objects;

public class HarvestingEnchantment implements Listener {
    private static final Map<Material, Material> CROP_TO_SEED = new ConcurrentHashMap<>();
    static {
        CROP_TO_SEED.put(Material.WHEAT, Material.WHEAT_SEEDS);
        CROP_TO_SEED.put(Material.CARROTS, Material.CARROT);
        CROP_TO_SEED.put(Material.POTATOES, Material.POTATO);
        CROP_TO_SEED.put(Material.BEETROOTS, Material.BEETROOT_SEEDS);
        CROP_TO_SEED.put(Material.NETHER_WART, Material.NETHER_WART);
    }

    private static final NamespacedKey HARVESTING_KEY = NamespacedKey.fromString("midnightpatch:harvesting");
    private static final Enchantment HARVESTING_ENCHANT = io.papermc.paper.registry.RegistryAccess.registryAccess().getRegistry(io.papermc.paper.registry.RegistryKey.ENCHANTMENT).get(Objects.requireNonNull(HARVESTING_KEY));

    @EventHandler
    public void onPlayerUseHoe(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        Player player = event.getPlayer();
        ItemStack hoe = player.getInventory().getItemInMainHand();
        if (!hoe.getType().toString().endsWith("_HOE")) return;
        if (HARVESTING_ENCHANT == null || !hoe.containsEnchantment(HARVESTING_ENCHANT)) return;

        int level = hoe.getEnchantmentLevel(HARVESTING_ENCHANT);

        Block center = event.getClickedBlock();
        if (center == null) {
            return;
        }

        // Range: level 1 = 1x1, level 2 = 3x3, ..., level 5 = 9x9
        int range = level * 2 - 1;
        int half = range / 2;
        MidnightPatch.instance.foliaLib.getScheduler().runAtEntity(player, (task) -> {
            for (int dx = -half; dx <= half; dx++) {
                for (int dz = -half; dz <= half; dz++) {
                    Block block = center.getRelative(dx, 0, dz);
                    Material cropType = block.getType();
                    if (!CROP_TO_SEED.containsKey(cropType)) continue;
                    if (!(block.getBlockData() instanceof Ageable ageable)) continue;
                    if (ageable.getAge() < ageable.getMaximumAge()) continue;
                    block.breakNaturally(hoe);

                    block.setType(cropType);
                    ageable.setAge(0);
                    block.setBlockData(ageable);
                    center.getWorld().playSound(center.getLocation().add(0.5, 0.5, 0.5), org.bukkit.Sound.BLOCK_COMPOSTER_READY, 1.2f, 1.0f);
                    double radius = (range) / 2.0;
                    int points = 36; // every 10 degrees
                    for (int i = 0; i < points; i++) {
                        double angle = 2 * Math.PI * i / points;
                        double x = Math.cos(angle) * radius;
                        double z = Math.sin(angle) * radius;
                        center.getWorld().spawnParticle(org.bukkit.Particle.ELECTRIC_SPARK,
                            center.getLocation().add(x + 0.5, 0.5, z + 0.5),
                            3, 0.1, 0.1, 0.1, 0.01);
                    }
                }
            }
        });

    }
} 