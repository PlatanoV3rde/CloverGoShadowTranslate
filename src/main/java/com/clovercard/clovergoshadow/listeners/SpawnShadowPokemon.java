// src/main/java/com/clovercard/clovergoshadow/listeners/SpawnShadowPokemon.java
package com.clovercard.clovergoshadow.listeners;

import com.clovercard.clovergoshadow.config.Config;
import com.clovercard.clovergoshadow.enums.RibbonEnum;
import com.clovercard.clovergoshadow.helpers.RibbonHelper;
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.ribbon.type.RibbonType;
import com.pixelmonmod.pixelmon.api.pokemon.species.aggression.Aggression;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.pokemon.SpawnActionPokemon;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.npcs.trainers.SpawnActionNPCTrainer;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;

public class SpawnShadowPokemon {
    private static final Random RNG = new Random();
    private static final double SPAWN_CHANCE = Config.CONFIG.getShadowSpawnPercent() / 100.0;
    private static final double TRAINER_CHANCE = Config.CONFIG.getShadowTrainerPercent() / 100.0;
    private static final RibbonType SHADOW_RIBBON =
        RibbonHelper.getRibbonTypeIfExists(RibbonEnum.SHADOW_RIBBON.getRibbonId());

    @SubscribeEvent
    public void onSpawn(SpawnEvent event) {
        // — Wild Pokémon —
        if (event.action instanceof SpawnActionPokemon) {
            SpawnActionPokemon action = (SpawnActionPokemon) event.action;
            if (!(action.spawnLocation.cause instanceof ServerPlayerEntity)) return;
            if (SHADOW_RIBBON == null) return;
            if (RNG.nextDouble() >= SPAWN_CHANCE) return;

            ServerPlayerEntity player = (ServerPlayerEntity) action.spawnLocation.cause;
            Pokemon shadow = action.pokemon;
            // Blacklists
            if (Config.CONFIG.getShadowBlackList().contains(shadow.getSpecies().getName())
             || Config.CONFIG.getShadowFormBlackList().contains(shadow.getForm().getName())) {
                return;
            }

            shadow.getOrCreatePixelmon().setAggression(Aggression.AGGRESSIVE);
            shadow.addRibbon(SHADOW_RIBBON);

            IFormattableTextComponent msg;
            if (Config.CONFIG.isUseTranslatables()) {
                msg = new TranslationTextComponent("clovergoshadow.spawn", shadow.getTranslatedName());
            } else {
                msg = new StringTextComponent("¡Un Pokémon Oscuro ")
                    .withStyle(Style.EMPTY.withColor(TextFormatting.GREEN))
                    .append(shadow.getTranslatedName().copy().withStyle(TextFormatting.LIGHT_PURPLE))
                    .append(" ha spawneado cerca de ti!");
            }
            player.sendMessage(msg, Util.NIL_UUID);
        }

        // — Shadow Trainer —
        if (event.action instanceof SpawnActionNPCTrainer) {
            SpawnActionNPCTrainer action = (SpawnActionNPCTrainer) event.action;
            if (!(action.spawnLocation.cause instanceof ServerPlayerEntity)) return;
            if (RNG.nextDouble() >= TRAINER_CHANCE) return;

            ServerPlayerEntity player = (ServerPlayerEntity) action.spawnLocation.cause;
            NPCTrainer trainer = action.getOrCreateEntity();
            trainer.getPersistentData().putBoolean("isshadowtrainer", true);
            // Nicknames
            for (Pokemon p : trainer.getPokemonStorage().getTeam()) {
                p.setNickname(new StringTextComponent("Oscuro " + p.getTranslatedName().getString()));
            }

            IFormattableTextComponent msg;
            if (Config.CONFIG.isUseTranslatables()) {
                msg = new TranslationTextComponent("clovergoshadow.spawntrainer");
            } else {
                msg = new StringTextComponent(String.format(
                    "Un entrenador sospechoso apareció en %d, %d, %d!",
                    action.spawnLocation.location.pos.getX(),
                    action.spawnLocation.location.pos.getY(),
                    action.spawnLocation.location.pos.getZ()
                )).withStyle(Style.EMPTY.withColor(TextFormatting.RED));
            }
            player.sendMessage(msg, Util.NIL_UUID);
        }
    }
}
