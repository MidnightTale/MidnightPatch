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
public class ToggleLootChestProtectionCommand implements BasicCommand {
    private static final NamespacedKey LOOT_CHEST_PROTECTION_KEY = new NamespacedKey("midnightpatch", "lootchestprotection_enabled");

    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        CommandSender sender = commandSourceStack.getSender();
        if (sender instanceof Player player) {
            boolean currentState = isLootChestProtectionEnabled(player);
            boolean newState = !currentState;
            if (newState) {
                player.getPersistentDataContainer().set(LOOT_CHEST_PROTECTION_KEY, PersistentDataType.INTEGER, 1);
                player.sendActionBar(Component.text("Loot Chest Protection: ENABLED", TextColor.fromHexString("#00FF00")));
            } else {
                player.getPersistentDataContainer().set(LOOT_CHEST_PROTECTION_KEY, PersistentDataType.INTEGER, 0);
                player.sendActionBar(Component.text("Loot Chest Protection: DISABLED", TextColor.fromHexString("#FF0000")));
            }
        } else {
            sender.sendMessage(Component.text("Only players can use /toggletargetloot!", NamedTextColor.RED));
        }
    }

    @Override
    public @Nullable String permission() {
        return "midnightpatch.lootchestprotection.toggle";
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("midnightpatch.lootchestprotection.toggle") || sender.isOp();
    }

    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        return List.of();
    }

    public static boolean isLootChestProtectionEnabled(Player player) {
        Integer value = player.getPersistentDataContainer().get(LOOT_CHEST_PROTECTION_KEY, PersistentDataType.INTEGER);
        return value == null || value == 1; // Default to enabled if not set
    }
} 