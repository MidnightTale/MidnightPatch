package fun.mntale.midnightPatch.module.entity.player.fakeplayer;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import com.mojang.authlib.GameProfile;
import java.util.UUID;

/**
 * @param skinProfile nullable, if null use name/uuid only
 * @param stateSource nullable, if copyStateFromPlayer is true, this is the player to copy from
 */
public record FakePlayerSpec(String name, UUID uuid, Location location, GameProfile skinProfile,
                             boolean copyStateFromPlayer, Player stateSource) {
}