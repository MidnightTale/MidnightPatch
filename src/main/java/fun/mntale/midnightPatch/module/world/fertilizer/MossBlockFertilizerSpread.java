package fun.mntale.midnightPatch.module.world.fertilizer;

import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.Material;
import org.bukkit.block.Block;
import java.util.concurrent.ThreadLocalRandom;

public class MossBlockFertilizerSpread {
    public static void applyMossSpread(Block centerBlock) {
        MidnightPatch.instance.foliaLib.getScheduler().runAtLocation(centerBlock.getLocation(), taskBase -> {
            int maxX = ThreadLocalRandom.current().nextBoolean() ? 2 : 3;
            int maxZ = ThreadLocalRandom.current().nextBoolean() ? 2 : 3;
            for (int x = -maxX; x <= maxX; x++) {
                for (int z = -maxZ; z <= maxZ; z++) {
                    if (Math.abs(x) == maxX && Math.abs(z) == maxZ) {
                        continue;
                    }
                    float chance = (Math.abs(x) == maxX || Math.abs(z) == maxZ) ? 0.75f : 1.0f;
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
                            if (MossBlockFertilizerUtil.canConvertToMoss(currentBlock.getType())) {
                                currentBlock.setType(Material.MOSS_BLOCK);
                                MossBlockFertilizerUtil.placeVegetation(currentBlock);
                                MossBlockFertilizerUtil.playMossEffects(currentBlock);
                            }
                            break;
                        }
                    }
                }
            }
        });
    }
} 