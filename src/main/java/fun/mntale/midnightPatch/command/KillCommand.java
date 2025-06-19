package fun.mntale.midnightPatch.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import java.util.Collection;
import java.util.List;

@NullMarked
public class KillCommand implements BasicCommand {
    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        CommandSender sender = commandSourceStack.getSender();
        if (sender instanceof Player player) {
            player.damage(Double.MAX_VALUE);
        } else {
            sender.sendMessage(Component.text("Only players can use /kill on themselves!", NamedTextColor.RED));
        }
    }

    @Override
    public @Nullable String permission() {
        return "midnightpatch.kill.use";
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.isOp();
    }

    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        return List.of();
    }
} 