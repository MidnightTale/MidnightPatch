package fun.mntale.midnightPatch.module.entity.player.fakeplayer;

import fun.mntale.midnightPatch.MidnightPatch;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import fun.mntale.midnightPatch.command.ToggleFakePlayerOnJoinLeaveCommand;


public class MobSpawnerPlayer implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (isCreativeOrSpectator(player)) return;
        
        if (ToggleFakePlayerOnJoinLeaveCommand.isEnabled(player)) {
            FoliaScheduler.getRegionScheduler().runDelayed(
                MidnightPatch.instance,
                player.getLocation(),
                (task) -> {
                    FakePlayerFactory.createFakeForPlayer(player);
                    rebroadcastAllFakePlayersToAllOnline();
                },
                20L
            );
        }
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        String fakeName = "[S]" + event.getName();
        ServerPlayer fakePlayer = FakePlayerFactory.fakePlayers.get(fakeName);
        if (fakePlayer != null) {
            Location loc = fakePlayer.getBukkitEntity().getLocation();
            FoliaScheduler.getRegionScheduler().run(
                MidnightPatch.instance,
                loc,
                (task) -> FakePlayerFactory.remove(fakeName)
            );
        }
    }

    @EventHandler
    public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
        rebroadcastAllFakePlayersToAllOnline();
    }

    /**
     * Checks if the player is in Creative or Spectator mode.
     */
    private boolean isCreativeOrSpectator(Player player) {
        GameMode mode = player.getGameMode();
        return mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR;
    }

    // Utility method to re-broadcast all fake players to all online players
    public static void rebroadcastAllFakePlayersToAllOnline() {
        for (Player online : org.bukkit.Bukkit.getOnlinePlayers()) {
            for (net.minecraft.server.level.ServerPlayer fake : fun.mntale.midnightPatch.module.entity.player.fakeplayer.FakePlayerFactory.fakePlayers.values()) {
                fun.mntale.midnightPatch.module.entity.player.fakeplayer.FakePlayerBroadcaster.broadcastToPlayer(fake, online);
            }
        }
    }
} 