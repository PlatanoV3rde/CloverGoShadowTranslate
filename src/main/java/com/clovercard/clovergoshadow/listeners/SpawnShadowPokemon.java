package com.clovercard.clovergoshadow.listeners;

import com.clovercard.clovergoshadow.config.Config;
import com.clovercard.clovergoshadow.enums.RibbonEnum;
import com.clovercard.clovergoshadow.helpers.RibbonHelper;
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.ribbon.type.RibbonType;
import com.pixelmonmod.pixelmon.api.pokemon.species.aggression.Aggression;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.npcs.trainers.SpawnActionNPCTrainer;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.pokemon.SpawnActionPokemon;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

public class SpawnShadowPokemon {
    
    // Configuración de colores
    private static final TextFormatting COLOR_TITULO = TextFormatting.LIGHT_PURPLE;
    private static final TextFormatting COLOR_POKEMON = TextFormatting.YELLOW;
    private static final TextFormatting COLOR_COORDENADAS = TextFormatting.GREEN;
    private static final TextFormatting COLOR_ENTRENADOR = TextFormatting.RED;
    
    @SubscribeEvent
    public void onSpawn(SpawnEvent event) {
        if(event.action instanceof SpawnActionPokemon) {
            handlePokemonSpawn(event);
        }
        else if(event.action instanceof SpawnActionNPCTrainer) {
            handleTrainerSpawn(event);
        }
    }
    
    private void handlePokemonSpawn(SpawnEvent event) {
        SpawnActionPokemon action = (SpawnActionPokemon) event.action;
        
        if(Math.random() < Config.CONFIG.getShadowSpawnPercent()/100) {
            Pokemon shadow = action.pokemon;
            
            // Verificar listas negras
            if(Config.CONFIG.getShadowBlackList().contains(shadow.getSpecies().getName())) return;
            if(Config.CONFIG.getShadowFormBlackList().contains(shadow.getForm().getName())) return;
            
            // Configurar Pokémon oscuro
            shadow.getOrCreatePixelmon().setAggression(Aggression.AGGRESSIVE);
            RibbonType ribbon = RibbonHelper.getRibbonTypeIfExists(RibbonEnum.SHADOW_RIBBON.getRibbonId());
            if(ribbon == null) return;
            
            if(!(event.action.spawnLocation.cause instanceof ServerPlayerEntity)) return;
            ServerPlayerEntity player = (ServerPlayerEntity) event.action.spawnLocation.cause;
            shadow.addRibbon(ribbon);
            
            // Obtener coordenadas
            double x = event.action.spawnLocation.location.pos.getX();
            double y = event.action.spawnLocation.location.pos.getY();
            double z = event.action.spawnLocation.location.pos.getZ();
            
            // Crear mensaje en español
            IFormattableTextComponent msg = new StringTextComponent("")
                .append(new StringTextComponent("¡Un Pokémon oscuro ").withStyle(COLOR_TITULO))
                .append(new StringTextComponent(shadow.getTranslatedName().getString()).withStyle(COLOR_POKEMON))
                .append(new StringTextComponent(" ha aparecido! ").withStyle(COLOR_TITULO))
                .append(new StringTextComponent("(").withStyle(TextFormatting.WHITE))
                .append(new StringTextComponent("X: " + String.format("%.1f", x) + ", ").withStyle(COLOR_COORDENADAS))
                .append(new StringTextComponent("Y: " + String.format("%.1f", y) + ", ").withStyle(COLOR_COORDENADAS))
                .append(new StringTextComponent("Z: " + String.format("%.1f", z)).withStyle(COLOR_COORDENADAS))
                .append(new StringTextComponent(")").withStyle(TextFormatting.WHITE));
            
            player.sendMessage(msg, Util.NIL_UUID);
        }
    }
    
    private void handleTrainerSpawn(SpawnEvent event) {
        SpawnActionNPCTrainer action = (SpawnActionNPCTrainer) event.action;
        
        if(Math.random() < Config.CONFIG.getShadowTrainerPercent()/100) {
            NPCTrainer trainer = action.getOrCreateEntity();
            trainer.getPersistentData().putBoolean("isshadowtrainer", true);
            
            // Configurar equipo del entrenador
            ArrayList<Pokemon> team = (ArrayList<Pokemon>) trainer.getPokemonStorage().getTeam();
            team.forEach(pkm -> pkm.setNickname(new StringTextComponent("Shadow " + pkm.getTranslatedName().getString())));
            
            // Obtener coordenadas
            double x = event.action.spawnLocation.location.pos.getX();
            double y = event.action.spawnLocation.location.pos.getY();
            double z = event.action.spawnLocation.location.pos.getZ();
            
            // Crear mensaje en español
            IFormattableTextComponent msg = new StringTextComponent("")
                .append(new StringTextComponent("¡Un entrenador sospechoso ha aparecido en ").withStyle(COLOR_ENTRENADOR))
                .append(new StringTextComponent("X: " + String.format("%.1f", x) + ", ").withStyle(COLOR_COORDENADAS))
                .append(new StringTextComponent("Y: " + String.format("%.1f", y) + ", ").withStyle(COLOR_COORDENADAS))
                .append(new StringTextComponent("Z: " + String.format("%.1f", z)).withStyle(COLOR_COORDENADAS))
                .append(new StringTextComponent("!").withStyle(COLOR_ENTRENADOR));
            
            if(event.action.spawnLocation.cause instanceof ServerPlayerEntity) {
                ServerPlayerEntity player = (ServerPlayerEntity) event.action.spawnLocation.cause;
                player.sendMessage(msg, Util.NIL_UUID);
            }
        }
    }
}