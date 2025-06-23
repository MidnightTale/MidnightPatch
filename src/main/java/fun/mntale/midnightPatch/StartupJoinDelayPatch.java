package fun.mntale.midnightPatch;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

public class StartupJoinDelayPatch implements Listener {
    private static final long START_TIME = System.currentTimeMillis();
    private static final long DELAY_MILLIS = 5000; // 5 seconds

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPreLogin(AsyncPlayerPreLoginEvent event) {
        long now = System.currentTimeMillis();
        if (now - START_TIME < DELAY_MILLIS) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Server is still starting up. Please wait a few seconds and try again.");
        }
    }
} 