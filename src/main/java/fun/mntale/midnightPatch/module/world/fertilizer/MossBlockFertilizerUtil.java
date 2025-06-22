package fun.mntale.midnightPatch.module.world.fertilizer;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.Sound;
import org.bukkit.Particle;
import java.util.concurrent.ThreadLocalRandom;

public class MossBlockFertilizerUtil {
    public static final float VEGETATION_CHANCE = 0.6f;
    public static final float SHORT_GRASS_CHANCE = 0.5208f;
    public static final float MOSS_CARPET_CHANCE = 0.2604f;
    public static final float TALL_GRASS_CHANCE = 0.1042f;
    public static final float AZALEA_CHANCE = 0.0729f;
    public static final float FLOWERING_AZALEA_CHANCE = 0.0417f;

    public static boolean canConvertToMoss(Material material) {
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

    public static void placeVegetation(Block block) {
        if (ThreadLocalRandom.current().nextFloat() < VEGETATION_CHANCE) {
            Block vegetationBlock = block.getRelative(0, 1, 0);
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
    }

    public static void playMossEffects(Block block) {
        block.getWorld().playSound(block.getLocation(), Sound.ITEM_BONE_MEAL_USE, 1.0f, 0.8f + ThreadLocalRandom.current().nextFloat() * 0.4f);
        block.getWorld().spawnParticle(Particle.valueOf("SPORE_BLOSSOM_AIR"), block.getLocation().add(0.5, 1.0, 0.5), 5, 0.2, 0.2, 0.2, 0.0);
        block.getWorld().spawnParticle(Particle.COMPOSTER, block.getLocation().add(0.5, 1.0, 0.5), 5, 0.2, 0.2, 0.2, 0.0);
    }
} 