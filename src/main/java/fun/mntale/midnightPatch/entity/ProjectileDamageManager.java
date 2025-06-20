package fun.mntale.midnightPatch.entity;

import fun.mntale.midnightPatch.MidnightPatch;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.SplashPotion;
import org.bukkit.entity.LingeringPotion;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

public class ProjectileDamageManager implements Listener {
    // Configurable damage values
    private static final double SNOWBALL_DAMAGE = 0.5;
    private static final double EGG_DAMAGE = 0.5;
    private static final double FISHING_ROD_DAMAGE = 0.5;
    private static final double DEFAULT_PROJECTILE_DAMAGE = 0.5;
    private static final double KNOCKBACK_STRENGTH = 0.7;

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile instanceof ThrownPotion || projectile instanceof SplashPotion || projectile instanceof LingeringPotion) return;
        if (projectile instanceof Arrow) return;
        if (projectile instanceof Trident) return;
        if (event.getHitEntity() instanceof LivingEntity target) {
            if (!(target instanceof org.bukkit.entity.Player)) return;
            LivingEntity shooterEntity = getShooter(projectile);
            if (!(shooterEntity instanceof org.bukkit.entity.Player)) return;
            double damage = getDamageForProjectile(projectile);
            FoliaScheduler.getRegionScheduler().run(MidnightPatch.instance, target.getLocation(), task -> {
                target.damage(damage, shooterEntity);
                org.bukkit.util.Vector direction = shooterEntity.getLocation().getDirection().normalize();
                org.bukkit.util.Vector knockback = direction.multiply(KNOCKBACK_STRENGTH).setY(0.35);
                target.setVelocity(knockback);
            });
        }
    }

    private double getDamageForProjectile(Projectile projectile) {
        String type = projectile.getType().name();
        return switch (type) {
            case "SNOWBALL" -> SNOWBALL_DAMAGE;
            case "EGG" -> EGG_DAMAGE;
            case "FISHING_BOBBER", "FISHING_HOOK", "FISHING_ROD" -> FISHING_ROD_DAMAGE;
            default -> DEFAULT_PROJECTILE_DAMAGE;
        };
    }

    private LivingEntity getShooter(Projectile projectile) {
        ProjectileSource shooter = projectile.getShooter();
        return (shooter instanceof LivingEntity le) ? le : null;
    }
} 