package fun.mntale.midnightPatch.module.entity.player.fakeplayer;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.server.level.ParticleStatus;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;

public class FakePlayerFactory {
    public static final Map<String, ServerPlayer> fakePlayers = new ConcurrentHashMap<>();

    public static void createFakePlayer(FakePlayerSpec spec) {
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel world = ((CraftWorld) spec.location().getWorld()).getHandle();
        GameProfile profile = (spec.skinProfile() != null) ? spec.skinProfile() : new GameProfile(spec.uuid(), spec.name());
        ClientInformation clientInformation = new ClientInformation(
            "en_us",
            8,
            ChatVisiblity.HIDDEN,
            false,
            127,
            HumanoidArm.RIGHT,
            false,
            false,
            ParticleStatus.MINIMAL
        );
        ServerPlayer fakePlayer = new ServerPlayer(server, world, profile, clientInformation);
        net.minecraft.network.Connection dummyNetwork = new DummyNetwork();
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(profile, false);
        fakePlayer.connection = new DummyConnection(server, dummyNetwork, fakePlayer, cookie);
        fakePlayer.setPos(spec.location().getX(), spec.location().getY(), spec.location().getZ());
        world.addFreshEntity(fakePlayer);
        FakePlayerBroadcaster.broadcast(fakePlayer);
        addFakePlayer(spec.name(), fakePlayer);
    }

    public static void create(Location location, String name) {
        if (fakePlayers.containsKey(name)) {
            fakePlayers.get(name).setPos(location.getX(), location.getY(), location.getZ());
            return;
        }
        UUID fakeUuid = UUID.nameUUIDFromBytes(("FAKE_PLAYER_" + name).getBytes(StandardCharsets.UTF_8));
        FakePlayerSpec spec = new FakePlayerSpec(name, fakeUuid, location, null, false, null);
        FakePlayerFactory.createFakePlayer(spec);
    }

    public static boolean remove(String name) {
        ServerPlayer fakePlayer = fakePlayers.remove(name);
        if (fakePlayer == null) return false;
        fakePlayer.remove(net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        // Remove from tab list for all players
        ClientboundPlayerInfoRemovePacket removePacket = new ClientboundPlayerInfoRemovePacket(List.of(fakePlayer.getUUID()));
        for (Player online : Bukkit.getOnlinePlayers()) {
            ServerPlayer nms = ((CraftServer) Bukkit.getServer()).getServer().getPlayerList().getPlayer(online.getUniqueId());
            if (nms != null) nms.connection.send(removePacket);
        }
        return true;
    }

    public static Player getFakePlayer(String name) {
        ServerPlayer fakePlayer = fakePlayers.get(name);
        return (fakePlayer != null) ? fakePlayer.getBukkitEntity() : null;
    }

    public static Collection<String> getFakePlayerNames() {
        return fakePlayers.keySet();
    }

    public static void createFakeForPlayer(Player player) {
        String fakeName = "\u00A7m\u00A78" + player.getName();
        UUID shadowUuid = UUID.nameUUIDFromBytes(("SHADOW_" + player.getName()).getBytes(StandardCharsets.UTF_8));
        GameProfile realProfile = ((org.bukkit.craftbukkit.entity.CraftPlayer) player).getProfile();
        GameProfile fakeProfile = new GameProfile(shadowUuid, fakeName);
        fakeProfile.getProperties().putAll(realProfile.getProperties());
        FakePlayerSpec spec = new FakePlayerSpec(fakeName, shadowUuid, player.getLocation(), fakeProfile, false, null);
        FakePlayerFactory.createFakePlayer(spec);
        Bukkit.getLogger().info("[MobSpawner] Spawned fake player for logout: " + fakeName);
    }

    public static void addFakePlayer(String name, ServerPlayer player) {
        fakePlayers.put(name, player);
    }
} 