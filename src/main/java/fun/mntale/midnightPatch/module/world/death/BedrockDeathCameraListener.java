package fun.mntale.midnightPatch.module.world.death;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;

import fun.mntale.midnightPatch.MidnightPatch;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.util.Vector;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.event.player.PlayerQuitEvent;

public class BedrockDeathCameraListener implements Listener {
    private final Map<UUID, ArmorStand> deathCameras = new ConcurrentHashMap<>();
    private static final Logger logger = MidnightPatch.instance.getLogger();

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (player.getUniqueId().toString().startsWith("00000000-0000-0000")) {
            return;
        }

        Location deathLoc = player.getLocation().clone();
        World world = player.getWorld();

        final double radius = 4.0;
        final double height = 2.5;
        final int totalTicks = 20 * 18;
        final Location center = deathLoc.clone().add(0, height, 0);
        double startAngle = 0;
        double x = center.getX() + radius * Math.cos(startAngle);
        double z = center.getZ() + radius * Math.sin(startAngle);
        double y = center.getY();
        Location camLoc = new Location(center.getWorld(), x, y, z);

        ArmorStand stand = (ArmorStand) world.spawnEntity(camLoc, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setMarker(true);
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.setSilent(true);
        stand.setPersistent(false);
        stand.setCustomNameVisible(false);
        stand.teleportAsync(camLoc);

        deathCameras.put(player.getUniqueId(), stand);

        sendSetCameraPacket(player, stand.getEntityId());

        ItemStack invisibleItem = new ItemStack(Material.BRICK);
        ItemMeta meta = invisibleItem.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            NamespacedKey modelKey = NamespacedKey.fromString("minecraft:air");
            meta.setItemModel(modelKey);
            invisibleItem.setItemMeta(meta);
        }
        sendSetSlotPacket(player, invisibleItem);

        final int[] tick = {0};
        WrappedTask[] orbitWrapper = new WrappedTask[1];

        orbitWrapper[0] = MidnightPatch.instance.foliaLib.getScheduler().runAtEntityTimer(stand, () -> {
            tick[0]++;
            double angle = 2 * Math.PI * (((double)(tick[0] % totalTicks)) / totalTicks);
            double newX = center.getX() + radius * Math.cos(angle);
            double newZ = center.getZ() + radius * Math.sin(angle);
            double newY = center.getY();
            Location newLoc = new Location(center.getWorld(), newX, newY, newZ);
            Vector direction = deathLoc.toVector().subtract(newLoc.toVector()).normalize();
            newLoc.setDirection(direction);
            stand.teleportAsync(newLoc);
        }, 1L, 1L);

        final Component deathLocMsg = Component.text("X: " + deathLoc.getBlockX() + "   Y: " + deathLoc.getBlockY() + "   Z: " + deathLoc.getBlockZ());
        final WrappedTask[] actionBarWrapper = new WrappedTask[1];
        actionBarWrapper[0] = MidnightPatch.instance.foliaLib.getScheduler().runAtEntityTimer(player, () -> player.sendActionBar(deathLocMsg), null, 0L, 20L);

        final WrappedTask[] wrapper = new WrappedTask[1];
        wrapper[0] = MidnightPatch.instance.foliaLib.getScheduler().runAtEntityTimer(player, () -> {
            if (!player.isDead()) {
                sendSetCameraPacket(player, player.getEntityId());
                ArmorStand cam = deathCameras.remove(player.getUniqueId());
                if (cam != null && !cam.isDead()) {
                    MidnightPatch.instance.foliaLib.getScheduler().runAtEntityLater(cam, (t) -> cam.remove(), null, 20L);
                }
                ItemStack realItem = player.getInventory().getItemInMainHand();
                sendSetSlotPacket(player, realItem);
                actionBarWrapper[0].cancel();
                wrapper[0].cancel();
            }
        }, null, 10L, 10L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ArmorStand cam = deathCameras.remove(player.getUniqueId());
        if (cam != null && !cam.isDead()) {
            cam.remove();
        }
    }

    private void sendSetCameraPacket(Player player, int entityId) {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.CAMERA);
        packet.getIntegers().write(0, entityId);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (Exception e) {
            logger.severe("Error sending camera packet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendSetSlotPacket(Player player, ItemStack item) {
        PacketContainer setSlot = new PacketContainer(PacketType.Play.Server.SET_SLOT);
        setSlot.getIntegers().write(0, 0); 
        setSlot.getIntegers().write(1, player.getInventory().getHeldItemSlot() + 36);
        setSlot.getItemModifier().write(0, item);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, setSlot);
        } catch (Exception e) {
            logger.severe("Error sending set slot packet: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 