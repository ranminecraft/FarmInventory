package cc.ranmc.farm;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cc.ranmc.command.record.ICheckCommand;
import cc.ranmc.farm.command.FarmAutoComplete;
import cc.ranmc.farm.command.FarmCommand;
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

		// 注册指令
		Objects.requireNonNull(Bukkit.getPluginCommand("farm")).setExecutor(new FarmCommand());
		Objects.requireNonNull(Bukkit.getPluginCommand("fm")).setExecutor(new FarmCommand());
		Objects.requireNonNull(Bukkit.getPluginCommand("farm")).setTabCompleter(new FarmAutoComplete());
		Objects.requireNonNull(Bukkit.getPluginCommand("fm")).setTabCompleter(new FarmAutoComplete());
        
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
	 * 加载配置
	 */
	public void loadConfig(){
        /*if (!new File(getDataFolder() + File.separator + "config.yml").exists()) {
        	saveDefaultConfig();
        }
        reloadConfig();*/

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
