// src/main/java/com/clovercard/clovergoshadow/listeners/SpawnShadowPokemon.java
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
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

public class SpawnShadowPokemon {
    // Colores para el mensaje
    private static final TextFormatting COLOR_TITULO      = TextFormatting.LIGHT_PURPLE;
    private static final TextFormatting COLOR_POKEMON     = TextFormatting.YELLOW;
    private static final TextFormatting COLOR_COORDENADAS = TextFormatting.GREEN;
    private static final TextFormatting COLOR_ENTRENADOR  = TextFormatting.RED;

    @SubscribeEvent
    public void onSpawn(SpawnEvent event) {
        if (event.action instanceof SpawnActionPokemon) {
            handlePokemonSpawn(event);
        } else if (event.action instanceof SpawnActionNPCTrainer) {
            handleTrainerSpawn(event);
        }
    }

    private void handlePokemonSpawn(SpawnEvent event) {
        SpawnActionPokemon action = (SpawnActionPokemon) event.action;

        // Probabilidad según config
        if (Math.random() < Config.CONFIG.getShadowSpawnPercent() / 100.0) {
            Pokemon shadow = action.pokemon;

            // Listas negras
            if (Config.CONFIG.getShadowBlackList().contains(shadow.getSpecies().getName())) return;
            if (Config.CONFIG.getShadowFormBlackList().contains(shadow.getForm().getName())) return;

            // Lo hacemos agresivo y aplicamos el ribbon Shadow
            shadow.getOrCreatePixelmon().setAggression(Aggression.AGGRESSIVE);
            RibbonType ribbon = RibbonHelper.getRibbonTypeIfExists(RibbonEnum.SHADOW_RIBBON.getRibbonId());
            if (ribbon == null) return;

            // Sólo lo enviamos a un jugador
            if (!(action.spawnLocation.cause instanceof ServerPlayerEntity)) return;
            ServerPlayerEntity player = (ServerPlayerEntity) action.spawnLocation.cause;

            // 1) aplicamos el ribbon
            shadow.addRibbon(ribbon);

            // 2) marcamos el tiempo de spawn en NBT para el ShadowLifetimeHandler
            PixelmonEntity entity = shadow.getOrCreatePixelmon();
            if (entity.level instanceof ServerWorld) {
                long tick = ((ServerWorld) entity.level).getGameTime();
                entity.getPersistentData().putLong("clovergoshadow:spawnTime", tick);
            }

            // Preparamos el mensaje
            double x = action.spawnLocation.location.pos.getX();
            double y = action.spawnLocation.location.pos.getY();
            double z = action.spawnLocation.location.pos.getZ();

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

        if (Math.random() < Config.CONFIG.getShadowTrainerPercent() / 100.0) {
            NPCTrainer trainer = action.getOrCreateEntity();
            trainer.getPersistentData().putBoolean("isshadowtrainer", true);

            // Renombra equipo
            @SuppressWarnings("unchecked")
            ArrayList<Pokemon> team = (ArrayList<Pokemon>) trainer.getPokemonStorage().getTeam();
            team.forEach(pkm ->
                pkm.setNickname(new StringTextComponent("Oscuro " + pkm.getTranslatedName().getString()))
            );

            double x = action.spawnLocation.location.pos.getX();
            double y = action.spawnLocation.location.pos.getY();
            double z = action.spawnLocation.location.pos.getZ();

            IFormattableTextComponent msg = new StringTextComponent("")
                .append(new StringTextComponent("¡Un entrenador sospechoso ha aparecido en ").withStyle(COLOR_ENTRENADOR))
                .append(new StringTextComponent("X: " + String.format("%.1f", x) + ", ").withStyle(COLOR_COORDENADAS))
                .append(new StringTextComponent("Y: " + String.format("%.1f", y) + ", ").withStyle(COLOR_COORDENADAS))
                .append(new StringTextComponent("Z: " + String.format("%.1f", z)).withStyle(COLOR_COORDENADAS))
                .append(new StringTextComponent("!").withStyle(COLOR_ENTRENADOR));

            if (action.spawnLocation.cause instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity) action.spawnLocation.cause).sendMessage(msg, Util.NIL_UUID);
            }
        }
    }
}
