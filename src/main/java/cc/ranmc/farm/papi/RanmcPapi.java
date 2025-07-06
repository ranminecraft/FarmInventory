package cc.ranmc.farm.papi;

import cc.ranmc.farm.Main;
import cc.ranmc.papi.bean.PapiHandler;
import org.bukkit.entity.Player;

public class RanmcPapi extends PapiHandler {

    public RanmcPapi(String prefix) {
        super(prefix);
    }

    @Override
    public String handle(Player player, String identifier) {
        if (!player.isOnline()) return "&c目标错误";
        return Main.getInstance().getConfig().getString(player.getName()+"#"+identifier,"0");
    }
}
