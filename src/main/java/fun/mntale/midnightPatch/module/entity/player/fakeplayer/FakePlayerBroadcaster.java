package fun.mntale.midnightPatch.module.entity.player.fakeplayer;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.craftbukkit.CraftServer;

public class FakePlayerBroadcaster {
    public static void broadcast(ServerPlayer fakePlayer) {
        ClientboundPlayerInfoUpdatePacket addPlayerInfo = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(fakePlayer));
        // 1. Create a new ServerEntity tracker for the fake player
        ServerEntity tracker = new ServerEntity(
            (net.minecraft.server.level.ServerLevel) fakePlayer.level(),
            fakePlayer,
            1200, // update interval (or 1)
            true, // trackDelta
            (packet) -> {}, // broadcast (no-op)
            (packet, uuids) -> {}, // broadcastWithIgnore (no-op)
            java.util.concurrent.ConcurrentHashMap.newKeySet() // trackedPlayers (thread-safe)
        );
        // 3. Spawn entity in world
        ClientboundAddEntityPacket addEntityPacket = new ClientboundAddEntityPacket(fakePlayer, tracker);

        for (Player online : Bukkit.getOnlinePlayers()) {
            ServerPlayer nms = ((CraftServer) Bukkit.getServer()).getServer().getPlayerList().getPlayer(online.getUniqueId());
            if (nms != null) {
                nms.connection.send(addPlayerInfo);
                nms.connection.send(addEntityPacket);
            }
        }
    }

    public static void broadcastToPlayer(ServerPlayer fakePlayer, Player online) {
        ClientboundPlayerInfoUpdatePacket addPlayerInfo = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(fakePlayer));
        ServerEntity tracker = new ServerEntity(
            (net.minecraft.server.level.ServerLevel) fakePlayer.level(),
            fakePlayer,
            1200, // update interval
            true, // trackDelta
            (packet) -> {},
            (packet, uuids) -> {},
            java.util.concurrent.ConcurrentHashMap.newKeySet() // trackedPlayers (thread-safe)
        );
        ClientboundAddEntityPacket addEntityPacket = new ClientboundAddEntityPacket(fakePlayer, tracker);

        ServerPlayer nms = ((CraftServer) Bukkit.getServer()).getServer().getPlayerList().getPlayer(online.getUniqueId());
        if (nms != null) {
            nms.connection.send(addPlayerInfo);
            nms.connection.send(addEntityPacket);
        }
    }
} 