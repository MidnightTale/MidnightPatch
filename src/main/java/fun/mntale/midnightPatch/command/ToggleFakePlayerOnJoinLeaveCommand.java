package fun.mntale.midnightPatch.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * /togglefakeplayer
 *
 * Toggles whether a fake player is spawned on join/leave for the sender.
 * Usage:
 *   /togglefakeplayer
 *     - Enables/disables the feature for the player.
 *
 * If enabled, a fake player will be spawned on join/leave for this player.
 */
public class ToggleFakePlayerOnJoinLeaveCommand implements BasicCommand {
    private static final NamespacedKey KEY = new NamespacedKey("midnightpatch", "fakeplayer_on_joinleave_enabled");

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by a player.");
            return;
        }
        boolean currentState = isEnabled(player);
        boolean newState = !currentState;
        if (newState) {
            player.getPersistentDataContainer().set(KEY, PersistentDataType.INTEGER, 1);
            player.sendActionBar(Component.text("Fake Player on Join/Leave: ENABLED", TextColor.fromHexString("#00FF00")));
        } else {
            player.getPersistentDataContainer().set(KEY, PersistentDataType.INTEGER, 0);
            player.sendActionBar(Component.text("Fake Player on Join/Leave: DISABLED", TextColor.fromHexString("#FF0000")));
        }
    }

    public static boolean isEnabled(Player player) {
        Integer value = player.getPersistentDataContainer().get(KEY, PersistentDataType.INTEGER);
        return value != null && value == 1;
    }

    @Override
    public @Nullable String permission() {
        return "midnightpatch.togglefakeplayer";
    }
} 