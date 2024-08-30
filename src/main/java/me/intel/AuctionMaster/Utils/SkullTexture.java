package me.intel.AuctionMaster.Utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.intel.AuctionMaster.AuctionMaster;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

import static me.intel.AuctionMaster.Utils.HeadDatabase.headApi;

public class SkullTexture {


    public static Method getMethod(final Class<?> clazz, final String name) {
        for (final Method m : clazz.getMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }

    private static Field getField(final Class<?> clazz, final String fieldName) throws NoSuchFieldException {
        return clazz.getDeclaredField(fieldName);
    }

    public static void setFieldValue(final Object object, final String fieldName, final Object value) throws NoSuchFieldException, IllegalAccessException {
        final Field f = getField(object.getClass(), fieldName);
        f.setAccessible(true);
        f.set(object, value);
    }

    public static Constructor<?> getConstructor(final Class<?> clazz, final int numParams) {
        for (final Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.getParameterTypes().length == numParams) {
                return constructor;
            }
        }

        return null;
    }

    public static ItemStack getSkull(String texture) {
        texture=texture.replace(" ", "");
        if(texture.length()>16) {
            try {
                ItemStack skull;
                if(AuctionMaster.upperVersion) {
                    skull = new ItemStack(Material.getMaterial("PLAYER_HEAD"), 1);
                }
                else {
                    skull = new ItemStack(Material.getMaterial("SKULL_ITEM"), 1, (short) 3);
                }

                final ItemMeta meta = skull.getItemMeta();
                try {
                    PlayerProfile profile = Bukkit.createPlayerProfile("AuctionMaster");
                    PlayerTextures textures = profile.getTextures();
                    texture = new String(Base64.getDecoder().decode(texture)).split("\"url\":\"")[1].split("\"")[0];
                    try {
                        textures.setSkin(new URL(texture));
                        ((SkullMeta)meta).setOwnerProfile(profile);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    System.err.println("Failed to create fake GameProfile for custom player head:");
                    e.printStackTrace();
                }
                skull.setItemMeta(meta);
                return skull;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            if (texture.contains("hdb-")) {
                try {
                    return headApi.getItemHead(texture.replace("hdb-", ""));
                } catch (Exception e) {
                    texture = "mhf_question";
                }
            }
            ItemStack playerHead;
            if(AuctionMaster.upperVersion) {
                playerHead = new ItemStack(Material.getMaterial("PLAYER_HEAD"), 1);
            }
            else {
                playerHead = new ItemStack(Material.getMaterial("SKULL_ITEM"), 1, (short) 3);
            }
            SkullMeta sm = (SkullMeta) playerHead.getItemMeta();
            sm.setOwner(texture);
            playerHead.setItemMeta(sm);
            return playerHead;
        }
        return null;
    }

}
