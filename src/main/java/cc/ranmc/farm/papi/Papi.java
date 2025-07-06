package cc.ranmc.farm.papi;

import cc.ranmc.farm.Main;
import cc.ranmc.farm.constant.SQLKey;
import cc.ranmc.farm.sql.SQLFilter;
import org.bukkit.OfflinePlayer;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Papi extends PlaceholderExpansion {
    
	private final Main plugin = Main.getInstance();

    public Papi() {}
	
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
    	
        if (player == null || !player.isOnline()) return "&c目标错误";
        Map<String,String> playerMap = plugin.getData().selectMap(SQLKey.PLAYER,
                new SQLFilter().where(SQLKey.PLAYER, player.getName()));
        return playerMap.getOrDefault(identifier.toUpperCase(), "0");
    }
}