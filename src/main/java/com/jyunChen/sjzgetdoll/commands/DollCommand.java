package com.jyunChen.sjzgetdoll.commands;

import com.jyunChen.sjzgetdoll.SJZ_GetDoll;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class DollCommand implements CommandExecutor, TabCompleter {

    private final SJZ_GetDoll plugin;

    public DollCommand(SJZ_GetDoll plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be run by a player.");
            return true;
        }

        if (args.length < 2) {
            player.sendMessage(Component.text("§6〚系統〛§7用法: /" + label + " <old|vip> <玩家名稱>", NamedTextColor.RED));
            return false;
        }

        String subCommand = args[0].toLowerCase();
        String targetPlayerName = args[1]; // 目標玩家的名稱，用於 NBT
        String executorPlayerName = player.getName(); // 執行者的名稱，用於 /give 目標

        if ("old".equals(subCommand)) {
            // --- 使用 targetPlayerName 構建 NBT ---
            String customNameNbt = String.format(
                "\"{\\\"text\\\":\\\"%s\\\",\\\"italic\\\":false,\\\"color\\\":\\\"#ccccff\\\",\\\"bold\\\":true}\"",
                targetPlayerName // 使用目標玩家名稱，並轉義 JSON
            );
            String loreNbt = "[\"{\\\"text\\\":\\\"老玩家專屬玩偶 :]\\\",\\\"italic\\\":false,\\\"color\\\":\\\"gray\\\"}\"]";
            String attributeNbt = "{modifiers:[{type:\"minecraft:armor\",amount:4d,slot:\"offhand\",id:\"1743511818291\",operation:\"add_value\"}]}";
            String itemModelNbt = String.format(
                "\"doll_%s\"",
                targetPlayerName.toLowerCase() // 轉為小寫，確保符合資源位置規範
            );

            // --- 構建最終的 give 指令，物品給予 executorPlayerName ---
            String giveCommand = String.format(
                "give %s minecraft:phantom_membrane[custom_name=%s,lore=%s,attribute_modifiers=%s,item_model=%s]",
                executorPlayerName, // 物品給予執行者
                customNameNbt,
                loreNbt,
                attributeNbt,
                itemModelNbt
            );

            // --- 使用 Global Region Scheduler 執行指令 ---
            final String finalGiveCommand = giveCommand;
            Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> {
                try {
                    plugin.getLogger().info("Attempting to execute command: " + finalGiveCommand);
                    boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalGiveCommand);
                    if (!success) {
                        plugin.getLogger().warning("Command dispatch returned false (command might be invalid or blocked).");
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Exception occurred while dispatching command: " + finalGiveCommand, e);
                }
            });

            // 提示訊息
            player.sendMessage(Component.text("§6〚系統〛§7已排程給予你 ", NamedTextColor.GREEN)
                    .append(Component.text(targetPlayerName, NamedTextColor.WHITE))
                    .append(Component.text(" §7的老玩家專屬玩偶！", NamedTextColor.GREEN)));

            return true;

        } else if ("vip".equals(subCommand)) {
            handleVipCommand(player);
            return true;

        } else {
            player.sendMessage(Component.text("§6〚系統〛§7無效的子指令! 用法: /" + label + " <old|vip> <玩家名稱>", NamedTextColor.RED));
            return false;
        }
    }

    private void handleVipCommand(Player player) {
        player.sendMessage(Component.text("§6〚系統〛§7VIP 玩偶功能目前尚未開放！", NamedTextColor.YELLOW));
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("sjz.getdoll")) {
            return Collections.emptyList();
        }
        List<String> completions = new ArrayList<>();
        List<String> possibilities = new ArrayList<>();
        if (args.length == 1) {
            possibilities.addAll(Arrays.asList("old", "vip"));
            StringUtil.copyPartialMatches(args[0], possibilities, completions);
        } else if (args.length == 2) {
            possibilities.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()));
            StringUtil.copyPartialMatches(args[1], possibilities, completions);
        }
        Collections.sort(completions);
        return completions;
    }
}