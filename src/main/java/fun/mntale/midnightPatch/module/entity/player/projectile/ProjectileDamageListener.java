package fun.mntale.midnightPatch.module.entity.player.projectile;

import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;

public class ProjectileDamageListener implements Listener {
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile instanceof ThrownPotion) return;
        if (projectile instanceof Arrow) return;
        if (projectile instanceof Trident) return;
        if (event.getHitEntity() instanceof LivingEntity target) {
            if (!(target instanceof org.bukkit.entity.Player player)) return;
            LivingEntity shooterEntity = ProjectileDamageUtil.getShooter(projectile);
            if (!(shooterEntity instanceof org.bukkit.entity.Player)) return;
            double damage = ProjectileDamageUtil.getDamageForProjectile(projectile);
            MidnightPatch.instance.foliaLib.getScheduler().runAtLocation(target.getLocation(), task -> {
                target.damage(damage, shooterEntity);
                org.bukkit.util.Vector direction = shooterEntity.getLocation().getDirection().normalize();
                org.bukkit.util.Vector knockback = direction.multiply(ProjectileDamageConfig.KNOCKBACK_STRENGTH).setY(0.35);
                target.setVelocity(knockback);
            });
        }
    }
} 