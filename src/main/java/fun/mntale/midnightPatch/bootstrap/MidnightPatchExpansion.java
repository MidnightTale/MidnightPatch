package fun.mntale.midnightPatch.bootstrap;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fun.mntale.midnightPatch.command.ToggleArmorStandPoseCommand;
import fun.mntale.midnightPatch.command.ToggleDeathCameraCommand;
import fun.mntale.midnightPatch.command.ToggleDeathLootGlowCommand;
import fun.mntale.midnightPatch.command.ToggleDeathLootInvulnerableCommand;
import fun.mntale.midnightPatch.command.ToggleDeathLootLetMobPickupCommand;
import fun.mntale.midnightPatch.command.ToggleDeathLootLetPlayerPickupCommand;
import fun.mntale.midnightPatch.command.ToggleDeathLootNoDespawnCommand;
import fun.mntale.midnightPatch.command.ToggleDesirePathCommand;
import fun.mntale.midnightPatch.command.ToggleHealthIndicatorCommand;
import fun.mntale.midnightPatch.command.ToggleLootChestProtectionCommand;
import fun.mntale.midnightPatch.command.ToggleMendingRepairCommand;
import fun.mntale.midnightPatch.command.TogglePhantomIsolationCommand;
import fun.mntale.midnightPatch.command.ToggleProjectileDamageCommand;
import fun.mntale.midnightPatch.command.ToggleReachAroundCommand;

/**
 * PlaceholderAPI expansion for MidnightPatch.
 * 
 * Usage: %midnightpatch_<placeholder>%
 * Each placeholder returns "Enabled" or "Disabled" for the given player.
 *
 * Available placeholders:
 *   %midnightpatch_reacharound%           - ReachAround feature toggle
 *   %midnightpatch_healthindicator%       - Health Indicator toggle
 *   %midnightpatch_deathlootglow%         - Death Loot Glow toggle
 *   %midnightpatch_deathlootinvulnerable% - Death Loot Invulnerable toggle
 *   %midnightpatch_deathlootnodespawn%    - Death Loot No Despawn toggle
 *   %midnightpatch_deathlootletmobpickup% - Death Loot Let Mob Pickup toggle
 *   %midnightpatch_deathlootletplayerpickup% - Death Loot Let Player Pickup toggle
 *   %midnightpatch_phantomisolation%      - Phantom Isolation toggle
 *   %midnightpatch_mendingrepair%         - Mending Repair toggle
 *   %midnightpatch_lootchestprotection%   - Loot Chest Protection toggle
 *   %midnightpatch_armorstandpose%        - Armor Stand Pose toggle
 *   %midnightpatch_projectiledamage%      - Projectile Damage toggle
 *   %midnightpatch_desirepath%            - Desire Path toggle
 *   %midnightpatch_deathcamera%           - Death Camera toggle
 */
public class MidnightPatchExpansion extends PlaceholderExpansion {
    @Override
    public @NotNull String getIdentifier() {
        return "midnightpatch";
    }

    @Override
    public @NotNull String getAuthor() {
        return "MidnightTale";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, @NotNull String identifier) {
        if (offlinePlayer == null) return "";
        Player player = offlinePlayer.getPlayer();
        if (player == null) return "";
        return switch (identifier.toLowerCase()) {
            case "reacharound" -> boolToString(ToggleReachAroundCommand.isReachAroundEnabled(player));
            case "healthindicator" -> boolToString(ToggleHealthIndicatorCommand.isHealthIndicatorEnabled(player));
            case "deathlootglow" -> boolToString(ToggleDeathLootGlowCommand.isEnabled(player));
            case "deathlootinvulnerable" -> boolToString(ToggleDeathLootInvulnerableCommand.isEnabled(player));
            case "deathlootnodespawn" -> boolToString(ToggleDeathLootNoDespawnCommand.isEnabled(player));
            case "deathlootletmobpickup" -> boolToString(ToggleDeathLootLetMobPickupCommand.isEnabled(player));
            case "deathlootletplayerpickup" -> boolToString(ToggleDeathLootLetPlayerPickupCommand.isEnabled(player));
            case "phantomisolation" -> boolToString(TogglePhantomIsolationCommand.isPhantomIsolationEnabled(player));
            case "mendingrepair" -> boolToString(ToggleMendingRepairCommand.isEnabled(player));
            case "lootchestprotection" -> boolToString(ToggleLootChestProtectionCommand.isLootChestProtectionEnabled(player));
            case "armorstandpose" -> boolToString(ToggleArmorStandPoseCommand.isArmorStandPoseEnabled(player));
            case "projectiledamage" -> boolToString(ToggleProjectileDamageCommand.isProjectileDamageEnabled(player));
            case "desirepath" -> boolToString(ToggleDesirePathCommand.isDesirePathEnabled(player));
            case "deathcamera" -> boolToString(ToggleDeathCameraCommand.isDeathCameraEnabled(player));
            default -> null;
        };
    }

    private String boolToString(boolean enabled) {
        return enabled ? "Enabled" : "Disabled";
    }
} 