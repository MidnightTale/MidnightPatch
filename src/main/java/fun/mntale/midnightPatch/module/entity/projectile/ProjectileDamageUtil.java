package fun.mntale.midnightPatch.module.entity.projectile;

import org.bukkit.entity.Projectile;
import org.bukkit.entity.LivingEntity;
import org.bukkit.projectiles.ProjectileSource;

public class ProjectileDamageUtil {
    public static double getDamageForProjectile(Projectile projectile) {
        String type = projectile.getType().name();
        return switch (type) {
            case "SNOWBALL" -> ProjectileDamageConfig.SNOWBALL_DAMAGE;
            case "EGG" -> ProjectileDamageConfig.EGG_DAMAGE;
            case "FISHING_BOBBER", "FISHING_HOOK", "FISHING_ROD" -> ProjectileDamageConfig.FISHING_ROD_DAMAGE;
            default -> ProjectileDamageConfig.DEFAULT_PROJECTILE_DAMAGE;
        };
    }

    public static LivingEntity getShooter(Projectile projectile) {
        ProjectileSource shooter = projectile.getShooter();
        return (shooter instanceof LivingEntity le) ? le : null;
    }
} 