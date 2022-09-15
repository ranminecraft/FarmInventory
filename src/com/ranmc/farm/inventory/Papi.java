package com.ranmc.farm.inventory;

import org.bukkit.OfflinePlayer;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class Papi extends PlaceholderExpansion {
	
	public String word,author,realtime,realdate;
    
	private final Main plugin;

    public Papi(Main plugin) {
        this.plugin = plugin;
    }
	
    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getAuthor(){
        return "RanWhite";
    }

    @Override
    public String getIdentifier(){
        return "fm";
    }

    @Override
    public String getVersion(){
        return "Beta";
    }
  
    @Override
    public String onRequest(OfflinePlayer player, String identifier){
    	
        if(!player.isOnline()) return "&c目标错误";
        return plugin.getConfig().getString(player.getName()+"#"+identifier,"0");
    }
}