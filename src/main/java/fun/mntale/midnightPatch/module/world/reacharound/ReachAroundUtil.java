package fun.mntale.midnightPatch.module.world.reacharound;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.util.RayTraceResult;

public class ReachAroundUtil {
    public static Location getPlayerReachAroundTarget(Player player) {
        RayTraceResult rayTraceResult = player.getWorld().rayTraceBlocks(
            player.getEyeLocation(),
            player.getEyeLocation().getDirection(),
            5
        );
        if (rayTraceResult != null && rayTraceResult.getHitBlock() != null) {
            Block hitBlock = rayTraceResult.getHitBlock();
            BlockFace hitFace = rayTraceResult.getHitBlockFace();
            if (hitFace != null && hitBlock.getType().isSolid()) {
                Block adjacentBlock = hitBlock.getRelative(hitFace);
                if (adjacentBlock.getType().isAir()) {
                    return null;
                }
            }
        }
        Location target = getPlayerVerticalReachAround(player);
        if (target == null) {
            target = getPlayerHorizontalReachAround(player);
        }
        return target;
    }

    public static Location getPlayerVerticalReachAround(Player player) {
        Vector vec = new Vector(0, 0.5, 0);
        RayTraceResult rayTrace = player.getWorld().rayTraceBlocks(
            player.getEyeLocation(),
            player.getEyeLocation().getDirection().clone().add(vec),
            5
        );
        if (rayTrace != null && rayTrace.getHitBlock() != null) {
            Location block = rayTrace.getHitBlock().getLocation();
            Location playerLoc = player.getLocation();
            if (playerLoc.getZ() - block.getZ() < 1.3 &&
                playerLoc.getY() - block.getY() == 1 &&
                playerLoc.getX() - block.getX() < 1.3) {
                Location target = block.subtract(0, 1, 0);
                if (target.getBlock().getType() == Material.AIR) {
                    return target;
                }
            }
        }
        return null;
    }

    public static Location getPlayerHorizontalReachAround(Player player) {
        Location playerLoc = player.getLocation();
        BlockFace facing = player.getFacing();
        Vector direction = player.getEyeLocation().getDirection();
        Vector vec = new Vector(0.5 * facing.getModX(), 0, 0.5 * facing.getModZ());
        RayTraceResult rayTrace = player.getWorld().rayTraceBlocks(
            player.getEyeLocation(),
            direction.clone().subtract(vec),
            4
        );
        if (rayTrace != null && rayTrace.getHitBlock() != null) {
            Location loc = rayTrace.getHitBlock().getLocation();
            double distance = (playerLoc.getX() - loc.getX()) + (playerLoc.getY() - loc.getY()) + (playerLoc.getZ() - loc.getZ()) / 3;
            if (distance < 1.85 && distance > 1.3) {
                Block target = loc.getBlock().getRelative(player.getFacing());
                if (target.getType() == Material.AIR) {
                    return target.getLocation();
                }
            }
        }
        return null;
    }

    public static BlockFace getPlayerFacingDirection(Player player) {
        float yaw = player.getLocation().getYaw();
        if (yaw < 0) yaw += 360;
        if (yaw >= 315 || yaw < 45) {
            return BlockFace.SOUTH;
        } else if (yaw >= 45 && yaw < 135) {
            return BlockFace.WEST;
        } else if (yaw >= 135 && yaw < 225) {
            return BlockFace.NORTH;
        } else {
            return BlockFace.EAST;
        }
    }

    public static org.bukkit.Sound getBlockPlaceSound(Material blockType) {
        String materialName = blockType.name();
        if (materialName.contains("WOOD") || materialName.contains("LOG") || materialName.contains("PLANKS") ||
            materialName.contains("FENCE") || materialName.contains("STAIRS") || materialName.contains("SLAB") ||
            materialName.contains("TRAPDOOR") || materialName.contains("DOOR") || materialName.contains("SIGN")) {
            return org.bukkit.Sound.BLOCK_WOOD_PLACE;
        }
        if (materialName.contains("STONE") || materialName.contains("COBBLESTONE") || materialName.contains("BRICKS") ||
            materialName.contains("CONCRETE") || materialName.contains("TERRACOTTA")) {
            return org.bukkit.Sound.BLOCK_STONE_PLACE;
        }
        if (materialName.contains("GLASS") || materialName.contains("ICE")) {
            return org.bukkit.Sound.BLOCK_GLASS_PLACE;
        }
        if (materialName.contains("IRON") || materialName.contains("GOLD") || materialName.contains("DIAMOND") ||
            materialName.contains("EMERALD") || materialName.contains("LAPIS") || materialName.contains("REDSTONE") ||
            materialName.contains("COAL") || materialName.contains("QUARTZ")) {
            return org.bukkit.Sound.BLOCK_METAL_PLACE;
        }
        if (materialName.contains("DIRT") || materialName.contains("GRASS") || materialName.contains("SAND") ||
            materialName.contains("GRAVEL") || materialName.contains("CLAY")) {
            return org.bukkit.Sound.BLOCK_GRAVEL_PLACE;
        }
        if (materialName.contains("WOOL") || materialName.contains("CARPET")) {
            return org.bukkit.Sound.BLOCK_WOOL_PLACE;
        }
        if (materialName.contains("NETHER") || materialName.contains("SOUL") || materialName.contains("BASALT")) {
            return org.bukkit.Sound.BLOCK_NETHERRACK_PLACE;
        }
        return org.bukkit.Sound.BLOCK_STONE_PLACE;
    }

    public static boolean isBedrockPlayer(Player player) {
        String uuid = player.getUniqueId().toString();
        return uuid.startsWith("00000000-0000-0000");
    }
} 