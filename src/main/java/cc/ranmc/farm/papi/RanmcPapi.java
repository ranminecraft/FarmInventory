package cc.ranmc.farm.papi;

import cc.ranmc.farm.Main;
import cc.ranmc.farm.bean.SQLData;
import cc.ranmc.farm.constant.SQLKey;
import cc.ranmc.farm.bean.SQLFilter;
import cc.ranmc.papi.bean.PapiHandler;
import org.bukkit.entity.Player;

import java.util.Map;

public class RanmcPapi extends PapiHandler {

    private final Main plugin = Main.getInstance();

    public RanmcPapi(String prefix) {
        super(prefix);
    }

    @Override
    public String handle(Player player, String identifier) {
        if (player == null || !player.isOnline()) return "&c目标错误";
        SQLData playerMap = plugin.getData().selectMap(SQLKey.PLAYER,
                new SQLFilter().where(SQLKey.PLAYER, player.getName()));
        return String.valueOf(playerMap.getInt(identifier.toUpperCase(), 0));
    }
}
