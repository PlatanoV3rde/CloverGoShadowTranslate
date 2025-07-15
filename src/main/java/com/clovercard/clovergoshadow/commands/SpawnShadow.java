// src/main/java/com/clovercard/clovergoshadow/commands/SpawnShadow.java
package com.clovercard.clovergoshadow.commands;

import com.clovercard.clovergoshadow.config.Config;
import com.clovercard.clovergoshadow.enums.RibbonEnum;
import com.clovercard.clovergoshadow.helpers.RibbonHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.ribbon.type.RibbonType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.*;

public class SpawnShadow {
    public SpawnShadow(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            Commands.literal("clovergoshadow")
                .then(Commands.literal("spawnshadow")
                    .requires(src -> CommandHelper.hasPermission(src, 2, "clovergoshadow.command.spawnshadow"))
                    .then(Commands.argument("input", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            CommandSource src = ctx.getSource();
                            ServerPlayerEntity sender = src.getPlayerOrException();
                            String raw = StringArgumentType.getString(ctx, "input");
                            String[] parts = raw.trim().split("\\s+");

                            ServerPlayerEntity targetPlayer = null;
                            double x, y, z;
                            StringBuilder specSb = new StringBuilder();

                            // 1) ¿Primer token es jugador?
                            if (parts.length > 0) {
                                targetPlayer = sender.getServer()
                                                     .getPlayerList()
                                                     .getPlayerByName(parts[0]);
                            }
                            if (targetPlayer != null) {
                                x = targetPlayer.getX();
                                y = targetPlayer.getY();
                                z = targetPlayer.getZ();
                                for (int i = 1; i < parts.length; i++) {
                                    specSb.append(parts[i]).append(' ');
                                }
                            } else {
                                // 2) ¿Primeros tres tokens son doubles?
                                if (parts.length >= 4) {
                                    try {
                                        x = Double.parseDouble(parts[0]);
                                        y = Double.parseDouble(parts[1]);
                                        z = Double.parseDouble(parts[2]);
                                        for (int i = 3; i < parts.length; i++) {
                                            specSb.append(parts[i]).append(' ');
                                        }
                                    } catch (NumberFormatException e) {
                                        // no eran coords válidas: fallback al sender
                                        x = sender.getX();
                                        y = sender.getY();
                                        z = sender.getZ();
                                        for (String t : parts) {
                                            specSb.append(t).append(' ');
                                        }
                                    }
                                } else {
                                    // 3) Menos de 4 tokens: fallback al sender
                                    x = sender.getX();
                                    y = sender.getY();
                                    z = sender.getZ();
                                    for (String t : parts) {
                                        specSb.append(t).append(' ');
                                    }
                                }
                                targetPlayer = sender;
                            }

                            String specs = specSb.toString().trim();
                            if (specs.isEmpty()) specs = "random";

                            return spawnShadowAt(targetPlayer, x, y, z, specs);
                        })
                    )
                )
        );
    }

    private int spawnShadowAt(ServerPlayerEntity player,
                              double x, double y, double z,
                              String specs) throws CommandSyntaxException {
        IFormattableTextComponent msg;

        // 1) Crear spec
        PokemonSpecification spec = PokemonSpecificationProxy.create(specs);
        if (spec == null) {
            msg = Config.CONFIG.isUseTranslatables()
                ? new TranslationTextComponent("clovergoshadow.spawnshadow.error1")
                : new StringTextComponent("No se pudieron crear las especificaciones del Pokémon.");
            player.sendMessage(msg.withStyle(Style.EMPTY.withColor(TextFormatting.RED)), Util.NIL_UUID);
            return 1;
        }

        // 2) Crear Pokémon
        Pokemon pokemon = PokemonFactory.create(spec);
        if (pokemon == null) {
            msg = Config.CONFIG.isUseTranslatables()
                ? new TranslationTextComponent("clovergoshadow.spawnshadow.error2")
                : new StringTextComponent("No se pudo encontrar el Pokémon especificado.");
            player.sendMessage(msg.withStyle(Style.EMPTY.withColor(TextFormatting.RED)), Util.NIL_UUID);
            return 1;
        }

        // 3) Ribbon shadow
        RibbonType ribbon = RibbonHelper.getRibbonTypeIfExists(RibbonEnum.SHADOW_RIBBON.getRibbonId());
        if (ribbon == null) {
            msg = Config.CONFIG.isUseTranslatables()
                ? new TranslationTextComponent("clovergoshadow.spawnshadow.error3")
                : new StringTextComponent("¡No se encontró la cinta de tipo Sombrío!");
            player.sendMessage(msg.withStyle(Style.EMPTY.withColor(TextFormatting.RED)), Util.NIL_UUID);
            return 1;
        }

        // 4) Añadir ribbon y spawnear en (x, y+1, z)
        pokemon.addRibbon(ribbon);
        pokemon.getOrSpawnPixelmon(
            player.getCommandSenderWorld(),
            x, y + 1, z
        );

        // 5) Mensaje de éxito
        msg = Config.CONFIG.isUseTranslatables()
            ? new TranslationTextComponent("clovergoshadow.spawnshadow.success")
            : new StringTextComponent(
                String.format("¡Has hecho aparecer un Pokémon Sombrío en (%.3f, %.3f, %.3f)!", x, y, z)
            );
        player.sendMessage(msg.withStyle(Style.EMPTY.withColor(TextFormatting.GREEN)), Util.NIL_UUID);

        return 0;
    }
}
