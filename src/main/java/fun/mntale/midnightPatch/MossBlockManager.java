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

public class MossBlockManager implements Listener {
    private final MidnightPatch plugin;
    private static final float VEGETATION_CHANCE = 0.6f; // 60% chance for vegetation

    // Vanilla vegetation chances
    private static final float SHORT_GRASS_CHANCE = 0.5208f; // 52.08%
    private static final float MOSS_CARPET_CHANCE = 0.2604f; // 26.04%
    private static final float TALL_GRASS_CHANCE = 0.1042f; // 10.42%
    private static final float AZALEA_CHANCE = 0.0729f; // 7.29%
    private static final float FLOWERING_AZALEA_CHANCE = 0.0417f; // 4.17%

    // Vanilla moss spread chances (7x7 pattern)
    private static final float[][] MOSS_SPREAD_CHANCES = {
        {0.0000f, 0.1875f, 0.3750f, 0.3750f, 0.3750f, 0.1875f, 0.0000f},
        {0.1875f, 0.6250f, 0.8750f, 0.8750f, 0.8750f, 0.6250f, 0.1875f},
        {0.3750f, 0.8750f, 1.0000f, 1.0000f, 1.0000f, 0.8750f, 0.3750f},
        {0.3750f, 0.8750f, 1.0000f, 1.0000f, 1.0000f, 0.8750f, 0.3750f},
        {0.3750f, 0.8750f, 1.0000f, 1.0000f, 1.0000f, 0.8750f, 0.3750f},
        {0.1875f, 0.6250f, 0.8750f, 0.8750f, 0.8750f, 0.6250f, 0.1875f},
        {0.0000f, 0.1875f, 0.3750f, 0.3750f, 0.3750f, 0.1875f, 0.0000f}
    };

    public MossBlockManager(MidnightPatch plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Check if the player right-clicked a block with an item
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getItem() == null || event.getItem().getType() != Material.BONE_MEAL) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null || clickedBlock.getType() != Material.MOSS_BLOCK) {
            return;
        }

        // Cancel the event to prevent vanilla bone meal behavior
        event.setCancelled(true);

        // Check if block above is air
        if (clickedBlock.getRelative(0, 1, 0).getType() != Material.AIR) {
            return;
        }

        applyMossSpread(clickedBlock);

        // Consume one bone meal from the player's hand
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

        // Cancel the event to prevent vanilla bone meal behavior
        event.setCancelled(true);

        // Check if block above is air
        if (targetBlock.getRelative(0, 1, 0).getType() != Material.AIR) {
            return;
        }

        applyMossSpread(targetBlock);
    }

    private void applyMossSpread(Block centerBlock) {
        FoliaScheduler.getRegionScheduler().run(plugin, centerBlock.getLocation(), taskBase -> {
            // Choose random max distances (2 or 3) for X and Z
            int maxX = ThreadLocalRandom.current().nextBoolean() ? 2 : 3;
            int maxZ = ThreadLocalRandom.current().nextBoolean() ? 2 : 3;

            // Process each column in the rectangular area
            for (int x = -maxX; x <= maxX; x++) {
                for (int z = -maxZ; z <= maxZ; z++) {
                    // Get the chance from our pattern (convert to 0-6 range)
                    float chance = MOSS_SPREAD_CHANCES[x + 3][z + 3];

                    // Skip if random check fails
                    if (ThreadLocalRandom.current().nextFloat() >= chance) {
                        continue;
                    }

                    // Get the block above the moss block
                    Block startBlock = centerBlock.getRelative(x, 1, z);
                    
                    // Determine scan direction and distance based on start block
                    boolean scanUp = startBlock.getType() != Material.AIR;
                    int maxScanDistance = scanUp ? 4 : 6;
                    int scanDirection = scanUp ? 1 : -1;

                    // Scan for a valid block to convert
                    Block targetBlock = null;
                    for (int y = 0; y < maxScanDistance; y++) {
                        Block currentBlock = startBlock.getRelative(0, y * scanDirection, 0);
                        if (currentBlock.getRelative(0, 1, 0).getType() == Material.AIR) {
                            targetBlock = currentBlock;
                            break;
                        }
                    }

                    // If we found a valid block, process it
                    if (targetBlock != null) {
                        final Block finalTargetBlock = targetBlock;
                        FoliaScheduler.getRegionScheduler().run(plugin, targetBlock.getLocation(), taskBlock -> {
                            boolean converted = false;

                            // Convert block to moss if possible
                            if (canConvertToMoss(finalTargetBlock.getType())) {
                                finalTargetBlock.setType(Material.MOSS_BLOCK);
                                converted = true;
                            }

                            // Place vegetation if block is or was converted to moss
                            if ((converted || finalTargetBlock.getType() == Material.MOSS_BLOCK) && 
                                ThreadLocalRandom.current().nextFloat() < VEGETATION_CHANCE) {
                                Block vegetationBlock = finalTargetBlock.getRelative(0, 1, 0);
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

                            // Play effects if any changes were made
                            if (converted) {
                                finalTargetBlock.getWorld().playSound(finalTargetBlock.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0f, 0.8f + ThreadLocalRandom.current().nextFloat() * 0.4f);
                                finalTargetBlock.getWorld().spawnParticle(Particle.valueOf("SPORE_BLOSSOM_AIR"), finalTargetBlock.getLocation().add(0.5, 1.0, 0.5), 5, 0.2, 0.2, 0.2, 0.0);
                                finalTargetBlock.getWorld().spawnParticle(Particle.COMPOSTER, finalTargetBlock.getLocation().add(0.5, 1.0, 0.5), 5, 0.2, 0.2, 0.2, 0.0);
                            }
                        });
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
               material == Material.MYCELIUM;
    }
} 