package fun.mntale.midnightPatch.module.entity.player.fakeplayer;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collection;
import java.nio.charset.StandardCharsets;

public class MobSpawnerPlayer implements Listener {

    private static final Map<String, ServerPlayer> fakePlayers = new ConcurrentHashMap<>();

    public static void create(Location location, String name) {
        try {
            if (fakePlayers.containsKey(name)) {
                ServerPlayer existing = fakePlayers.get(name);
                existing.setPos(location.getX(), location.getY(), location.getZ());
                return;
            }

            MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
            ServerLevel world = ((CraftWorld) location.getWorld()).getHandle();
            UUID fakeUuid = UUID.nameUUIDFromBytes(("FAKE_PLAYER_" + name).getBytes(StandardCharsets.UTF_8));
            GameProfile gameProfile = new GameProfile(fakeUuid, name);
            ClientInformation clientInformation = ClientInformation.createDefault();
            ServerPlayer fakePlayer = new ServerPlayer(server, world, gameProfile, clientInformation);

            net.minecraft.network.Connection dummyNetwork = new DummyNetwork();
            CommonListenerCookie cookie = CommonListenerCookie.createInitial(gameProfile, false);
            fakePlayer.connection = new DummyConnection(server, dummyNetwork, fakePlayer, cookie);

            fakePlayer.setPos(location.getX(), location.getY(), location.getZ());

            world.addFreshEntity(fakePlayer);

            broadcastFakePlayer(fakePlayer);

            fakePlayers.put(name, fakePlayer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void broadcastFakePlayer(ServerPlayer fakePlayer) {
        // 1. Add to tab list
        ClientboundPlayerInfoUpdatePacket addPlayerInfo = ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(fakePlayer));
        // 2. Create a new ServerEntity tracker for the fake player
        ServerEntity tracker = new ServerEntity(
            (ServerLevel) fakePlayer.level(),
            fakePlayer,
            0, // update interval
            true, // trackDelta
            (packet) -> {}, // broadcast (no-op)
            (packet, uuids) -> {}, // broadcastWithIgnore (no-op)
            new java.util.HashSet<>() // trackedPlayers
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

    public static boolean remove(String name) {
        ServerPlayer fakePlayer = fakePlayers.remove(name);
        if (fakePlayer == null) return false;
        fakePlayer.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);

        ClientboundPlayerInfoRemovePacket removePacket = new ClientboundPlayerInfoRemovePacket(List.of(fakePlayer.getUUID()));
        for (org.bukkit.entity.Player online : Bukkit.getOnlinePlayers()) {
            ServerPlayer nms = ((CraftServer) Bukkit.getServer()).getServer().getPlayerList().getPlayer(online.getUniqueId());
            if (nms != null) {
                nms.connection.send(removePacket);
            }
        }
        return true;
    }

    public static Player getFakePlayer(String name) {
        ServerPlayer fakePlayer = fakePlayers.get(name);
        return (fakePlayer != null) ? fakePlayer.getBukkitEntity() : null;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
    }

    public static Collection<String> getFakePlayerNames() {
        return fakePlayers.keySet();
    }
} 