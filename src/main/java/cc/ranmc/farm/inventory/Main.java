package cc.ranmc.farm.inventory;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

import static cc.ranmc.farm.inventory.Util.color;
import static cc.ranmc.farm.inventory.Util.getItem;
import static cc.ranmc.farm.inventory.Util.print;

public class Main extends JavaPlugin implements Listener{
	
	// 收集掉落物的农作物列表
	private static final List<Material> CROP_TYPE = Arrays.asList(
			Material.POTATO,
			Material.CARROT,
			Material.WHEAT,
			Material.WHEAT_SEEDS,
			Material.BEETROOT,
			Material.BEETROOT_SEEDS,
			Material.NETHER_WART,
			Material.PUMPKIN);
	// PAPI变量
	public Papi papi;
	// 板
	private static final ItemStack PANE = getItem(Material.GRAY_STAINED_GLASS_PANE, 1, " ");
	// 提示
	private List<String> noteList = new ArrayList<>();
	
	/**
	 * 插件启动
	 */
	@Override
	public void onEnable() {
		color("§e-----------------------");
		color("§bFarmInventory §dBy RanWhite");
		color("§bVersion: " + getDescription().getVersion());
		color("§chttp://www.ranmc.cc/");
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
		color("§b[FM] §a已经成功卸载");
		super.onDisable();
	}
	
	/**
	 * 指令控制
	 */
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){
		
		if (!(sender instanceof Player)) {
        	print(color("&b[FM] &c该指令不能在控制台输入"));
        	return true;
		}
	 
		Player p = (Player) sender;
		
		if (cmd.getName().equalsIgnoreCase("fm") && args.length == 1){
			if(args[0].equalsIgnoreCase("reload")) {
				if (sender.hasPermission("fm.admin")) {
					LoadConfig();
					p.sendMessage(color("&b[FM] &a重载完成"));
					return true;
				} else {
					p.sendMessage(color("&b[FM] &c你没有权限这样做"));
					return true;
				}
			}
			
			if(args[0].equalsIgnoreCase("switch")) {
				if (sender.hasPermission("fm.user")) {
					boolean isOpen = getConfig().getBoolean(p.getName()+"#OPEN",true);
					if(isOpen) {
						getConfig().set(p.getName()+"#OPEN", false);
						p.sendMessage(color("&b桃花源>>>&c你已关闭作物仓库"));
					} else {
						getConfig().set(p.getName()+"#OPEN", true);
						p.sendMessage(color("&b桃花源>>>&a你已打开作物仓库"));
					}
					saveConfig();
					return true;
				} else {
					p.sendMessage(color("&b[FM] &c你没有权限这样做"));
					return true;
				}
			}
			
			if (sender.hasPermission("fm.user")) {
				openCropGUI(p, args[0].toUpperCase(), 1);
				return true;
			} else {
				sender.sendMessage("§c你没有权限这么做");
				return true;
			}
		}
		
		sender.sendMessage("§c未知指令");
		return true;
	}
	
	/**
	 * 打开农作物仓库菜单
	 */
	public void openCropGUI(Player player, String crop, int page) {
		if (page > 20 && !player.hasPermission("thy.vip")) page = 20;
		if (page > 50 && !player.hasPermission("thy.svip")) page = 50;
		if (page > 100) page = 100;
		if (page < 1) page = 1;
		int count = this.getConfig().getInt(player.getName() + "#" + crop);
		Inventory inventory = Bukkit.createInventory(null, 54, color("&d&l桃花源丨作物仓库"));

		Cop cop = new Cop(crop);
		if (cop.getMaterial() == Material.AIR) {
			player.sendMessage(color("&b桃花源>>>&c没有找到这个农作物"));
			return;
		}

		inventory.setItem(45, getItem(Material.RED_STAINED_GLASS_PANE, 1, "&c返回菜单"));
		inventory.setItem(46, PANE);
		inventory.setItem(47, getItem(Material.PAPER, 1, "&b当前页数 " + page, "&e左键切换上页", "&e右键快速翻页"));
		inventory.setItem(48, PANE);
		inventory.setItem(49, getItem(cop.getMaterial(), 1, "&b" + cop.getName(), "&e仓库库存: " + count, "&e不要放其他东西哦", "&e否则丢了后果自负"));
		inventory.setItem(50, PANE);
		inventory.setItem(51, getItem(Material.PAPER, 1, "&b当前页数 " + page, "&e左键切换上页", "&e右键快速翻页"));
		inventory.setItem(52, PANE);
		inventory.setItem(53, getItem(Material.RED_STAINED_GLASS_PANE, 1, "&c关闭菜单"));

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
			if (event.getRawSlot() >= 45 && inventory != player.getInventory()) event.setCancelled(true);
			if (clicked == null && inventory == player.getInventory()) return;

			if (event.getRawSlot() == 53) {
				player.closeInventory();
				return;
			}
			if (event.getRawSlot() == 45) {
				save(player, inventory);
				player.chat("/cd");
				return;
			}
			if (event.getRawSlot() == 47) {
				int page = Integer.parseInt(clicked.getItemMeta().getDisplayName().split(" ")[1]);
				if (event.getClick().isLeftClick()) page--;
				if (event.getClick().isRightClick()) page -= 10;
				save(player, inventory);
				openCropGUI(player, inventory.getItem(49).getType().toString(), page);
				return;
			}
			if (event.getRawSlot() == 51) {
				int page = Integer.parseInt(clicked.getItemMeta().getDisplayName().split(" ")[1]);
				if (event.getClick().isLeftClick()) page++;
				if (event.getClick().isRightClick()) page += 10;
				save(player, inventory);
				openCropGUI(player, inventory.getItem(49).getType().toString(), page);
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
		Cop cop = new Cop(inventory.getItem(49).getType().toString());
		int page = Integer.parseInt(inventory.getItem(47).getItemMeta().getDisplayName().split(" ")[1]);
		int totalItems = getConfig().getInt(player.getName() + "#" + cop.getMaterial().toString());
		int itemsPerPage = 45;
		int maxStackSize = cop.getMaterial().getMaxStackSize();
		int startIndex = (page - 1) * itemsPerPage * maxStackSize;
		int endIndex = page * itemsPerPage * maxStackSize;
		if (endIndex > totalItems) endIndex = totalItems;
		int pageCount = endIndex - startIndex;
		if (pageCount < 0) pageCount = 0;

		if (cop.getMaterial() != Material.AIR) {
			int count = 0;
			for (int i = 0; i < 45; i++) {
				ItemStack item = inventory.getItem(i);
				if (item != null) {
					if (item.getType() == cop.getMaterial()) {
						count += inventory.getItem(i).getAmount();
					} else {
						inventory.setItem(i, new ItemStack(Material.AIR));
						player.getInventory().addItem(item);
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
		}
	}
	
	/**
	 * 储存掉落物
	 * @param event 事件
	 */
	@EventHandler
	public void onBlockDropItemEvent(BlockDropItemEvent event) {
		Player player = event.getPlayer();
		print(player.getName());
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
			if (!noteList.contains(player.getName())) {
				Bukkit.getGlobalRegionScheduler().runDelayed(this, task -> {
					player.sendMessage(color("&b桃花源>>>&a全部作物已存放仓库,打开菜单查看吧"));
					noteList.remove(player.getName());
				}, 200);
			}
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
        
        // 检测PAPI
        if( Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")){
            papi = new Papi(this);
            papi.register();
			print("§b[FM] §a成功载入PlaceholderAPI");
        } else {
        	print("§b[FM] §c无法加载PlaceholderAPI,可能会影响正常使用");
        }
	}
	
}
