package cc.ranmc.farm.inventory;

import lombok.Data;
import org.bukkit.Material;

@Data
public class Cop {
    Material material;
    String name;

    public Cop(String type) {
        switch (type) {
            case "POTATO" -> {
                material = Material.POTATO;
                name = "马铃薯";
            }
            case "CARROT" -> {
                material = Material.CARROT;
                name = "胡萝卜";
            }
            case "WHEAT" -> {
                material = Material.WHEAT;
                name = "小麦";
            }
            case "WHEAT_SEEDS" -> {
                material = Material.WHEAT_SEEDS;
                name = "小麦种子";
            }
            case "BEETROOT" -> {
                material = Material.BEETROOT;
                name = "甜菜";
            }
            case "BEETROOT_SEEDS" -> {
                material = Material.BEETROOT_SEEDS;
                name = "甜菜种子";
            }
            case "NETHER_WART" -> {
                material = Material.NETHER_WART;
                name = "地狱疣";
            }
            case "PUMPKIN" -> {
                material = Material.PUMPKIN;
                name = "南瓜";
            }
            case "MELON" -> {
                material = Material.MELON;
                name = "西瓜";
            }
            default -> {
                material = Material.AIR;
                name = "未知";
            }
        }
    }
}
