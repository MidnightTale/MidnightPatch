package fun.mntale.midnightPatch.chunk.block;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.Sound;
import org.bukkit.Particle;
import org.bukkit.World;
import org.jspecify.annotations.NullMarked;

import fun.mntale.midnightPatch.MidnightPatch;

import org.bukkit.plugin.Plugin;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;

import java.util.concurrent.ThreadLocalRandom;

@NullMarked
public class BoneMealManager implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isValidBoneMealUse(event)) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock == null) {
            return;
        }

        GrowthType growthType = GrowthType.fromMaterial(clickedBlock.getType());
        if (growthType == null) {
            return;
        }

        if (growthType.attemptGrowth(clickedBlock, MidnightPatch.instance)) {
            handleSuccessfulGrowth(event);
        }
    }

    private boolean isValidBoneMealUse(PlayerInteractEvent event) {
        return event.getAction() == Action.RIGHT_CLICK_BLOCK &&
               event.getItem() != null &&
               event.getItem().getType() == Material.BONE_MEAL &&
               event.getHand() == org.bukkit.inventory.EquipmentSlot.HAND;
    }

    private void handleSuccessfulGrowth(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        
        // Consume bone meal
        if (player.getGameMode() != org.bukkit.GameMode.CREATIVE) {
            event.getItem().setAmount(event.getItem().getAmount() - 1);
        }

        // Play effects
        World world = player.getWorld();
        world.playSound(block.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0f, 1.0f);
        world.spawnParticle(Particle.HAPPY_VILLAGER, block.getLocation().add(0.5, 0.5, 0.5), 15, 0.5, 0.5, 0.5);
    }

    private enum GrowthType {
        SUGAR_CANE(Material.SUGAR_CANE, 0.45, new SugarCaneGrowth()),
        CACTUS(Material.CACTUS, 0.40, new CactusGrowth()),
        KELP(Material.KELP, 0.30, new KelpGrowth()),
        KELP_PLANT(Material.KELP_PLANT, 0.30, new KelpGrowth()),
        TWISTING_VINES(Material.TWISTING_VINES, 0.25, new VineGrowth(true)),
        WEEPING_VINES(Material.WEEPING_VINES, 0.25, new VineGrowth(false)),
        // Bedrock Edition flower spreading
        DANDELION(Material.DANDELION, 0.60, new FlowerGrowth()),
        POPPY(Material.POPPY, 0.60, new FlowerGrowth()),
        BLUE_ORCHID(Material.BLUE_ORCHID, 0.60, new FlowerGrowth()),
        ALLIUM(Material.ALLIUM, 0.60, new FlowerGrowth()),
        AZURE_BLUET(Material.AZURE_BLUET, 0.60, new FlowerGrowth()),
        RED_TULIP(Material.RED_TULIP, 0.60, new FlowerGrowth()),
        ORANGE_TULIP(Material.ORANGE_TULIP, 0.60, new FlowerGrowth()),
        WHITE_TULIP(Material.WHITE_TULIP, 0.60, new FlowerGrowth()),
        PINK_TULIP(Material.PINK_TULIP, 0.60, new FlowerGrowth()),
        OXEYE_DAISY(Material.OXEYE_DAISY, 0.60, new FlowerGrowth()),
        CORNFLOWER(Material.CORNFLOWER, 0.60, new FlowerGrowth()),
        LILY_OF_THE_VALLEY(Material.LILY_OF_THE_VALLEY, 0.60, new FlowerGrowth()),
        TORCHFLOWER(Material.TORCHFLOWER, 0.60, new FlowerGrowth());

        private final Material material;
        private final double successChance;
        private final GrowthHandler growthHandler;

        GrowthType(Material material, double successChance, GrowthHandler growthHandler) {
            this.material = material;
            this.successChance = successChance;
            this.growthHandler = growthHandler;
        }

        public static GrowthType fromMaterial(Material material) {
            for (GrowthType type : values()) {
                if (type.material == material) {
                    return type;
                }
            }
            return null;
        }

        public boolean attemptGrowth(Block block, Plugin plugin) {
            if (ThreadLocalRandom.current().nextDouble() > successChance) {
                return false;
            }
            return growthHandler.grow(block, plugin);
        }
    }

    private interface GrowthHandler {
        boolean grow(Block block, Plugin plugin);
    }

    private static class SugarCaneGrowth implements GrowthHandler {
        @Override
        public boolean grow(Block block, Plugin plugin) {
            // Find base and count height
            Block base = findBase(block);
            int height = countHeight(base);
            
            if (height >= 3) {
                return false; // Already at max height
            }

            // Grow to max height
            int toGrow = 3 - height;
            Block top = base.getRelative(BlockFace.UP, height - 1);
            
            for (int i = 0; i < toGrow; i++) {
                Block next = top.getRelative(BlockFace.UP, i + 1);
                if (next.getType() != Material.AIR) {
                    return false; // Obstruction
                }
                
                final Block finalNext = next;
                FoliaScheduler.getRegionScheduler().execute(plugin, finalNext.getLocation(), () -> {
                    finalNext.setType(Material.SUGAR_CANE);
                });
            }
            
            return true;
        }

        private Block findBase(Block block) {
            Block current = block;
            while (current.getRelative(BlockFace.DOWN).getType() == Material.SUGAR_CANE) {
                current = current.getRelative(BlockFace.DOWN);
            }
            return current;
        }

        private int countHeight(Block base) {
            int height = 0;
            Block current = base;
            while (current.getType() == Material.SUGAR_CANE) {
                height++;
                current = current.getRelative(BlockFace.UP);
            }
            return height;
        }
    }

    private static class CactusGrowth implements GrowthHandler {
        @Override
        public boolean grow(Block block, Plugin plugin) {
            Block top = findTop(block);
            int worldHeight = block.getWorld().getMaxHeight();
            int growthAmount = ThreadLocalRandom.current().nextInt(1, 3);
            int grown = 0;

            for (int i = 1; i <= growthAmount; i++) {
                Block next = top.getRelative(BlockFace.UP, i);
                
                if (next.getY() >= worldHeight || next.getType() != Material.AIR) {
                    break;
                }

                if (hasAdjacentBlocks(next)) {
                    break;
                }

                final Block finalNext = next;
                FoliaScheduler.getRegionScheduler().execute(plugin, finalNext.getLocation(), () -> {
                    finalNext.setType(Material.CACTUS);
                });
                grown++;
            }

            return grown > 0;
        }

        private Block findTop(Block block) {
            Block current = block;
            while (current.getRelative(BlockFace.UP).getType() == Material.CACTUS) {
                current = current.getRelative(BlockFace.UP);
            }
            return current;
        }

        private boolean hasAdjacentBlocks(Block block) {
            BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};
            for (BlockFace face : faces) {
                if (block.getRelative(face).getType().isSolid()) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class KelpGrowth implements GrowthHandler {
        @Override
        public boolean grow(Block block, Plugin plugin) {
            Block top = findTop(block);
            Block above = top.getRelative(BlockFace.UP);
            
            if (above.getType() == Material.WATER) {
                final Block finalAbove = above;
                FoliaScheduler.getRegionScheduler().execute(plugin, finalAbove.getLocation(), () -> {
                    finalAbove.setType(Material.KELP);
                });
                return true;
            }
            
            return false;
        }

        private Block findTop(Block block) {
            Block current = block;
            while (current.getRelative(BlockFace.UP).getType() == Material.KELP_PLANT || 
                   current.getRelative(BlockFace.UP).getType() == Material.KELP) {
                current = current.getRelative(BlockFace.UP);
            }
            return current;
        }
    }

    private static class VineGrowth implements GrowthHandler {
        private final boolean isTwisting;

        public VineGrowth(boolean isTwisting) {
            this.isTwisting = isTwisting;
        }

        @Override
        public boolean grow(Block block, Plugin plugin) {
            BlockFace direction = isTwisting ? BlockFace.UP : BlockFace.DOWN;
            Block target = block.getRelative(direction);
            
            if (target.getType() == Material.AIR) {
                final Block finalTarget = target;
                final Material vineType = isTwisting ? Material.TWISTING_VINES : Material.WEEPING_VINES;
                
                FoliaScheduler.getRegionScheduler().execute(plugin, finalTarget.getLocation(), () -> {
                    finalTarget.setType(vineType);
                });
                return true;
            }
            
            return false;
        }
    }

    private static class FlowerGrowth implements GrowthHandler {
        @Override
        public boolean grow(Block block, Plugin plugin) {
            // In Bedrock Edition, bone meal on flowers generates flowers based on biome gradients
            // rather than just spreading the same flower type
            int flowersPlaced = 0;
            
            // Try to place 2-4 flowers in nearby locations
            int attempts = ThreadLocalRandom.current().nextInt(2, 5);
            
            for (int i = 0; i < attempts; i++) {
                // Random offset within 3x3 area around the original flower
                int offsetX = ThreadLocalRandom.current().nextInt(-2, 3);
                int offsetZ = ThreadLocalRandom.current().nextInt(-2, 3);
                
                Block targetBlock = block.getRelative(offsetX, 0, offsetZ);
                
                // Check if the target location is suitable for flower placement
                if (canPlaceFlower(targetBlock)) {
                    // Generate flower based on biome gradient
                    Material flowerType = getBiomeFlower(block.getLocation());
                    
                    if (flowerType != null) {
                        final Block finalTarget = targetBlock;
                        final Material finalFlowerType = flowerType;
                        
                        FoliaScheduler.getRegionScheduler().execute(plugin, finalTarget.getLocation(), () -> {
                            finalTarget.setType(finalFlowerType);
                        });
                        flowersPlaced++;
                    }
                }
            }
            
            return flowersPlaced > 0;
        }
        
        private Material getBiomeFlower(org.bukkit.Location location) {
            // Bedrock Edition flower gradient based on biome
            // This is a simplified version - in real Bedrock it's more complex with exact gradients
            double random = ThreadLocalRandom.current().nextDouble();
            
            // Meadow biome gradient (most common)
            if (random < 0.125) return Material.ALLIUM;
            if (random < 0.250) return Material.POPPY;
            if (random < 0.375) return Material.CORNFLOWER;
            if (random < 0.500) return Material.OXEYE_DAISY;
            if (random < 0.625) return Material.AZURE_BLUET;
            if (random < 0.750) return Material.DANDELION;
            if (random < 0.875) return null; // Short grass (handled separately)
            
            return null; // Nothing
        }
        
        private boolean canPlaceFlower(Block block) {
            // Check if the block is air and the block below is suitable for flowers
            if (block.getType() != Material.AIR) {
                return false;
            }
            
            Block below = block.getRelative(BlockFace.DOWN);
            Material belowType = below.getType();
            
            // Flowers can grow on these blocks
            return belowType == Material.GRASS_BLOCK ||
                   belowType == Material.DIRT ||
                   belowType == Material.COARSE_DIRT ||
                   belowType == Material.ROOTED_DIRT ||
                   belowType == Material.FARMLAND ||
                   belowType == Material.PODZOL ||
                   belowType == Material.MYCELIUM ||
                   belowType == Material.MOSS_BLOCK ||
                   belowType == Material.MUD ||
                   belowType == Material.MUDDY_MANGROVE_ROOTS;
        }
    }
} 