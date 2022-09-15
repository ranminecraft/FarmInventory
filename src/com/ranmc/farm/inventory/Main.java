package com.ranmc.farm.inventory;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Chest;
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
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class Main extends JavaPlugin implements Listener{
	
	//农作物列表
	private List<String> CropList = new ArrayList<>();
	//PAPI变量
	public Papi papi;
	//计时器
	private BukkitTask task;
	//提示名单
	private Map<String,Integer> noteMap = new HashMap<>();
	
	/**
	 * 插件启动
	 */
	@Override
	public void onEnable() {
		outPut("§e-----------------------");
		outPut("§bFarmInventory §dBy RanWhite");
		outPut("§bVersion: "+getDescription().getVersion());
		outPut("§chttp://www.ranmc.cn");
	    outPut("§e-----------------------");
	    
	    //加载配置
	    LoadConfig();
	    
	    //注册Event
        Bukkit.getPluginManager().registerEvents(this, this);
        
        task = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
        	@Override
			public void run() {
        		for(Player player:Bukkit.getOnlinePlayers()) {
        			if(noteMap.containsKey(player.getName())) {
        				int count = noteMap.get(player.getName());
        				if(count > 0) {
        					count--;
        				}
        				if(count == 1) {
        					player.sendMessage(textReplace("&b桃花源>>>&a全部作物已存放仓库,打开菜单查看吧"));
        				}
        				noteMap.put(player.getName(),count);
        			}
        		}
        		
        	}
        }, 40, 40);
        
		super.onEnable();
	}
	
	/**
	 * 插件卸载
	 */
	@Override
	public void onDisable() {
		outPut("§b[FM] §a已经成功卸载");
		task.cancel();
		super.onDisable();
	}
	
	/**
	 * 指令控制
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
		
		if (!(sender instanceof Player)) {
        	outPut(textReplace("&b[FM] &c该指令不能在控制台输入"));
        	return true;
		}
	 
		Player p = (Player) sender;
		
		if (cmd.getName().equalsIgnoreCase("fm") && args.length == 1){
			if(args[0].equalsIgnoreCase("reload")) {
				if (sender.hasPermission("fm.admin")) {
					LoadConfig();
					p.sendMessage(textReplace("&b[FM] &a重载完成"));
					return true;
				} else {
					p.sendMessage(textReplace("&b[FM] &c你没有权限这样做"));
					return true;
				}
			}
			
			if(args[0].equalsIgnoreCase("switch")) {
				if (sender.hasPermission("fm.user")) {
					boolean isOpen = getConfig().getBoolean(p.getName()+"#OPEN",true);
					if(isOpen) {
						getConfig().set(p.getName()+"#OPEN", false);
						p.sendMessage(textReplace("&b桃花源>>>&c你已关闭作物仓库"));
					} else {
						getConfig().set(p.getName()+"#OPEN", true);
						p.sendMessage(textReplace("&b桃花源>>>&a你已打开作物仓库"));
					}
					saveConfig();
					return true;
				} else {
					p.sendMessage(textReplace("&b[FM] &c你没有权限这样做"));
					return true;
				}
			}
			
			if (sender.hasPermission("fm.user")) {
				openCropGUI(p, args[0]);
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
	public void openCropGUI(Player p,String crop) {
		int count = this.getConfig().getInt(p.getName()+"#"+crop);
		Inventory inventory = Bukkit.createInventory(null, 54, textReplace("&d&l桃花源丨作物仓库"));
		
		ItemStack item2 = new ItemStack(Material.WHITE_STAINED_GLASS_PANE, 1);
        ItemMeta meta2 = item2.getItemMeta();
        meta2.setDisplayName("");
        item2.setItemMeta(meta2);
        inventory.setItem(1, item2);
        inventory.setItem(2, item2);
        inventory.setItem(3, item2);
        inventory.setItem(5, item2);
        inventory.setItem(6, item2);
        inventory.setItem(7, item2);
        
        ItemStack item3 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
		ItemMeta meta3 = item3.getItemMeta();
		meta3.setDisplayName(textReplace("&b关闭菜单"));
        item3.setItemMeta(meta3);
		inventory.setItem(0, item3);
		inventory.setItem(8, item3);
        
        /*
        ItemStack item3 = new ItemStack(Material.ARROW);
		ItemMeta meta3 = item3.getItemMeta();
		meta3.setDisplayName(textReplace("&b上一页"));
        item3.setItemMeta(meta3);
		inventory.setItem(0, item3);
		
		ItemStack item4 = new ItemStack(Material.ARROW);
		ItemMeta meta4 = item4.getItemMeta();
		meta4.setDisplayName(textReplace("&b下一页"));
        item4.setItemMeta(meta4);
		inventory.setItem(8, item4);*/
		
		Material material = Material.AIR;
		String copsName = "未知";
		
		switch (crop) {
		case "POTATO":
			material = Material.POTATO;
			copsName = "马铃薯";
			break;
		case "CARROT":
			material = Material.CARROT;
			copsName = "胡萝卜";
			break;
		case "WHEAT":
			material = Material.WHEAT;
			copsName = "小麦";
			break;
		case "WHEAT_SEEDS":
			material = Material.WHEAT_SEEDS;
			copsName = "小麦种子";
			break;
		case "BEETROOT":
			material = Material.BEETROOT;
			copsName = "甜菜";
			break;
		case "BEETROOT_SEEDS":
			material = Material.BEETROOT_SEEDS;
			copsName = "甜菜种子";
			break;
		case "NETHER_WART":
			material = Material.NETHER_WART;
			copsName = "地狱疣";
			break;

		default:
			break;
		}
		
		ItemStack item = new ItemStack(Material.CHEST_MINECART);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(textReplace("&b"+copsName));
		ArrayList<String> Lore = new ArrayList<String>();
        Lore.add(textReplace("&e仓库库存:&c"+count));
        Lore.add(textReplace("&c不要放其他东西哦"));
        meta.setLore(Lore);
        item.setItemMeta(meta);
		inventory.setItem(4, item);
		
		ItemStack copsitem = new ItemStack(material);
		
		for (int i = 0; i < count; i++) {
			if(i < 2880) {
				inventory.addItem(copsitem);
			}
		}
		
		p.openInventory(inventory);
		
	}
	
	/**
	 * 菜单点击
	 * @param event
	 */
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		
		Player p = (Player) event.getWhoClicked();
		ItemStack clicked = event.getCurrentItem();
		if(clicked==null) {
			return;
		}
		
		if(event.getView().getTitle().equals(textReplace("&d&l桃花源丨作物仓库"))) {
			if(event.getRawSlot()<9) {
				event.setCancelled(true);
			}
			
			if(event.getRawSlot()==0 || event.getRawSlot()==8) {
				p.closeInventory();
			}
		}
		
	}
	
	/**
	 * 命令补全
	 */
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> cmdls = new ArrayList<String>();
		if (alias.equalsIgnoreCase("fm") && args.length == 1 && sender.hasPermission("fm.user")) {
			cmdls = CropList;
		}
		
		return cmdls;
	}
	
	/**
	 * 菜单关闭
	 * @param event
	 */
	@EventHandler
	public void onInventoryClose(InventoryCloseEvent event) {
		if(!event.getView().getTitle().equals(textReplace("&d&l桃花源丨作物仓库"))) {
			return;
		}
		Player player = (Player) event.getPlayer();
		Inventory inventory = event.getInventory();
		Material material = Material.AIR;
		String copsType = ChatColor.stripColor(inventory.getItem(4).getItemMeta().getDisplayName());
		String saveName = "WARN";
		switch (copsType) {
			case "马铃薯":
				material = Material.POTATO;
				saveName = "POTATO";
				break;
			case "胡萝卜":
				material = Material.CARROT;
				saveName = "CARROT";
				break;
			case "小麦":
				material = Material.WHEAT;
				saveName = "WHEAT";
				break;
			case "小麦种子":
				material = Material.WHEAT_SEEDS;
				saveName = "WHEAT_SEEDS";
				break;
			case "甜菜":
				material = Material.BEETROOT;
				saveName = "BEETROOT";
				break;
			case "甜菜种子":
				material = Material.BEETROOT_SEEDS;
				saveName = "BEETROOT_SEEDS";
				break;
			case "地狱疣":
				material = Material.NETHER_WART;
				saveName = "NETHER_WART";
				break;
			default:
				break;
		}
		
		if(material != Material.AIR) {
			int count = 0;
			for (int i = 9; i < inventory.getSize(); i++) {
				if(inventory.getItem(i)!=null) {
					if(inventory.getItem(i).getType()==material) {
						count += inventory.getItem(i).getAmount();
					}
				}
			}
			
			int let = this.getConfig().getInt(player.getName()+"#"+saveName);
			if(let>2880) {
				count = let-2880+count;
			}
			
			this.getConfig().set(player.getName()+"#"+saveName, count);
			saveConfig();
		}
		
		
	}
	
	/**
	 * 储存掉落物
	 * @param event
	 */
	@EventHandler
	public void onBlockDropItemEvent(BlockDropItemEvent event) {
		Player player = event.getPlayer();
		if(player==null||!getConfig().getBoolean(player.getName()+"#OPEN",true)) return;
		boolean isCrop = true;
		List<Item> items = event.getItems();
		if(items.size()==0) return;
		for(int i=0;i<items.size();i++) {
			ItemStack item = items.get(i).getItemStack();
			if (!CropList.contains(item.getType().toString())) {
				isCrop = false;
			}
		}
		if(isCrop) {
			for(int i=0;i<items.size();i++) {
				ItemStack item = items.get(i).getItemStack();
				String info = player.getName()+"#"+item.getType();
				getConfig().set(info, this.getConfig().getInt(info)+item.getAmount());
			}
			saveConfig();
			noteMap.put(player.getName(), 3);
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
        
        CropList = getConfig().getStringList("CropList");
        
        //检测PAPI
        if( Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")){
            papi = new Papi(this);
            papi.register();
            outPut("§b[FM] §a成功载入PlaceholderAPI");
        } else {
        	outPut("§b[FM] §c无法加载PlaceholderAPI,可能会影响正常使用");
        }
	}
	
	/**
	 * 输出日志
	 * @param s
	 */
	public void outPut(String s){
        Bukkit.getConsoleSender().sendMessage(s);
	}
	
	/**
	 * 输入聊天
	 * @param s
	 */
	public void say(String s){
		Bukkit.broadcastMessage(s);
	}
	
	/**
	 * 文本替换
	 * @param text 内容
	 * @param p 玩家
	 * @return
	 */
	public String textReplace(String text,Player p) {
		if(text == null) {
			text = "";
			outPut("§b[FM] §c加载文本错误");
			}else {
				text=text.replace("&", "§")
					.replace("%player%",p.getName())
					.replace("%player_x%",""+p.getLocation().getBlockX())
					.replace("%player_y%",""+p.getLocation().getBlockY())
					.replace("%player_z%",""+p.getLocation().getBlockZ());
			}
		return text;
	}
	
	public String textReplace(String text) {
		if(text == null) {
			text = "";
			outPut("§b[FM] §c加载文本错误");
			}else {
				text=text.replace("&", "§");
			}
		return text;
	}
	
}
