package com.clovercard.clovergoshadow.commands;

import com.clovercard.clovergoshadow.config.Config;
import com.clovercard.clovergoshadow.helpers.ExpHelper;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
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

public class ShowStats {
    public ShowStats(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("clovergoshadow")
                        .then(Commands.literal("stats").requires((src) -> CommandHelper.hasPermission(src, 0, "clovergoshadow.command.stats"))
                                .executes(cmd -> getPlayerResearch(cmd.getSource())))
        );
    }
    
    public int getPlayerResearch(CommandSource src) {
        if(!(src.getEntity() instanceof ServerPlayerEntity)) return 1;
        ServerPlayerEntity player = (ServerPlayerEntity) src.getEntity();
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
        if(levelObj == null || expObj == null) {
            return 1;
        }
        
        Score plLevel = scoreboard.getOrCreatePlayerScore(player.getName().getString(), levelObj);
        Score plExp = scoreboard.getOrCreatePlayerScore(player.getName().getString(), expObj);
        IFormattableTextComponent msg;
        
        if(Config.CONFIG.isUseTranslatables()) {
            msg = new TranslationTextComponent("clovergoshadow.displaystats", plLevel.getScore(), (ExpHelper.getLevelExp(plLevel.getScore()) - plExp.getScore()));
        }
        else {
            // Crear mensaje con colores
            IFormattableTextComponent baseMsg = new StringTextComponent("Tu nivel de investigación actual es ");
            
            // Nivel en color dorado
            IFormattableTextComponent levelPart = new StringTextComponent(String.valueOf(plLevel.getScore()));
            levelPart.setStyle(levelPart.getStyle().applyFormat(TextFormatting.GOLD));
            
            // Texto intermedio
            IFormattableTextComponent middlePart = new StringTextComponent("! Necesitas ");
            
            // Experiencia necesaria en color verde
            int expNeeded = ExpHelper.getLevelExp(plLevel.getScore()) - plExp.getScore();
            IFormattableTextComponent expPart = new StringTextComponent(String.valueOf(expNeeded));
            expPart.setStyle(expPart.getStyle().applyFormat(TextFormatting.GREEN));
            
            // Texto final
            IFormattableTextComponent endPart = new StringTextComponent(" puntos de experiencia para subir de nivel!");
            
            // Construir mensaje completo
            msg = baseMsg.append(levelPart).append(middlePart).append(expPart).append(endPart);
        }
        
        player.sendMessage(msg, Util.NIL_UUID);
        return 0;
    }
}
