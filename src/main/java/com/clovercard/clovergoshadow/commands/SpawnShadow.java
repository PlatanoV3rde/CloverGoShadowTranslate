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
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class SpawnShadow {
    public SpawnShadow(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            Commands.literal("clovergoshadow")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("spawnshadow")
                    .then(Commands.argument("input", StringArgumentType.greedyString())
                        .executes(ctx -> {
                            CommandSource src = ctx.getSource();
                            ServerPlayerEntity sender = (src.getEntity() instanceof ServerPlayerEntity)
                                ? (ServerPlayerEntity) src.getEntity()
                                : null;

                            String raw = StringArgumentType.getString(ctx, "input");
                            String[] parts = raw.trim().split("\\s+");

                            ServerPlayerEntity target = null;
                            double x = 0, y = 0, z = 0;
                            int idx = 0;
                            boolean coordsProvided = false;

                            // 1) ¿Primer token es jugador online?
                            if (parts.length > 0) {
                                target = ((sender != null)
                                    ? sender.getServer().getPlayerList()
                                    : src.getServer().getPlayerList())
                                    .getPlayerByName(parts[0]);
                            }
                            if (target != null) {
                                x = target.getX();
                                y = target.getY();
                                z = target.getZ();
                                idx = 1;
                            } else if (parts.length >= 4) {
                                // 2) ¿Primeros 3 tokens son doubles?
                                try {
                                    x = Double.parseDouble(parts[0]);
                                    y = Double.parseDouble(parts[1]);
                                    z = Double.parseDouble(parts[2]);
                                    idx = 3;
                                    coordsProvided = true;
                                } catch (NumberFormatException ignored) {
                                }
                            }

                            // 3) Desde consola sin jugador y sin coords => error
                            if (sender == null && target == null && !coordsProvided) {
                                sendError(src, "Debes indicar <jugador> o coordenadas válidas.");
                                return 1;
                            }

                            // 4) Construir specs
                            StringBuilder sb = new StringBuilder();
                            for (int i = idx; i < parts.length; i++) {
                                if (sb.length() > 0) sb.append(' ');
                                sb.append(parts[i]);
                            }
                            String specs = sb.toString().trim();
                            if (specs.isEmpty()) specs = "random";

                            return spawn(src, target, coordsProvided, x, y, z, specs);
                        })
                    )
                )
        );
    }

    private int spawn(CommandSource src,
                      ServerPlayerEntity target,
                      boolean coordsProvided,
                      double x, double y, double z,
                      String specs) throws CommandSyntaxException {
        // 1) Si no hay target y no son coords => error
        if (target == null && !coordsProvided) {
            sendError(src, "No hay jugador objetivo.");
            return 1;
        }

        // 2) Crear spec
        PokemonSpecification spec = PokemonSpecificationProxy.create(specs);
        if (spec == null) {
            sendError(src, "Especificación inválida: " + specs);
            return 1;
        }

        // 3) Crear Pokémon
        Pokemon pkm = PokemonFactory.create(spec);
        if (pkm == null) {
            sendError(src, "Pokémon no encontrado: " + specs);
            return 1;
        }

        // 4) Ribbon Shadow
        RibbonType ribbon = RibbonHelper.getRibbonTypeIfExists(RibbonEnum.SHADOW_RIBBON.getRibbonId());
        if (ribbon == null) {
            sendError(src, "Cinta de Pokémon Oscuro no encontrada.");
            return 1;
        }

        // 5) Aplicar ribbon
        pkm.addRibbon(ribbon);

        // 6) Determinar el mundo y spawnear en World
        World lvl = coordsProvided
            ? src.getLevel()
            : target.getCommandSenderWorld();
        pkm.getOrSpawnPixelmon(lvl, x, y + 1, z);

        // 7) MARCAR spawnTime en NBT
        PixelmonEntity spawned = (PixelmonEntity) pkm.getOrCreatePixelmon();
        if (lvl instanceof ServerWorld) {
            ServerWorld sw = (ServerWorld) lvl;
            long tick = sw.getGameTime();
            spawned.getPersistentData().putLong("clovergoshadow:spawnTime", tick);
        }

        // 8) Mensaje de éxito
        sendSuccess(src, pkm, x, y, z);
        return 0;
    }

    private void sendError(CommandSource src, String msg) {
        IFormattableTextComponent error = new StringTextComponent("✖ ")
            .withStyle(Style.EMPTY.withColor(TextFormatting.RED))
            .append(new StringTextComponent(msg)
                .withStyle(Style.EMPTY.withColor(TextFormatting.WHITE)));
        src.sendFailure(error);
    }

    private void sendSuccess(CommandSource src, Pokemon pkm, double x, double y, double z) {
        IFormattableTextComponent success = new StringTextComponent("✔ ")
            .withStyle(Style.EMPTY.withColor(TextFormatting.GREEN))
            .append(pkm.getTranslatedName().withStyle(Style.EMPTY.withColor(TextFormatting.YELLOW)))
            .append(new StringTextComponent(
                String.format(" ha aparecido en (%.2f, %.2f, %.2f)", x, y, z))
                .withStyle(Style.EMPTY.withColor(TextFormatting.AQUA))
            );
        src.sendSuccess(success, false);
    }
}
