package cc.ranmc.farm.listener;

import cc.ranmc.farm.Main;
import cc.ranmc.farm.constant.SQLKey;
import cc.ranmc.farm.bean.SQLData;
import cc.ranmc.farm.bean.SQLFilter;
import cc.ranmc.farm.util.FarmUtil;
import cc.ranmc.utils.MenuUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static cc.ranmc.farm.constant.FarmConstant.CROP_TYPE;
import static cc.ranmc.farm.util.FarmUtil.color;
import static cc.ranmc.farm.util.FarmUtil.getInventoryAirCount;
import static cc.ranmc.farm.util.FarmUtil.openCropGUI;

public class FarmListener implements Listener {

    private static final Main plugin = Main.getInstance();
    // 提示
    private final List<String> noteList = new ArrayList<>();

    /**
     * 菜单关闭
     * @param event 事件
     */
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        SQLData playerMap = plugin.getData().selectMap(SQLKey.PLAYER,
                new SQLFilter().where(SQLKey.PLAYER, player.getName()));
        if (playerMap.isEmpty()) {
            SQLData parms = new SQLData();
            parms.set(SQLKey.PLAYER, player.getName());
            plugin.getData().insert(SQLKey.PLAYER, parms);
        }
    }

    /**
     * 菜单关闭
     * @param event 事件
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(color("&d&l桃花源丨作物仓库"))) {
            return;
        }
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        FarmUtil.save(player, inventory);
    }

    /**
     * 菜单点击
     * @param event 事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        if (event.getView().getTitle().equals(color("&d&l桃花源丨作物仓库"))) {
            Inventory inventory = event.getClickedInventory();
            if (inventory == null) return;
            if (event.getRawSlot() >= 45 &&
                    inventory != player.getInventory()) {
                event.setCancelled(true);
            }
            if (clicked == null &&
                    inventory == player.getInventory()) {
                return;
            }
            if (event.getRawSlot() == 49) {

                int airCount = getInventoryAirCount(player);

                for (int i = 0; i < 45; i++) {
                    ItemStack item = inventory.getItem(i);
                    if (item != null) {
                        if (airCount == 0) {
                            break;
                        }
                        inventory.setItem(i, new ItemStack(Material.AIR));
                        player.getInventory().addItem(item);
                        airCount--;
                    }
                }
                player.closeInventory();
                return;
            }

            if (event.getRawSlot() == 53) {
                player.closeInventory();
                return;
            }
            if (event.getRawSlot() == 45) {
                FarmUtil.save(player, inventory);
                if (Main.getInstance().isRanmc()) {
                    MenuUtil.open(player, "farm");
                } else player.closeInventory();
                return;
            }
            ItemStack item = inventory.getItem(49);
            if (item == null || item.getType() == Material.AIR) return;
            String copType = item.getType().toString();
            if (event.getRawSlot() == 47) {
                int page = Integer.parseInt(Objects.requireNonNull(clicked).getItemMeta().getDisplayName().split(" ")[1]);
                if (event.getClick().isLeftClick()) page--;
                if (event.getClick().isRightClick()) page = 1;
                FarmUtil.save(player, inventory);
                openCropGUI(player, copType, page);
                return;
            }
            if (event.getRawSlot() == 51) {
                int page = Integer.parseInt(Objects.requireNonNull(clicked).getItemMeta().getDisplayName().split(" ")[1]);
                if (event.getClick().isLeftClick()) page++;
                if (event.getClick().isRightClick()) page += 10;
                FarmUtil.save(player, inventory);
                openCropGUI(player, copType, page);
            }
        }

    }

    /**
     * 储存掉落物
     * @param event 事件
     */
    @EventHandler
    public void onBlockDropItemEvent(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        boolean isCrop = true;
        List<Item> items = event.getItems();
        if (items.isEmpty()) return;
        for (Item value : items) {
            ItemStack item = value.getItemStack();
            if (!CROP_TYPE.contains(item.getType())) {
                isCrop = false;
            }
        }
        if (isCrop) {
            SQLData playerMap = plugin.getData().selectMap(SQLKey.PLAYER,
                    new SQLFilter().where(SQLKey.PLAYER, player.getName()));
            if (!playerMap.getBoolean(SQLKey.OPEN, true)) return;
            Map<String,Integer> updateMap = new HashMap<>();
            for (Item value : items) {
                ItemStack item = value.getItemStack();
                String type = item.getType().toString().toUpperCase();
                updateMap.put(type,
                        updateMap.getOrDefault(type, playerMap.getInt(type, 0))
                         + item.getAmount());
            }
            SQLFilter filter = new SQLFilter();
            boolean first = true;
            for (String key : updateMap.keySet()) {
                if (first) {
                    filter.set(key, updateMap.get(key));
                    first = false;
                } else {
                    filter.andSet(key, updateMap.get(key));
                }
            }

            plugin.getData().update(SQLKey.PLAYER, filter.where(playerMap.getInt(SQLKey.ID)));
            plugin.saveConfig();
            Bukkit.getGlobalRegionScheduler().runDelayed(plugin, task -> {
                noteList.remove(player.getName());
                if (!noteList.contains(player.getName())) {
                    player.sendMessage(color("&b桃花源>>>&a作物已存放仓库,打开菜单查看吧"));
                }
            }, 100);
            noteList.add(player.getName());
            event.setCancelled(true);
        }
    }
}
