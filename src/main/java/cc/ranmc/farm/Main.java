package cc.ranmc.farm;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import cc.ranmc.farm.constant.SQLKey;
import cc.ranmc.farm.listener.FarmListener;
import cc.ranmc.farm.papi.Papi;
import cc.ranmc.farm.papi.RanmcPapi;
import cc.ranmc.farm.sql.Database;
import cc.ranmc.farm.sql.SQLFilter;
import cc.ranmc.papi.PapiAPI;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import static cc.ranmc.farm.util.FarmUtil.color;
import static cc.ranmc.farm.util.FarmUtil.openCropGUI;
import static cc.ranmc.farm.util.FarmUtil.print;

public class Main extends JavaPlugin implements Listener {

	@Getter
	private static Main instance;
	@Getter
	private boolean ranmc = false;
	// 数据库
	@Getter
	private Database data;
	
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
	    
	    // 加载配置
	    loadConfig();

		data = new Database();
	    
	    // 注册 Event
        Bukkit.getPluginManager().registerEvents(new FarmListener(), this);
        
		super.onEnable();
	}
	
	/**
	 * 插件卸载
	 */
	@Override
	public void onDisable() {
		data.close();
		print("&b[作物仓库] §a已经成功卸载");
		super.onDisable();
	}
	
	/**
	 * 指令控制
	 */
	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){
		
		if (!(sender instanceof Player player)) {
        	print(color("&b[作物仓库] &c该指令不能在控制台输入"));
        	return true;
		}

        if (cmd.getName().equalsIgnoreCase("fm") && args.length == 1){
			if(args[0].equalsIgnoreCase("reload")) {
				if (sender.hasPermission("fm.admin")) {
					loadConfig();
					player.sendMessage(color("&b[作物仓库] &a重载完成"));
                } else {
					player.sendMessage(color("&b[作物仓库] &c你没有权限这样做"));
                }
                return true;
            }
			
			if(args[0].equalsIgnoreCase("switch")) {
				if (sender.hasPermission("fm.user")) {
					Map<String,String> playerMap = data.selectMap(SQLKey.PLAYER,
							new SQLFilter().where(SQLKey.PLAYER, player.getName()));
					boolean isOpen = playerMap.getOrDefault(SQLKey.OPEN, "1").equals("0");
					if (isOpen) {
						data.selectMap(SQLKey.PLAYER,
								new SQLFilter()
										.set(SQLKey.OPEN, "0")
										.where(playerMap.get(SQLKey.ID)));
						player.sendMessage(color("&b桃花源>>>&c你已关闭作物仓库"));
					} else {
						data.selectMap(SQLKey.PLAYER,
								new SQLFilter()
										.set(SQLKey.OPEN, "1")
										.where(playerMap.get(SQLKey.ID)));
						player.sendMessage(color("&b桃花源>>>&a你已打开作物仓库"));
					}
					saveConfig();
                } else {
					player.sendMessage(color("&b[作物仓库] &c你没有权限这样做"));
                }
                return true;
            }
			
			if (sender.hasPermission("fm.user")) {
				openCropGUI(player, args[0], 1);
            } else {
				sender.sendMessage("§c你没有权限这么做");
            }
            return true;
        }
		
		sender.sendMessage("§c未知指令");
		return true;
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
	 * 加载配置
	 */
	public void loadConfig(){
        if (!new File(getDataFolder() + File.separator + "config.yml").exists()) {
        	saveDefaultConfig();
        }
        reloadConfig();

		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new Papi().register();
			print("&b[作物仓库] §a成功载入PlaceholderAPI");
		}

		if (Bukkit.getPluginManager().getPlugin("Ranmc") != null) {
			PapiAPI.registerHandler(new RanmcPapi("fm"));
			print("&b[作物仓库] §a成功载入Ranmc");
			ranmc = true;
		}
	}
	
}
