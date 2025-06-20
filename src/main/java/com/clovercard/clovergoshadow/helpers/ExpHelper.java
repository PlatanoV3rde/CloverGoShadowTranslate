package com.clovercard.clovergoshadow.helpers;

import com.clovercard.clovergoshadow.config.Config;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class ExpHelper {
    public static int getLevelExp(int level){
        return (level * Config.CONFIG.getExpDifPerLevel()) + Config.CONFIG.getBaseExp();
    }

    public static void addExpToPlayer(ServerPlayerEntity player, int amount) {
        if(player == null) return;
        ServerScoreboard scoreboard = ServerLifecycleHooks.getCurrentServer().getScoreboard();
        ScoreObjective levelObj = scoreboard.getObjective("clshadowlevels");
        ScoreObjective expObj = scoreboard.getObjective("clshadowexp");

        if (levelObj == null) {
            scoreboard.addObjective("clshadowlevels", ScoreCriteria.DUMMY, new StringTextComponent("researchlevel"), ScoreCriteria.DUMMY.getDefaultRenderType());
            levelObj = scoreboard.getObjective("clshadowlevels");
        }
        if (expObj == null) {
            scoreboard.addObjective("clshadowexp", ScoreCriteria.DUMMY, new StringTextComponent("researchexp"), ScoreCriteria.DUMMY.getDefaultRenderType());
            expObj = scoreboard.getObjective("clshadowexp");
        }
        if(levelObj == null || expObj == null) return;

        Score plLevel = scoreboard.getOrCreatePlayerScore(player.getName().getString(), levelObj);
        Score plExp = scoreboard.getOrCreatePlayerScore(player.getName().getString(), expObj);
        int required = getLevelExp(plLevel.getScore());
        plExp.setScore(plExp.getScore() + amount);

        IFormattableTextComponent msg;
        if(Config.CONFIG.isUseTranslatables()) {
            msg = new TranslationTextComponent("clovergoshadow.expgain", amount);
        } else {
            msg = new StringTextComponent(TextFormatting.GREEN + "¡Has ganado " +
                TextFormatting.YELLOW + amount + TextFormatting.GREEN + " exp!");
        }
        player.sendMessage(msg, Util.NIL_UUID);

        while(plExp.getScore() > required) {
            plExp.setScore(plExp.getScore() - required);
            if(plLevel.getScore() < 50) {
                plLevel.setScore(plLevel.getScore() + 1);
                if(plLevel.getScore() != 0 && plLevel.getScore() % 10 == 0) {
                    ItemStack spawner = ItemHelper.makeLegendSpawner();
                    if(spawner != null) {
                        ServerPlayerEntity pl = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(player.getUUID());
                        if (pl != null) pl.inventory.add(spawner);
                    }
                }
            } else {
                plLevel.setScore(0);
                ItemStack spawner = ItemHelper.makeLegendSpawner();
                if(spawner != null) {
                    ServerPlayerEntity pl = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(player.getUUID());
                    if (pl != null) pl.inventory.add(spawner);
                }
            }
            required = getLevelExp(plLevel.getScore());

            IFormattableTextComponent levelMsg;
            if(Config.CONFIG.isUseTranslatables()) {
                levelMsg = new TranslationTextComponent("clovergoshadow.levelgain", plLevel.getScore());
            } else {
                levelMsg = new StringTextComponent(TextFormatting.AQUA + "¡Tu nivel de investigación ha subido a " +
                    TextFormatting.GOLD + plLevel.getScore() + TextFormatting.AQUA + "!");
            }
            player.sendMessage(levelMsg, Util.NIL_UUID);
        }
    }

    public static void addLevelToPlayer(ServerPlayerEntity player, int amount) {
        if(player == null) return;
        ServerScoreboard scoreboard = ServerLifecycleHooks.getCurrentServer().getScoreboard();
        ScoreObjective levelObj = scoreboard.getObjective("clshadowlevels");
        ScoreObjective expObj = scoreboard.getObjective("clshadowexp");

        if (levelObj == null) {
            scoreboard.addObjective("clshadowlevels", ScoreCriteria.DUMMY, new StringTextComponent("researchlevel"), ScoreCriteria.DUMMY.getDefaultRenderType());
            levelObj = scoreboard.getObjective("clshadowlevels");
        }
        if (expObj == null) {
            scoreboard.addObjective("clshadowexp", ScoreCriteria.DUMMY, new StringTextComponent("researchexp"), ScoreCriteria.DUMMY.getDefaultRenderType());
            expObj = scoreboard.getObjective("clshadowexp");
        }
        if(levelObj == null || expObj == null) return;

        Score plLevel = scoreboard.getOrCreatePlayerScore(player.getName().getString(), levelObj);
        Score plExp = scoreboard.getOrCreatePlayerScore(player.getName().getString(), expObj);
        int rewards = ((plLevel.getScore() + amount) % 10) - (plLevel.getScore() % 10);

        if(plLevel.getScore() + amount > 50) {
            plExp.setScore(0);
            plLevel.setScore(plLevel.getScore() + amount - 50);
        } else {
            plExp.setScore(0);
            plLevel.setScore(plLevel.getScore() + amount);
        }

        while(rewards > 0) {
            ItemStack spawner = ItemHelper.makeLegendSpawner();
            if(spawner != null) {
                ServerPlayerEntity pl = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(player.getUUID());
                if (pl != null) pl.inventory.add(spawner);
            }
            rewards--;
        }

        IFormattableTextComponent msg;
        if(Config.CONFIG.isUseTranslatables()) {
            msg = new TranslationTextComponent("clovergoshadow.levelgain", amount);
        } else {
            msg = new StringTextComponent(TextFormatting.AQUA + "¡Tu nivel de investigación ha aumentado en " +
                TextFormatting.GOLD + amount + TextFormatting.AQUA + "!");
        }
        player.sendMessage(msg, Util.NIL_UUID);
    }

    public static void setExpForPlayer(ServerPlayerEntity player, int amount) {
        if (player == null) return;
        ServerScoreboard scoreboard = ServerLifecycleHooks.getCurrentServer().getScoreboard();
        ScoreObjective levelObj = scoreboard.getObjective("clshadowlevels");
        ScoreObjective expObj = scoreboard.getObjective("clshadowexp");

        if (levelObj == null) {
            scoreboard.addObjective("clshadowlevels", ScoreCriteria.DUMMY, new StringTextComponent("researchlevel"), ScoreCriteria.DUMMY.getDefaultRenderType());
            levelObj = scoreboard.getObjective("clshadowlevels");
        }
        if (expObj == null) {
            scoreboard.addObjective("clshadowexp", ScoreCriteria.DUMMY, new StringTextComponent("researchexp"), ScoreCriteria.DUMMY.getDefaultRenderType());
            expObj = scoreboard.getObjective("clshadowexp");
        }
        if (levelObj == null || expObj == null) return;

        Score plLevel = scoreboard.getOrCreatePlayerScore(player.getName().getString(), levelObj);
        Score plExp = scoreboard.getOrCreatePlayerScore(player.getName().getString(), expObj);
        plExp.setScore(amount);

        IFormattableTextComponent msg;
        if (Config.CONFIG.isUseTranslatables()) {
            msg = new TranslationTextComponent("clovergoshadow.expset", amount);
        } else {
            msg = new StringTextComponent(TextFormatting.LIGHT_PURPLE + "¡Tu experiencia ha sido fijada en " +
                TextFormatting.YELLOW + amount + TextFormatting.LIGHT_PURPLE + "!");
        }
        player.sendMessage(msg, Util.NIL_UUID);
    }

    public static void setLevelForPlayer(ServerPlayerEntity player, int amount) {
        if(player == null) return;
        ServerScoreboard scoreboard = ServerLifecycleHooks.getCurrentServer().getScoreboard();
        ScoreObjective levelObj = scoreboard.getObjective("clshadowlevels");
        ScoreObjective expObj = scoreboard.getObjective("clshadowexp");

        if (levelObj == null) {
            scoreboard.addObjective("clshadowlevels", ScoreCriteria.DUMMY, new StringTextComponent("researchlevel"), ScoreCriteria.DUMMY.getDefaultRenderType());
            levelObj = scoreboard.getObjective("clshadowlevels");
        }
        if (expObj == null) {
            scoreboard.addObjective("clshadowexp", ScoreCriteria.DUMMY, new StringTextComponent("researchexp"), ScoreCriteria.DUMMY.getDefaultRenderType());
            expObj = scoreboard.getObjective("clshadowexp");
        }
        if(levelObj == null || expObj == null) return;

        Score plLevel = scoreboard.getOrCreatePlayerScore(player.getName().getString(), levelObj);
        Score plExp = scoreboard.getOrCreatePlayerScore(player.getName().getString(), expObj);
        if(amount > 50) amount = 50;
        plLevel.setScore(amount);
        plExp.setScore(0);

        IFormattableTextComponent msg;
        if(Config.CONFIG.isUseTranslatables()) {
            msg = new TranslationTextComponent("clovergoshadow.levelset", amount);
        } else {
            msg = new StringTextComponent(TextFormatting.LIGHT_PURPLE + "¡Tu nivel de investigación ha sido fijado en " +
                TextFormatting.GOLD + amount + TextFormatting.LIGHT_PURPLE + "!");
        }
        player.sendMessage(msg, Util.NIL_UUID);
    }
}
