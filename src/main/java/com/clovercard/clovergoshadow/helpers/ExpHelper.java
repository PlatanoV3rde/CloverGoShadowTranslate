// src/main/java/com/clovercard/clovergoshadow/helpers/ExpHelper.java
package com.clovercard.clovergoshadow.helpers;

import com.clovercard.clovergoshadow.config.Config;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class ExpHelper {

    private static final int MAX_LEVEL = 50;

    /** Cuánta EXP hace falta para subir del nivel dado */
    public static int getLevelExp(int level) {
        return (level * Config.CONFIG.getExpDifPerLevel()) + Config.CONFIG.getBaseExp();
    }

    /** Añade EXP y maneja subida de nivel, con mensaje centrado */
    public static void addExpToPlayer(ServerPlayerEntity player, int amount) {
        if (player == null) return;
        ServerScoreboard sb = ServerLifecycleHooks.getCurrentServer().getScoreboard();
        ScoreObjective lvlObj = sb.getObjective("clshadowlevels");
        ScoreObjective expObj = sb.getObjective("clshadowexp");

        if (lvlObj == null) {
            sb.addObjective("clshadowlevels", ScoreCriteria.DUMMY,
                new StringTextComponent("researchlevel"), ScoreCriteria.DUMMY.getDefaultRenderType());
            lvlObj = sb.getObjective("clshadowlevels");
        }
        if (expObj == null) {
            sb.addObjective("clshadowexp", ScoreCriteria.DUMMY,
                new StringTextComponent("researchexp"), ScoreCriteria.DUMMY.getDefaultRenderType());
            expObj = sb.getObjective("clshadowexp");
        }
        if (lvlObj == null || expObj == null) return;

        Score plLvl = sb.getOrCreatePlayerScore(player.getName().getString(), lvlObj);
        Score plExp = sb.getOrCreatePlayerScore(player.getName().getString(), expObj);

        // 1) Dar EXP
        plExp.setScore(plExp.getScore() + amount);
        IFormattableTextComponent gainMsg = Config.CONFIG.isUseTranslatables()
            ? new TranslationTextComponent("clovergoshadow.expgain", amount)
            : new StringTextComponent("§a¡Has ganado §e" + amount + " §aexp!");
        player.sendMessage(gainMsg, Util.NIL_UUID);

        // 2) Bucle de subida de nivel y recompensas
        int required = getLevelExp(plLvl.getScore());
        while (plExp.getScore() >= required) {
            plExp.setScore(plExp.getScore() - required);

            int nextLevel = plLvl.getScore() + 1;
            if (nextLevel > MAX_LEVEL) {
                plLvl.setScore(0);
            } else {
                plLvl.setScore(nextLevel);
            }

            // Recompensa cada 10 niveles
            if (nextLevel % 10 == 0) {
                ItemStack spawner = ItemHelper.makeLegendSpawner();
                if (spawner != null) {
                    player.inventory.add(spawner);
                }
            }

            required = getLevelExp(plLvl.getScore());

            // 3) Mensaje de nivel alcanzado, centrado y con colores solicitados
            String header = "§r\n"
                + "§b§lᴀꜱᴄᴇɴᴅᴇʀꜱᴍᴄ §7|§6§l ᴘɪxᴇʟᴍᴏɴ\n"
                + "§r";
            String body = ""
                + "§f¡ɴᴜᴇᴠᴏ ɴɪᴠᴇʟ ᴅᴇ ɪɴᴠᴇꜱᴛɪɢᴀᴅᴏʀ ᴀʟᴄᴀɴᴢᴀᴅᴏ!\n"
                + "§fɴɪᴠᴇʟ: §a" + nextLevel + "§7/§c" + MAX_LEVEL + "\n"
                + "§r";
            ChatHelper.sendCentered(player, header + body);
        }
    }

    /** Añade niveles directos y resetea EXP, con mensaje centrado */
    public static void addLevelToPlayer(ServerPlayerEntity player, int amount) {
        if (player == null) return;
        ServerScoreboard sb = ServerLifecycleHooks.getCurrentServer().getScoreboard();
        ScoreObjective lvlObj = sb.getObjective("clshadowlevels");
        ScoreObjective expObj = sb.getObjective("clshadowexp");
        if (lvlObj == null || expObj == null) return;

        Score plLvl = sb.getOrCreatePlayerScore(player.getName().getString(), lvlObj);
        plLvl.setScore(Math.min(plLvl.getScore() + amount, MAX_LEVEL));
        sb.getOrCreatePlayerScore(player.getName().getString(), expObj).setScore(0);

        int newLevel = plLvl.getScore();

        String header = "§r\n"
            + "§b§lᴀꜱᴄᴇɴᴅᴇʀꜱᴍᴄ §7|§6§l ᴘɪxᴇʟᴍᴏɴ\n"
            + "§r";
        String body = ""
            + "§cᴛᴜ ɴɪᴠᴇʟ ᴅᴇ ɪɴᴠᴇꜱᴛɪɢᴀᴅᴏʀ ʜᴀ ꜱɪᴅᴏ ꜰɪᴊᴀᴅᴏ ᴇɴ:\n"
            + "§fɴɪᴠᴇʟ: §a" + newLevel + "§7/§c" + MAX_LEVEL + "\n"
            + "§r";
        ChatHelper.sendCentered(player, header + body);
    }

    /** Fija la EXP al valor indicado (mensaje simple) */
    public static void setExpForPlayer(ServerPlayerEntity player, int amount) {
        if (player == null) return;
        ServerScoreboard sb = ServerLifecycleHooks.getCurrentServer().getScoreboard();
        ScoreObjective expObj = sb.getObjective("clshadowexp");
        if (expObj == null) return;

        sb.getOrCreatePlayerScore(player.getName().getString(), expObj).setScore(amount);
        IFormattableTextComponent msg = Config.CONFIG.isUseTranslatables()
            ? new TranslationTextComponent("clovergoshadow.expset", amount)
            : new StringTextComponent("§d¡Tu experiencia ha sido fijada en §e" + amount + "§d!");
        player.sendMessage(msg, Util.NIL_UUID);
    }

    /** Fija el nivel al valor indicado y resetea EXP, con mensaje centrado */
    public static void setLevelForPlayer(ServerPlayerEntity player, int amount) {
        if (player == null) return;
        ServerScoreboard sb = ServerLifecycleHooks.getCurrentServer().getScoreboard();
        ScoreObjective lvlObj = sb.getObjective("clshadowlevels");
        ScoreObjective expObj = sb.getObjective("clshadowexp");
        if (lvlObj == null || expObj == null) return;

        Score plLvl = sb.getOrCreatePlayerScore(player.getName().getString(), lvlObj);
        plLvl.setScore(Math.min(amount, MAX_LEVEL));
        sb.getOrCreatePlayerScore(player.getName().getString(), expObj).setScore(0);

        int newLevel = plLvl.getScore();

        String header = "§r\n"
            + "§b§lᴀꜱᴄᴇɴᴅᴇʀꜱᴍᴄ §7|§6§l ᴘɪxᴇʟᴍᴏɴ\n"
            + "§r";
        String body = ""
            + "§cᴛᴜ ɴɪᴠᴇʟ ᴅᴇ ɪɴᴠᴇꜱᴛɪɢᴀᴅᴏʀ ʜᴀ ꜱɪᴅᴏ ꜰɪᴊᴀᴅᴏ ᴇɴ:\n"
            + "§fɴɪᴠᴇʟ: §a" + newLevel + "§7/§c" + MAX_LEVEL + "\n"
            + "§r";
        ChatHelper.sendCentered(player, header + body);
    }
}
