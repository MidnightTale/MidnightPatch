package fun.mntale.midnightPatch.module.entity.player.task.tool;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class EntityTargetingUtil {
    
    public static Entity getTargetEntityDoubleRange(Player player, double range) {
        Location eye = player.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        double step = 0.1;
        for (double d = 0; d <= range; d += step) {
            Location checkLoc = eye.clone().add(direction.clone().multiply(d));
            List<Entity> nearby = player.getWorld().getNearbyEntities(checkLoc, 0.3, 0.3, 0.3).stream()
                .filter(e -> e != player && e instanceof LivingEntity && !e.isDead())
                .toList();
            if (!nearby.isEmpty()) {
                return nearby.get(0);
            }
        }
        return null;
    }
} 