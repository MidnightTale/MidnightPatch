package fun.mntale.midnightPatch.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import java.util.Collection;
import java.util.List;

@NullMarked
public class ToggleReachAroundCommand implements BasicCommand {
    
    private static final NamespacedKey REACH_AROUND_KEY = new NamespacedKey("midnightpatch", "reacharound_enabled");
    
    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        CommandSender sender = commandSourceStack.getSender();
        if (sender instanceof Player player) {
            boolean currentState = isReachAroundEnabled(player);
            boolean newState = !currentState;
            
            // Store in PDC
            if (newState) {
                player.getPersistentDataContainer().set(REACH_AROUND_KEY, PersistentDataType.INTEGER, 1);
                player.sendActionBar(Component.text("Reacharound: ENABLED", TextColor.fromHexString("#00FF00")));
            } else {
                player.getPersistentDataContainer().set(REACH_AROUND_KEY, PersistentDataType.INTEGER, 0);
                player.sendActionBar(Component.text("Reacharound: DISABLED", TextColor.fromHexString("#FF0000")));
            }
        } else {
            sender.sendMessage(Component.text("Only players can use /togglereacharound!", NamedTextColor.RED));
        }
    }

    @Override
    public @Nullable String permission() {
        return "midnightpatch.reacharound.toggle";
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("midnightpatch.reacharound.toggle") || sender.isOp();
    }

    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        return List.of();
    }
    
    /**
     * Check if reach-around is enabled for a specific player
     * @param player The player
     * @return true if reach-around is enabled, false otherwise
     */
    public static boolean isReachAroundEnabled(Player player) {
        Integer value = player.getPersistentDataContainer().get(REACH_AROUND_KEY, PersistentDataType.INTEGER);
        return value != null && value == 1; // Default to disabled if not set
    }
} 