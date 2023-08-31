package cc.ranmc.farm.inventory;

import org.bukkit.OfflinePlayer;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

public class Papi extends PlaceholderExpansion {
    
	private final Main plugin;

    public Papi(Main plugin) {
        this.plugin = plugin;
    }
	
    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public @NotNull String getAuthor(){
        return "Ranica";
    }

    @Override
    public @NotNull String getIdentifier(){
        return "fm";
    }

    @Override
    public @NotNull String getVersion(){
        return "Beta";
    }
  
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier){
    	
        if (!player.isOnline()) return "&c目标错误";
        return plugin.getConfig().getString(player.getName()+"#"+identifier,"0");
    }
}