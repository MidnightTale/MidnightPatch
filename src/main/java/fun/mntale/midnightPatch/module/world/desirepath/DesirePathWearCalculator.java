package fun.mntale.midnightPatch.module.world.desirepath;

import org.bukkit.entity.Player;
import org.bukkit.World;

public class DesirePathWearCalculator {
    @SuppressWarnings("deprecation")
    public static int calculateWear(Player player) {
        boolean isSprinting = player.isSprinting();
        boolean isSneaking = player.isSneaking();
        boolean isJumping = player.getVelocity().getY() > 0.1 && !player.isOnGround();
        World world = player.getWorld();
        boolean isRaining = world.hasStorm();
        boolean isThundering = world.isThundering();
        int baseWear = 1;
        if (isSprinting) baseWear += 1;
        if (isJumping) baseWear += 2;
        if (isSneaking) baseWear -= 1;
        baseWear = Math.max(1, baseWear);
        if (isThundering) {
            baseWear *= 3;
        } else if (isRaining) {
            baseWear *= 2;
        }
        return baseWear;
    }
} 