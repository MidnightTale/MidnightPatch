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
public class TogglePhantomIsolationCommand implements BasicCommand {
    private static final NamespacedKey PHANTOM_ISOLATION_KEY = new NamespacedKey("midnightpatch", "phantomisolation_enabled");

    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        CommandSender sender = commandSourceStack.getSender();
        if (sender instanceof Player player) {
            boolean currentState = isPhantomIsolationEnabled(player);
            boolean newState = !currentState;
            if (newState) {
                player.getPersistentDataContainer().set(PHANTOM_ISOLATION_KEY, PersistentDataType.INTEGER, 1);
                player.sendActionBar(Component.text("Phantom Isolation: ENABLED", TextColor.fromHexString("#00FF00")));
            } else {
                player.getPersistentDataContainer().set(PHANTOM_ISOLATION_KEY, PersistentDataType.INTEGER, 0);
                player.sendActionBar(Component.text("Phantom Isolation: DISABLED", TextColor.fromHexString("#FF0000")));
            }
        } else {
            sender.sendMessage(Component.text("Only players can use /togglephantom!", NamedTextColor.RED));
        }
    }

    @Override
    public @Nullable String permission() {
        return "midnightpatch.phantomisolation.toggle";
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("midnightpatch.phantomisolation.toggle") || sender.isOp();
    }

    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        return List.of();
    }

    public static boolean isPhantomIsolationEnabled(Player player) {
        Integer value = player.getPersistentDataContainer().get(PHANTOM_ISOLATION_KEY, PersistentDataType.INTEGER);
        return value == null || value == 1; // Default to enabled if not set
    }
} 