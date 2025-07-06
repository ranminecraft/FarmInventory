package cc.ranmc.farm.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class FarmUtil {
    /**
     * 输出日志
     * @param text 内容
     */
    public static void print(String text){
        Bukkit.getConsoleSender().sendMessage(text);
    }

    /**
     * 输入聊天
     * @param text 内容
     */
    public static void say(String text){
        Bukkit.broadcastMessage(text);
    }

    /**
     * 文本替换
     * @param text 内容
     * @param p 玩家
     * @return 内容
     */
    public static String color(String text, Player p) {
        if(text == null) {
            text = "";
            print("§b[FarmInventory] §c加载文本错误");
        }else {
            text=text.replace("&", "§")
                    .replace("%player%",p.getName())
                    .replace("%player_x%",""+p.getLocation().getBlockX())
                    .replace("%player_y%",""+p.getLocation().getBlockY())
                    .replace("%player_z%",""+p.getLocation().getBlockZ());
        }
        return text;
    }

    public static String color(String text) {
        if(text == null) {
            text = "";
            print("§b[FarmInventory] §c加载文本错误");
        }else {
            text=text.replace("&", "§");
        }
        return text;
    }

    /**
     * 获取物品
     */
    public static ItemStack getItem(Material material, int count) {
        return new ItemStack(material,count);
    }

    public static ItemStack getItem(Material material, int count, String name) {
        ItemStack item = new ItemStack(material,count);
        ItemMeta meta = item.getItemMeta();
        Objects.requireNonNull(meta).setDisplayName(color(name));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getItem(Material material, int count, String name, String... lore) {
        ItemStack item = new ItemStack(material,count);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(name));
        for (int i = 0; i < lore.length; i++) {
            lore[i] = color(lore[i]);
        }
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getItem(Material material, int count, String name, List<String> lore) {
        ItemStack item = new ItemStack(material,count);
        ItemMeta meta = item.getItemMeta();
        Objects.requireNonNull(meta).setDisplayName(color(name));
        lore.replaceAll(FarmUtil::color);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
