package fun.mntale.midnightPatch;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class MossBlockManager implements Listener {
    private final MidnightPatch plugin;
    private static final float VEGETATION_CHANCE = 0.6f; // 60% chance for vegetation

    // Vanilla vegetation chances
    private static final float SHORT_GRASS_CHANCE = 0.5208f; // 52.08%
    private static final float MOSS_CARPET_CHANCE = 0.2604f; // 26.04%
    private static final float TALL_GRASS_CHANCE = 0.1042f; // 10.42%
    private static final float AZALEA_CHANCE = 0.0729f; // 7.29%
    private static final float FLOWERING_AZALEA_CHANCE = 0.0417f; // 4.17%


    public MossBlockManager(MidnightPatch plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLogger().info("MossBlockManager initialized");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getItem() == null || event.getItem().getType() != Material.BONE_MEAL) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.MOSS_BLOCK) {
            return;
        }

        event.setCancelled(true);

        if (clickedBlock.getRelative(0, 1, 0).getType() != Material.AIR) {
            return;
        }

        applyMossSpread(clickedBlock);

        if (event.getPlayer().getGameMode() != org.bukkit.GameMode.CREATIVE) {
            event.getItem().setAmount(event.getItem().getAmount() - 1);
        }
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event) {
        // Check if a dispenser is dispensing bone meal
        if (!(event.getBlock().getState() instanceof Dispenser) || event.getItem().getType() != Material.BONE_MEAL) {
            return;
        }

        Dispenser dispenser = (Dispenser) event.getBlock().getState();
        BlockFace facing = ((org.bukkit.block.data.Directional) dispenser.getBlockData()).getFacing();
        Block targetBlock = event.getBlock().getRelative(facing);

        if (targetBlock.getType() != Material.MOSS_BLOCK) {
            return;
        }

        event.setCancelled(true);

        if (targetBlock.getRelative(0, 1, 0).getType() != Material.AIR) {
            return;
        }

        applyMossSpread(targetBlock);
    }

    private void applyMossSpread(Block centerBlock) {
        FoliaScheduler.getRegionScheduler().run(plugin, centerBlock.getLocation(), taskBase -> {
            int maxX = ThreadLocalRandom.current().nextBoolean() ? 2 : 3;
            int maxZ = ThreadLocalRandom.current().nextBoolean() ? 2 : 3;

            for (int x = -maxX; x <= maxX; x++) {
                for (int z = -maxZ; z <= maxZ; z++) {
                    if (Math.abs(x) == maxX && Math.abs(z) == maxZ) {
                        continue;
                    }

                    float chance;
                    if (Math.abs(x) == maxX || Math.abs(z) == maxZ) {
                        chance = 0.75f;
                    } else {
                        chance = 1.0f; 
                    }

                    if (ThreadLocalRandom.current().nextFloat() >= chance) {
                        continue;
                    }

                    Block startBlock = centerBlock.getRelative(x, 0, z);
                    
                    boolean scanUp = startBlock.getRelative(0, 1, 0).getType() != Material.AIR;
                    int maxScanDistance = scanUp ? 5 : 7;
                    int scanDirection = scanUp ? 1 : -1;

                    for (int y = 0; y < maxScanDistance; y++) {
                        Block currentBlock = startBlock.getRelative(0, y * scanDirection, 0);
                        
                        if (currentBlock.getRelative(0, 1, 0).getType() == Material.AIR) {
                            if (canConvertToMoss(currentBlock.getType())) {
                                currentBlock.setType(Material.MOSS_BLOCK);

                                if (ThreadLocalRandom.current().nextFloat() < VEGETATION_CHANCE) {
                                    Block vegetationBlock = currentBlock.getRelative(0, 1, 0);
                                    float vegetationRoll = ThreadLocalRandom.current().nextFloat();
                                    
                                    if (vegetationRoll < SHORT_GRASS_CHANCE) {
                                        vegetationBlock.setType(Material.SHORT_GRASS);
                                    } else if (vegetationRoll < SHORT_GRASS_CHANCE + MOSS_CARPET_CHANCE) {
                                        vegetationBlock.setType(Material.MOSS_CARPET);
                                    } else if (vegetationRoll < SHORT_GRASS_CHANCE + MOSS_CARPET_CHANCE + TALL_GRASS_CHANCE) {
                                        vegetationBlock.setType(Material.TALL_GRASS);
                                    } else if (vegetationRoll < SHORT_GRASS_CHANCE + MOSS_CARPET_CHANCE + TALL_GRASS_CHANCE + AZALEA_CHANCE) {
                                        vegetationBlock.setType(Material.AZALEA);
                                    } else if (vegetationRoll < SHORT_GRASS_CHANCE + MOSS_CARPET_CHANCE + TALL_GRASS_CHANCE + AZALEA_CHANCE + FLOWERING_AZALEA_CHANCE) {
                                        vegetationBlock.setType(Material.FLOWERING_AZALEA);
                                    }
                                }

                                currentBlock.getWorld().playSound(currentBlock.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0f, 0.8f + ThreadLocalRandom.current().nextFloat() * 0.4f);
                                currentBlock.getWorld().spawnParticle(Particle.valueOf("SPORE_BLOSSOM_AIR"), currentBlock.getLocation().add(0.5, 1.0, 0.5), 5, 0.2, 0.2, 0.2, 0.0);
                                currentBlock.getWorld().spawnParticle(Particle.COMPOSTER, currentBlock.getLocation().add(0.5, 1.0, 0.5), 5, 0.2, 0.2, 0.2, 0.0);
                            }
                            break;
                        }
                    }
                }
            }
        });
    }

    private boolean canConvertToMoss(Material material) {
        return material == Material.STONE ||
               material == Material.DEEPSLATE ||
               material == Material.DIRT ||
               material == Material.GRASS_BLOCK ||
               material == Material.PODZOL ||
               material == Material.MYCELIUM ||
               material == Material.ANDESITE ||
               material == Material.COARSE_DIRT ||
               material == Material.DIORITE ||
               material == Material.GRANITE ||
               material == Material.MUD ||
               material == Material.MUDDY_MANGROVE_ROOTS ||
               material == Material.ROOTED_DIRT ||
               material == Material.TUFF ||
               material == Material.MOSS_BLOCK;
    }
} 