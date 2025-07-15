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
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;               // <-- import añadido
import com.pixelmonmod.pixelmon.api.pokemon.ribbon.type.RibbonType;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.*;

public class SpawnShadow {
    public SpawnShadow(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            Commands.literal("clovergoshadow")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("spawnshadow")
                    .then(Commands.argument("input", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            CommandSource src = ctx.getSource();
                            ServerPlayerEntity sender = src.getPlayerOrException();
                            String raw = StringArgumentType.getString(ctx, "input");
                            String[] parts = raw.trim().split("\\s+");

                            ServerPlayerEntity target = null;
                            double x, y, z;
                            int idx = 0;

                            // 1) ¿Primer token es jugador online?
                            if (parts.length > 0) {
                                target = sender.getServer()
                                               .getPlayerList()
                                               .getPlayerByName(parts[0]);
                            }
                            if (target != null) {
                                x = target.getX();
                                y = target.getY();
                                z = target.getZ();
                                idx = 1;
                            } else {
                                // 2) ¿Primeros 3 tokens son doubles?
                                if (parts.length >= 4) {
                                    try {
                                        x = Double.parseDouble(parts[0]);
                                        y = Double.parseDouble(parts[1]);
                                        z = Double.parseDouble(parts[2]);
                                        idx = 3;
                                    } catch (NumberFormatException e) {
                                        x = sender.getX();
                                        y = sender.getY();
                                        z = sender.getZ();
                                    }
                                } else {
                                    x = sender.getX();
                                    y = sender.getY();
                                    z = sender.getZ();
                                }
                                target = sender;
                            }

                            // 4) Construir specs
                            StringBuilder sb = new StringBuilder();
                            for (int i = idx; i < parts.length; i++) {
                                if (sb.length() > 0) sb.append(' ');
                                sb.append(parts[i]);
                            }
                            String specs = sb.toString().trim();
                            if (specs.isEmpty()) specs = "random";

                            return spawn(src, target, x, y, z, specs);
                        })
                    )
                )
        );
    }

    private int spawn(CommandSource src,
                      ServerPlayerEntity target,
                      double x, double y, double z,
                      String specs) throws CommandSyntaxException {
        // 1) Crear spec
        PokemonSpecification spec = PokemonSpecificationProxy.create(specs);
        if (spec == null) {
            sendError(src, "clovergoshadow.spawnshadow.error1",
                      "No se pudo interpretar la especificación: " + specs);
            return 1;
        }

        // 2) Crear Pokémon
        Pokemon pkm = PokemonFactory.create(spec);
        if (pkm == null) {
            sendError(src, "clovergoshadow.spawnshadow.error2",
                      "Pokémon no encontrado: " + specs);
            return 1;
        }

        // 3) Ribbon Shadow
        RibbonType ribbon = RibbonHelper.getRibbonTypeIfExists(RibbonEnum.SHADOW_RIBBON.getRibbonId());
        if (ribbon == null) {
            sendError(src, "clovergoshadow.spawnshadow.error3",
                      "Cinta Sombrío no encontrada.");
            return 1;
        }

        // 4) Aplicar ribbon y spawnear
        pkm.addRibbon(ribbon);
        pkm.getOrSpawnPixelmon(
            target.getCommandSenderWorld(),
            x, y + 1, z
        );

        // 5) Mensaje de éxito
        sendSuccess(src, pkm, x, y, z);
        return 0;
    }

    private void sendError(CommandSource src, String key, String fallback) {
        IFormattableTextComponent msg = Config.CONFIG.isUseTranslatables()
            ? new TranslationTextComponent(key)
            : new StringTextComponent(fallback);
        src.sendFailure(msg);
    }

    private void sendSuccess(CommandSource src, Pokemon pkm, double x, double y, double z) {
        IFormattableTextComponent msg = new StringTextComponent("")
            .append(new StringTextComponent("✔ ").withStyle(Style.EMPTY.withColor(TextFormatting.GREEN)))
            .append(new StringTextComponent("Spawned "))
            // Aquí usamos directamente el componente traducido del Pokémon
            .append(pkm.getTranslatedName().withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)))
            .append(new StringTextComponent(
                String.format(" at (%.2f, %.2f, %.2f)", x, y, z))
                .withStyle(Style.EMPTY.withColor(TextFormatting.AQUA))
            );
        src.sendSuccess(msg, false);
    }
}
