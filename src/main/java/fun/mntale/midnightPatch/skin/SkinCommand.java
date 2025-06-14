package fun.mntale.midnightPatch.skin;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@NullMarked
public class SkinCommand implements BasicCommand {
    private final SkinManager skinManager;
    private static final List<String> VALID_TIERS = Arrays.asList("tier1", "tier2", "tier3", "tier4", "tier5", "tier6");

    public SkinCommand(SkinManager skinManager) {
        this.skinManager = skinManager;
    }

    @Override
    public void execute(CommandSourceStack commandSourceStack, String[] args) {
        CommandSender sender = commandSourceStack.getSender();
        
        if (!sender.isOp()) {
            sender.sendMessage(Component.text("You must be an operator to use this command!", NamedTextColor.RED));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /midnightpatch skin set <tier1|tier2|tier3|tier4|tier5|tier6> [playername]", NamedTextColor.RED));
            return;
        }

        if (!args[0].equalsIgnoreCase("skin") || !args[1].equalsIgnoreCase("set")) {
            sender.sendMessage(Component.text("Usage: /midnightpatch skin set <tier1|tier2|tier3|tier4|tier5|tier6> [playername]", NamedTextColor.RED));
            return;
        }

        String tier = args[2].toLowerCase();
        if (!VALID_TIERS.contains(tier)) {
            sender.sendMessage(Component.text("Invalid tier! Use tier1, tier2, tier3, tier4, tier5, or tier6", NamedTextColor.RED));
            return;
        }

        // If sender is console, require a target player
        if (!(sender instanceof Player) && args.length < 4) {
            sender.sendMessage(Component.text("Console must specify a target player!", NamedTextColor.RED));
            return;
        }

        String targetPlayer = args.length > 3 ? args[3] : sender.getName();
        skinManager.setSkin(sender, tier, targetPlayer);
    }

    @Override
    public @Nullable String permission() {
        return "midnightpatch.skin.use";
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.isOp();
    }

    @Override
    public Collection<String> suggest(CommandSourceStack commandSourceStack, String[] args) {
        CommandSender sender = commandSourceStack.getSender();
        if (!sender.isOp()) {
            return List.of();
        }

        if (args.length == 1) {
            return List.of("skin");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("skin")) {
            return List.of("set");
        } else if (args.length == 3 && args[0].equalsIgnoreCase("skin") && args[1].equalsIgnoreCase("set")) {
            String input = args[2].toLowerCase();
            return VALID_TIERS.stream()
                .filter(tier -> tier.startsWith(input))
                .toList();
        } else if (args.length == 4 && args[0].equalsIgnoreCase("skin") && args[1].equalsIgnoreCase("set")) {
            String input = args[3].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(input))
                .toList();
        }
        return List.of();
    }
} 