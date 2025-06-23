package fun.mntale.midnightPatch.module.entity.player.indicator;

import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import fun.mntale.midnightPatch.command.ToggleHealthIndicatorCommand;
import java.util.List;
import org.bukkit.entity.Player;

public class HealthDamageIndicatorListener implements Listener {
    public static HealthDamageIndicatorListener instance;
    private static final double MIN_WIDTH_OFFSET = 0.05;
    private static final double MAX_WIDTH_EXTRA = 0.15;
    private static final double MIN_HEIGHT_OFFSET = 0.0;
    private static final double MAX_HEIGHT_EXTRA = 0.6;
    private static final TextColor DAMAGE_COLOR = TextColor.color(255, 0, 0);
    private static final TextColor HEAL_COLOR = TextColor.color(0, 255, 0);
    private static final TextColor XP_COLOR = TextColor.color(85, 255, 85);
    private static final int ANIMATION_DURATION = 40;
    private static final float ANIMATION_SCALE = 1.3f;
    private static final double ANIMATION_Y_OFFSET = 1.5;

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.isCancelled() ||
            e.getFinalDamage() == 0 ||
            e.getEntity() instanceof org.bukkit.entity.Item ||
            e.getEntity().getType() == org.bukkit.entity.EntityType.ALLAY) return;
        String icon = getIconForDamageCause(e.getCause());
        spawnDamageDisplay(e.getEntity(),
            icon + String.format("-%.1f", e.getFinalDamage()),
            DAMAGE_COLOR);
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent e) {
        if (e.isCancelled() ||
            e.getAmount() <= 0 ||
            e.getEntity() instanceof org.bukkit.entity.Item ||
            e.getEntity().getType() == org.bukkit.entity.EntityType.ALLAY) return;
        spawnDamageDisplay(e.getEntity(),
            String.format("+%.1f", e.getAmount()),
            HEAL_COLOR);
    }

    @EventHandler
    public void onPlayerExpChange(PlayerExpChangeEvent e) {
        if (e.getAmount() <= 0) return;
        spawnDamageDisplay(e.getPlayer(),
            String.format("+%d xp", e.getAmount()),
            XP_COLOR);
    }

    private void spawnDamageDisplay(Entity entity, String text, TextColor color) {
        org.bukkit.util.Vector direction = entity.getLocation().getDirection();
        double sideOffset = getRandomOffset(entity.getWidth());
        double heightOffset = getRandomHeightOffset(entity.getHeight() - 0.5);
        if (ThreadLocalRandom.current().nextBoolean()) {
            sideOffset = -sideOffset;
        }
        TextDisplay display = entity.getWorld().spawn(
            entity.getLocation().add(
                -direction.getZ() * sideOffset,
                heightOffset,
                direction.getX() * sideOffset
            ),
            TextDisplay.class
        );
        configureDisplay(display, text, color);

        // Show only to enabled players
        if (entity instanceof Player player) {
            if (ToggleHealthIndicatorCommand.isHealthIndicatorEnabled(player)) {
                player.showEntity(fun.mntale.midnightPatch.MidnightPatch.instance, display);
            } else {
                display.remove();
                return;
            }
        } else {
            List<Player> viewers = entity.getWorld().getPlayers().stream()
                .filter(ToggleHealthIndicatorCommand::isHealthIndicatorEnabled)
                .toList();
            if (viewers.isEmpty()) {
                display.remove();
                return;
            }
            for (Player viewer : viewers) {
                viewer.showEntity(fun.mntale.midnightPatch.MidnightPatch.instance, display);
            }
        }

        new DisplayAnimator(
            display,
            ANIMATION_DURATION,
            ANIMATION_SCALE,
            ANIMATION_Y_OFFSET
        ).start();
    }

    private void configureDisplay(TextDisplay display, String text, TextColor color) {
        display.text(Component.text(text).color(color));
        display.setSeeThrough(false);
        display.setPersistent(false);
        display.setShadowed(false);
        display.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
        display.setAlignment(TextDisplay.TextAlignment.CENTER);
        display.setBillboard(Display.Billboard.CENTER);
    }

    private double getRandomOffset(double entityWidth) {
        return (entityWidth + MIN_WIDTH_OFFSET) +
            ThreadLocalRandom.current().nextDouble() * MAX_WIDTH_EXTRA;
    }

    private double getRandomHeightOffset(double entityHeight) {
        return (entityHeight - MIN_HEIGHT_OFFSET) +
            ThreadLocalRandom.current().nextDouble() * MAX_HEIGHT_EXTRA;
    }

    private String getIconForDamageCause(DamageCause cause) {
        return switch (cause) {
            case ENTITY_ATTACK, ENTITY_SWEEP_ATTACK -> "\uD83D\uDDE1"; // 🗡
            case PROJECTILE -> "\uD83C\uDFF9"; // 🏹
            case CONTACT -> "\uD83E\uDE93"; // 🪓
            case ENTITY_EXPLOSION, BLOCK_EXPLOSION -> "\u2604"; // ☄
            case FALL -> "\u23F3"; // ⏳
            case FIRE, FIRE_TICK -> "\uD83D\uDD25"; // 🔥
            case LAVA, HOT_FLOOR -> "\u2604"; // ☄
            case MAGIC -> "\uD83E\uDDEA"; // 🧪
            case POISON -> "\u2697"; // ⚗
            case THORNS -> "\u2B6A"; // ⯪
            case DROWNING -> "\u2614"; // ☔
            case SUFFOCATION -> "\u2B6B"; // ⯫
            case VOID -> "\u2C6D"; // Ɑ
            case LIGHTNING -> "\u26A1"; // ⚡
            case STARVATION -> "\uD83C\uDF56"; // 🍖
            case FALLING_BLOCK -> "\uD83E\uDEA3"; // 🪣
            case CRAMMING -> "\uD83D\uDD14"; // 🔔
            case DRAGON_BREATH -> "\u23F3"; // ⏳
            case WITHER -> "\u26CF"; // ⛏
            case FREEZE -> "\u26C4"; // ⛄
            case SONIC_BOOM -> "\uD83D\uDD0A"; // 🔊
            default -> ""; // (default/unknown)
        };
    }
}
