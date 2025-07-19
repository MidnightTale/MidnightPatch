package fun.mntale.midnightPatch.bootstrap;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import fun.mntale.midnightPatch.command.ToggleReachAroundCommand;

/**
 * PlaceholderAPI expansion for MidnightPatch.
 * 
 * Usage: %midnightpatch_<placeholder>%
 * Each placeholder returns "Enabled" or "Disabled" for the given player.
 *
 * Available placeholders:
 *   %midnightpatch_reacharound%           - ReachAround feature toggle
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
        return "1.1";
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
            default -> null;
        };
    }

    private String boolToString(boolean enabled) {
        return enabled ? "Enabled" : "Disabled";
    }
} 