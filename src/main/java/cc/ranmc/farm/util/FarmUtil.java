package cc.ranmc.farm.util;

import cc.ranmc.farm.Main;
import cc.ranmc.farm.bean.Cop;
import cc.ranmc.farm.constant.SQLKey;
import cc.ranmc.farm.sql.SQLFilter;
import cc.ranmc.utils.BasicUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cc.ranmc.utils.BasicUtil.THY_PREFIX;

public class FarmUtil {

    private static final Main plugin = Main.getInstance();
    // 板
    private static final ItemStack PANE = getItem(Material.GRAY_STAINED_GLASS_PANE, 1, " ");
    /**
     * 打开农作物仓库菜单
     */
    public static void openCropGUI(Player player, String crop, int page) {
        if (page > 20 && !player.hasPermission("ranmc.vip")) page = 20;
        if (page > 30 && !player.hasPermission("ranmc.svip")) page = 30;
        if (page > 50) page = 50;
        if (page < 1) page = 1;
        Map<String,String> playerMap = plugin.getData().selectMap(SQLKey.PLAYER,
                new SQLFilter().where(SQLKey.PLAYER, player.getName()));
        int count = Integer.parseInt(playerMap.getOrDefault(crop.toUpperCase(), "0"));
        Inventory inventory = Bukkit.createInventory(null, 54,
                color("&d&l桃花源丨作物仓库"));

        Cop cop = new Cop(crop);
        if (cop.getMaterial() == Material.AIR) {
            player.sendMessage(color("&b桃花源>>>&c没有找到这个农作物"));
            return;
        }

        inventory.setItem(45, getItem(Material.RED_STAINED_GLASS_PANE, 1, "&c返回菜单"));
        inventory.setItem(46, PANE);
        inventory.setItem(47, getItem(Material.PAPER, 1,
                "&b当前页数 " + page, "&e左键切换上页", "&e右键跳转首页"));
        inventory.setItem(48, PANE);
        inventory.setItem(49, getItem(cop.getMaterial(), 1,
                "&b" + cop.getName(),
                "&b仓库库存: &e" + count, "&e点击取出该页作物"));
        inventory.setItem(50, PANE);
        inventory.setItem(51, getItem(Material.PAPER, 1,
                "&b当前页数 " + page, "&e左键切换下页", "&e右键快速翻页"));
        inventory.setItem(52, PANE);
        inventory.setItem(53, getItem(Material.RED_STAINED_GLASS_PANE, 1,
                "&c关闭菜单"));

        int itemsPerPage = 45;
        int maxStackSize = cop.getMaterial().getMaxStackSize();
        int startIndex = (page - 1) * itemsPerPage * maxStackSize;
        int endIndex = page * itemsPerPage * maxStackSize;
        if (endIndex > count) endIndex = count;
        int pageCount = endIndex - startIndex;
        ItemStack copItem = new ItemStack(cop.getMaterial());
        while (pageCount > 0) {
            int amount = Math.min(pageCount, maxStackSize);
            pageCount -= amount;
            copItem.setAmount(amount);
            inventory.addItem(copItem.clone());
        }
        player.openInventory(inventory);
    }

    public static int getInventoryAirCount(Player player) {
        int count = 0;
        for (int i = 0; i < 36; i++) {
            if (player.getInventory().getItem(i) == null) {
                count++;
            }
        }
        return count;
    }

    public static void save(Player player, Inventory inventory) {
        ItemStack copItem = inventory.getItem(49);
        if (copItem == null) return;
        Cop cop = new Cop(copItem.getType().toString());
        if (cop.getMaterial() == Material.AIR) return;
        int page = Integer.parseInt(Objects.requireNonNull(inventory.getItem(47)).getItemMeta().getDisplayName().split(" ")[1]);
        Map<String,String> playerMap = plugin.getData().selectMap(SQLKey.PLAYER,
                new SQLFilter().where(SQLKey.PLAYER, player.getName()));
        int totalItems = Integer.parseInt(playerMap.getOrDefault(cop.getMaterial().toString().toUpperCase(), "0"));
        int itemsPerPage = 45;
        int maxStackSize = cop.getMaterial().getMaxStackSize();
        int startIndex = (page - 1) * itemsPerPage * maxStackSize;
        int endIndex = page * itemsPerPage * maxStackSize;
        if (endIndex > totalItems) endIndex = totalItems;
        int pageCount = endIndex - startIndex;
        if (pageCount < 0) pageCount = 0;
        int count = 0;
        for (int i = 0; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null) {
                if (item.getType() == cop.getMaterial()) {
                    count += Objects.requireNonNull(inventory.getItem(i)).getAmount();
                } else {
                    inventory.setItem(i, new ItemStack(Material.AIR));
                    if (BasicUtil.isInventoryFull(player)) {
                        player.getWorld().dropItem(player.getLocation(), item);
                        player.sendMessage(THY_PREFIX + color("&c请勿放入非作物,已掉落地面"));
                    } else {
                        player.getInventory().addItem(item);
                        player.sendMessage(THY_PREFIX + color("&c请勿放入非作物,已返还背包"));
                    }
                }
            }
        }
        if (count > pageCount) {
            totalItems += count - pageCount;
        } else if (count < pageCount) {
            totalItems -= pageCount - count;
        }
        plugin.getData().update(SQLKey.PLAYER,
                new SQLFilter()
                        .set(cop.getMaterial().toString().toUpperCase(), totalItems)
                        .where(playerMap.get(SQLKey.ID)));
        plugin.saveConfig();
        inventory.setItem(49, new ItemStack(Material.AIR));
    }

    /**
     * 输出日志
     * @param text 内容
     */
    public static void print(String text){
        Bukkit.getConsoleSender().sendMessage(color(text));
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
            print("&b[作物仓库] §c加载文本错误");
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
            print("&b[作物仓库] §c加载文本错误");
        }else {
            text=text.replace("&", "§");
        }
        return text;
    }

    /**
     * 获取物品
     */
    public static ItemStack getItem(Material material, int count) {
        return new ItemStack(material, count);
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
