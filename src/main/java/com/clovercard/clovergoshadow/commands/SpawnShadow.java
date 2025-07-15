package com.clovercard.clovergoshadow.commands;

import com.clovercard.clovergoshadow.config.Config;
import com.clovercard.clovergoshadow.enums.RibbonEnum;
import com.clovercard.clovergoshadow.helpers.RibbonHelper;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
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
    // Configuración de colores
    private static final TextFormatting COLOR_TITULO = TextFormatting.LIGHT_PURPLE;
    private static final TextFormatting COLOR_EXITO = TextFormatting.GREEN;
    private static final TextFormatting COLOR_ERROR = TextFormatting.RED;
    private static final TextFormatting COLOR_POKEMON = TextFormatting.YELLOW;
    private static final TextFormatting COLOR_COORDENADAS = TextFormatting.AQUA;
    private static final TextFormatting COLOR_DESTACADO = TextFormatting.GOLD;

    public SpawnShadow(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            Commands.literal("clovergoshadow")
                .then(Commands.literal("spawnshadow")
                    .requires(src -> CommandHelper.hasPermission(src, 2, "clovergoshadow.command.spawnshadow"))
                    .then(Commands.argument("specs", StringArgumentType.greedyString())
                        .executes(cmd -> spawnShadowPokemon(cmd.getSource(), StringArgumentType.getString(cmd, "specs")))
                    )
                )
        );
    }

    public int spawnShadowPokemon(CommandSource src, String specs) {
        if(!(src.getEntity() instanceof ServerPlayerEntity)) return 1;
        
        ServerPlayerEntity player = (ServerPlayerEntity) src.getEntity();
        PokemonSpecification spec = PokemonSpecificationProxy.create(specs);
        
        // Mensaje de error: Especificación inválida
        if(spec == null) {
            sendErrorMessage(player, "No se pudo interpretar la especificación del Pokémon.");
            return 1;
        }
        
        Pokemon pokemon = PokemonFactory.create(spec);
        
        // Mensaje de error: Pokémon no encontrado
        if(pokemon == null) {
            sendErrorMessage(player, "No se encontró el Pokémon especificado.", specs);
            return 1;
        }
        
        RibbonType ribbon = RibbonHelper.getRibbonTypeIfExists(RibbonEnum.SHADOW_RIBBON.getRibbonId());
        
        // Mensaje de error: Cinta no encontrada
        if(ribbon == null) {
            sendCriticalError(player, "No se encontró la cinta de Pokémon oscuro.");
            return 1;
        }
        
        // Spawnear el Pokémon
        pokemon.addRibbon(ribbon);
        double x = player.getX();
        double y = player.getY() + 1;
        double z = player.getZ();
        pokemon.getOrSpawnPixelmon(player.getCommandSenderWorld(), x, y, z);
        
        // Mensaje de éxito con detalles
        sendSuccessMessage(player, pokemon, specs, x, y, z);
        return 0;
    }

    private void sendErrorMessage(ServerPlayerEntity player, String message) {
        IFormattableTextComponent errorMsg = new StringTextComponent("")
            .append(new StringTextComponent("✖ ").withStyle(COLOR_ERROR))
            .append(new StringTextComponent("Error: ").withStyle(COLOR_ERROR))
            .append(new StringTextComponent(message).withStyle(TextFormatting.WHITE));
        player.sendMessage(errorMsg, Util.NIL_UUID);
    }

    private void sendErrorMessage(ServerPlayerEntity player, String message, String specs) {
        IFormattableTextComponent errorMsg = new StringTextComponent("")
            .append(new StringTextComponent("✖ ").withStyle(COLOR_ERROR))
            .append(new StringTextComponent("Error: ").withStyle(COLOR_ERROR))
            .append(new StringTextComponent(message).withStyle(TextFormatting.WHITE))
            .append(new StringTextComponent("\nEspecificación usada: ").withStyle(COLOR_ERROR))
            .append(new StringTextComponent(specs).withStyle(COLOR_DESTACADO));
        player.sendMessage(errorMsg, Util.NIL_UUID);
    }

    private void sendCriticalError(ServerPlayerEntity player, String message) {
        IFormattableTextComponent errorMsg = new StringTextComponent("")
            .append(new StringTextComponent("✖ ").withStyle(COLOR_ERROR))
            .append(new StringTextComponent("Error crítico: ").withStyle(COLOR_ERROR))
            .append(new StringTextComponent(message).withStyle(TextFormatting.WHITE))
            .append(new StringTextComponent("\n¡Reporta este error a un administrador!").withStyle(COLOR_ERROR));
        player.sendMessage(errorMsg, Util.NIL_UUID);
    }

    private void sendSuccessMessage(ServerPlayerEntity player, Pokemon pokemon, String specs, double x, double y, double z) {
        IFormattableTextComponent successMsg = new StringTextComponent("")
            .append(new StringTextComponent("✔ ").withStyle(COLOR_EXITO))
            .append(new StringTextComponent("¡Pokémon oscuro generado con éxito!\n").withStyle(COLOR_EXITO))
            .append(new StringTextComponent("▸ Pokémon: ").withStyle(COLOR_TITULO))
            .append(new StringTextComponent(pokemon.getDisplayName()).withStyle(COLOR_POKEMON))
            .append(new StringTextComponent("\n▸ Coordenadas: ").withStyle(COLOR_TITULO))
            .append(new StringTextComponent(String.format("X: %.1f, Y: %.1f, Z: %.1f", x, y, z)).withStyle(COLOR_COORDENADAS))
            .append(new StringTextComponent("\n▸ Especificación usada: ").withStyle(COLOR_TITULO))
            .append(new StringTextComponent(specs).withStyle(COLOR_DESTACADO));
        
        player.sendMessage(successMsg, Util.NIL_UUID);
    }
}