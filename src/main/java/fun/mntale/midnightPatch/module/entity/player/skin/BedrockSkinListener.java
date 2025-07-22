package fun.mntale.midnightPatch.module.entity.player.skin;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import fun.mntale.midnightPatch.MidnightPatch;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class BedrockSkinListener implements Listener {

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.getUniqueId().toString().startsWith("00000000-0000-0000")) {
            return;
        }
        fetchAndApplySkin(player);
    }

    private void fetchAndApplySkin(Player player) {
        String uuid = player.getUniqueId().toString();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.creepernation.net/raw/" + uuid))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> {
                    String value = parseJson(responseBody, "value");
                    String signature = parseJson(responseBody, "signature");

                    if (value != null && signature != null) {
                        MidnightPatch.instance.foliaLib.getScheduler().runAtEntity(player, (fetchAndApplySkin) -> applySkin(player, value, signature));
                    }
                });
    }

    private void applySkin(Player player, String value, String signature) {
        PlayerProfile profile = player.getPlayerProfile();
        profile.getProperties().clear();
        profile.setProperty(new ProfileProperty("textures", value, signature));
        player.setPlayerProfile(profile);

        MidnightPatch.instance.foliaLib.getScheduler().runAtEntity(player, (applySkin) -> {
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.equals(player)) {
                    onlinePlayer.hidePlayer(MidnightPatch.instance, player);
                    onlinePlayer.showPlayer(MidnightPatch.instance, player);
                }
            }
        });
    }

    private String parseJson(String json, String key) {
        String search = "\"" + key + "\":\"";
        int startIndex = json.indexOf(search);
        if (startIndex == -1) {
            return null;
        }
        startIndex += search.length();
        int endIndex = json.indexOf("\"", startIndex);
        if (endIndex == -1) {
            return null;
        }
        return json.substring(startIndex, endIndex);
    }
}