package cc.ranmc.farm.command;

import cc.ranmc.farm.Main;
import cc.ranmc.farm.constant.SQLKey;
import cc.ranmc.farm.sql.SQLFilter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static cc.ranmc.farm.util.FarmUtil.color;
import static cc.ranmc.farm.util.FarmUtil.openCropGUI;
import static cc.ranmc.farm.util.FarmUtil.print;

public class FarmCommand implements CommandExecutor {

    private static final Main plugin = Main.getInstance();
    /**
     * 指令控制
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args){

        if (args.length == 1) {
            if(args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("fm.admin")) {
                    plugin.loadConfig();
                    sender.sendMessage(color("&b[作物仓库] &a重载完成"));
                } else {
                    sender.sendMessage(color("&b[作物仓库] &c你没有权限这样做"));
                }
                return true;
            }

            if (!(sender instanceof Player player)) {
                print(color("&b[作物仓库] &c该指令不能在控制台输入"));
                return true;
            }

            if (!sender.hasPermission("fm.user")) {
                player.sendMessage(color("&b[作物仓库] &c你没有权限这样做"));
                return true;
            }

            if (args[0].equalsIgnoreCase("switch")) {
                Map<String,String> playerMap = plugin.getData().selectMap(SQLKey.PLAYER,
                        new SQLFilter().where(SQLKey.PLAYER, player.getName()));
                boolean isOpen = playerMap.getOrDefault(SQLKey.OPEN, "1").equals("1");
                plugin.getData().update(SQLKey.PLAYER,
                        new SQLFilter()
                                .set(SQLKey.OPEN, isOpen ? "0" : "1")
                                .where(playerMap.get(SQLKey.ID)));
                player.sendMessage(color("&b桃花源>>>&e你已" + (isOpen ? "关闭" : "打开") + "作物仓库"));
                return true;
            }

            openCropGUI(player, args[0], 1);
            return true;
        }

        sender.sendMessage("§c未知指令");
        return true;
    }
}
