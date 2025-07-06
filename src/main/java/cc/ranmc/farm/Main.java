package cc.ranmc.farm;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cc.ranmc.farm.bean.Cop;
import cc.ranmc.farm.papi.Papi;
import cc.ranmc.farm.papi.RanmcPapi;
import cc.ranmc.papi.PapiAPI;
import cc.ranmc.utils.BasicUtil;
import cc.ranmc.utils.MenuUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import static cc.ranmc.farm.util.FarmUtil.color;
import static cc.ranmc.farm.util.FarmUtil.getItem;
import static cc.ranmc.farm.util.FarmUtil.print;
import static cc.ranmc.utils.BasicUtil.THY_PREFIX;

public class Main extends JavaPlugin implements Listener {
	
	// 收集掉落物的农作物列表
	private static final List<Material> CROP_TYPE = Arrays.asList(
			Material.CACTUS,
			Material.POTATO,
			Material.CARROT,
			Material.WHEAT,
			Material.WHEAT_SEEDS,
			Material.BEETROOT,
			Material.BEETROOT_SEEDS,
			Material.NETHER_WART,
			Material.PUMPKIN);
	// 板
	private static final ItemStack PANE = getItem(Material.GRAY_STAINED_GLASS_PANE, 1, " ");
	// 提示
	private final List<String> noteList = new ArrayList<>();
	@Getter
	private static Main instance;
	private boolean ranmc = false;
	
	/**
	 * 插件启动
	 */
	@Override
	public void onEnable() {
		instance = this;
		color("§e-----------------------");
		color("§bFarmInventory §dBy RanWhite");
		color("§bVersion: " + getDescription().getVersion());
		color("§chttps://www.ranmc.cc/");
		color("§e-----------------------");
	    
	    //加载配置
	    LoadConfig();
	    
	    //注册Event
        Bukkit.getPluginManager().registerEvents(this, this);
        
		super.onEnable();
	}
	
	/**
	 * 插件卸载
	 */
	@Override
	public void onDisable() {
		color("§b[FarmInventory] §a已经成功卸载");
		super.onDisable();
	}
	
	/**
	 * 指令控制
	 */
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){
		
		if (!(sender instanceof Player p)) {
        	print(color("&b[FM] &c该指令不能在控制台输入"));
        	return true;
		}

        if (cmd.getName().equalsIgnoreCase("fm") && args.length == 1){
			if(args[0].equalsIgnoreCase("reload")) {
				if (sender.hasPermission("fm.admin")) {
					LoadConfig();
					p.sendMessage(color("&b[FM] &a重载完成"));
                } else {
					p.sendMessage(color("&b[FM] &c你没有权限这样做"));
                }
                return true;
            }
			
			if(args[0].equalsIgnoreCase("switch")) {
				if (sender.hasPermission("fm.user")) {
					boolean isOpen = getConfig().getBoolean(p.getName() + "#OPEN",true);
					if(isOpen) {
						getConfig().set(p.getName()+"#OPEN", false);
						p.sendMessage(color("&b桃花源>>>&c你已关闭作物仓库"));
					} else {
						getConfig().set(p.getName()+"#OPEN", true);
						p.sendMessage(color("&b桃花源>>>&a你已打开作物仓库"));
					}
					saveConfig();
                } else {
					p.sendMessage(color("&b[FM] &c你没有权限这样做"));
                }
                return true;
            }
			
			if (sender.hasPermission("fm.user")) {
				openCropGUI(p, args[0].toUpperCase(), 1);
            } else {
				sender.sendMessage("§c你没有权限这么做");
            }
            return true;
        }
		
		sender.sendMessage("§c未知指令");
		return true;
	}
	
	/**
	 * 打开农作物仓库菜单
	 */
	public void openCropGUI(Player player, String crop, int page) {
		if (page > 20 && !player.hasPermission("ranmc.vip")) page = 20;
		if (page > 30 && !player.hasPermission("ranmc.svip")) page = 30;
		if (page > 50) page = 50;
		if (page < 1) page = 1;
		int count = this.getConfig().getInt(player.getName() + "#" + crop);
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
				save(player, inventory);
				if (ranmc) {
					MenuUtil.open(player, "farm");
				} else player.closeInventory();
				return;
			}
			ItemStack item = inventory.getItem(49);
			if (item == null || item.getType() == Material.AIR) return;
			String copType = item.getType().toString();
			if (event.getRawSlot() == 47) {
				int page = Integer.parseInt(clicked.getItemMeta().getDisplayName().split(" ")[1]);
				if (event.getClick().isLeftClick()) page--;
				if (event.getClick().isRightClick()) page = 1;
				save(player, inventory);
				openCropGUI(player, copType, page);
				return;
			}
			if (event.getRawSlot() == 51) {
				int page = Integer.parseInt(clicked.getItemMeta().getDisplayName().split(" ")[1]);
				if (event.getClick().isLeftClick()) page++;
				if (event.getClick().isRightClick()) page += 10;
				save(player, inventory);
				openCropGUI(player, copType, page);
			}
		}
		
	}
	
	/**
	 * 命令补全
	 */
	@Override
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, String alias, String[] args) {
		if (alias.equalsIgnoreCase("fm") && args.length == 1 && sender.hasPermission("fm.user")) {
			return List.of("switch");
		}
		return new ArrayList<>();
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
		save(player, inventory);
	}

	public void save(Player player, Inventory inventory) {
		ItemStack copItem = inventory.getItem(49);
		if (copItem == null) return;
		Cop cop = new Cop(copItem.getType().toString());
		if (cop.getMaterial() == Material.AIR) return;
		int page = Integer.parseInt(inventory.getItem(47).getItemMeta().getDisplayName().split(" ")[1]);
		int totalItems = getConfig().getInt(player.getName() + "#" + cop.getMaterial().toString());
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
					count += inventory.getItem(i).getAmount();
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
		this.getConfig().set(player.getName() + "#" + cop.getMaterial().toString(), totalItems);
		saveConfig();
		inventory.setItem(49, new ItemStack(Material.AIR));
	}
	
	/**
	 * 储存掉落物
	 * @param event 事件
	 */
	@EventHandler
	public void onBlockDropItemEvent(BlockDropItemEvent event) {
		Player player = event.getPlayer();
		if (!getConfig().getBoolean(player.getName()+"#OPEN",true)) return;
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
            for (Item value : items) {
                ItemStack item = value.getItemStack();
                String info = player.getName() + "#" + item.getType();
                getConfig().set(info, this.getConfig().getInt(info) + item.getAmount());
            }
			saveConfig();
			Bukkit.getGlobalRegionScheduler().runDelayed(this, task -> {
				noteList.remove(player.getName());
				if (!noteList.contains(player.getName())) {
					player.sendMessage(color("&b桃花源>>>&a作物已存放仓库,打开菜单查看吧"));
				}
			}, 100);
			noteList.add(player.getName());
			event.setCancelled(true);
		}
	}
	
	/**
	 * 加载配置
	 */
	public void LoadConfig(){
        if (!new File(getDataFolder() + File.separator + "config.yml").exists()) {
        	saveDefaultConfig();
        }
        reloadConfig();

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new Papi(this).register();
			print("§b[FarmInventory] §a成功载入PlaceholderAPI");
		}

		if (Bukkit.getPluginManager().getPlugin("Ranmc") != null) {
			PapiAPI.registerHandler(new RanmcPapi("fm"));
			print("§b[FarmInventory] §a成功载入Ranmc");
			ranmc = true;
		}
	}
	
}
