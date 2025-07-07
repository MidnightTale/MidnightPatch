package fun.mntale.midnightPatch.module.entity.boat;

import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.block.data.Directional;

public class DispenserBoatListener implements Listener {
    private static final Material[] ICE_TYPES = {Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE};

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event) {
        ItemStack item = event.getItem();
        Material mat = item.getType();
        EntityType entityType = getBoatEntityType(mat);
        if (entityType == null) return;
        Block dispenserBlock = event.getBlock();
        if (!(dispenserBlock.getState() instanceof Dispenser dispenser)) return;
        BlockFace facing = ((Directional) dispenserBlock.getBlockData()).getFacing();

        boolean found = false;
        Block iceBlock = null;
        Location spawnLoc = null;
        float yaw = getYawFromFacing(facing);

        if (facing == BlockFace.UP || facing == BlockFace.DOWN) {
            // Up/down logic: look up to 2 air blocks, then for ice-type
            Block current = dispenserBlock.getRelative(facing);
            int airCount = 0;
            int maxAir = 2;
            for (int i = 0; i < 4; i++) {
                Material type = current.getType();
                if (airCount < maxAir && type == Material.AIR) {
                    airCount++;
                    current = current.getRelative(facing);
                    continue;
                }
                if (isIceType(type)) {
                    found = true;
                    iceBlock = current;
                    spawnLoc = iceBlock.getLocation().add(0.5, 1.1, 0.5);
                    break;
                }
                break;
            }
        } else {
            // Horizontal logic: look up to 2 air blocks forward, check below each for ice-type
            Block current = dispenserBlock.getRelative(facing);
            for (int i = 0; i < 2; i++) {
                if (current.getType() == Material.AIR) {
                    Block below = current.getRelative(BlockFace.DOWN);
                    if (isIceType(below.getType())) {
                        found = true;
                        iceBlock = below;
                        spawnLoc = iceBlock.getLocation().add(0.5, 1.1, 0.5);
                        break;
                    }
                    current = current.getRelative(facing);
                } else {
                    break;
                }
            }
        }
        if (!found) return;
        event.setCancelled(true);
        // Folia region scheduler for world/entity access
        Location finalSpawnLoc = spawnLoc;
        MidnightPatch.instance.foliaLib.getScheduler().runAtLocation(finalSpawnLoc, (task) -> {
            finalSpawnLoc.setYaw(yaw);
            Entity entity = dispenserBlock.getWorld().spawnEntity(finalSpawnLoc, entityType);
            entity.setRotation(yaw, 0f);
            // Remove one boat/raft from the dispenser
            ItemStack[] contents = dispenser.getInventory().getContents();
            for (int i = 0; i < contents.length; i++) {
                ItemStack stack = contents[i];
                if (stack != null && stack.getType() == mat && stack.getAmount() > 0) {
                    stack.setAmount(stack.getAmount() - 1);
                    dispenser.getInventory().setItem(i, stack.getAmount() > 0 ? stack : null);
                    break;
                }
            }
        });
    }

    private EntityType getBoatEntityType(Material mat) {
        try {
            return EntityType.valueOf(mat.name());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private boolean isIceType(Material mat) {
        for (Material ice : ICE_TYPES) {
            if (mat == ice) return true;
        }
        return false;
    }

    private float getYawFromFacing(BlockFace face) {
        // Minecraft yaw: 0 = south, -90 = east, 180 = north, 90 = west, -180 = up, 0 = down
        return switch (face) {
            case NORTH -> 180f;
            case SOUTH -> 0f;
            case WEST -> 90f;
            case EAST -> -90f;
            case UP, DOWN -> 0f;
            default -> 0f;
        };
    }
} 