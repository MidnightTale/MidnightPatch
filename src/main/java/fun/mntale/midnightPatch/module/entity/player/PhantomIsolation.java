package fun.mntale.midnightPatch.module.entity.player;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import io.github.retrooper.packetevents.util.folia.TaskWrapper;

public class PhantomIsolation {
    private static TaskWrapper task;

    public static void start(Plugin plugin) {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        task = FoliaScheduler.getAsyncScheduler().runAtFixedRate(
            plugin,
            (taskphantom) -> {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!shouldIsolate(player)) {
                        player.setStatistic(Statistic.TIME_SINCE_REST, 0);
                    }
                }
            },
            1L,
            120L
        );
    }

    // Placeholder for isolation logic, always true for now
    private static boolean shouldIsolate(Player player) {
        return fun.mntale.midnightPatch.command.TogglePhantomIsolationCommand.isPhantomIsolationEnabled(player);
    }
} 