package com.clovercard.clovergoshadow.commands;

import com.clovercard.clovergoshadow.enums.RibbonEnum;
import com.clovercard.clovergoshadow.helpers.RibbonHelper;
import com.clovercard.clovergoshadow.objects.ShadowPokemonStorage;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import com.pixelmonmod.pixelmon.command.PixelCommand;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

import java.util.Collection;
import java.util.List;

public class GiveRaidShadowCommand extends PixelCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("giveraidshadow")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("pokemon", StringArgumentType.string())
                        .executes(ctx -> giveShadowPokemon(ctx.getSource(), StringArgumentType.getString(ctx, "pokemon"), null))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(ctx -> giveShadowPokemon(ctx.getSource(), StringArgumentType.getString(ctx, "pokemon"), EntityArgument.getPlayers(ctx, "targets")))));
        dispatcher.register(builder);
    }

    private static int giveShadowPokemon(CommandSource source, String pokemonSpec, Collection<ServerPlayerEntity> targets) throws CommandSyntaxException {
        if (targets == null) {
            targets = List.of(source.getPlayerOrException());
        }

        for (ServerPlayerEntity player : targets) {
            PlayerPartyStorage storage = StorageProxy.getParty(player);
            Pokemon pokemon = PixelCommand.createPokemon(pokemonSpec);
            if (pokemon == null) {
                source.sendSuccess(new StringTextComponent("¡El Pokémon especificado no es válido!"), false);
                return 0;
            }

            // Convertir a Pokémon oscuro
            ShadowPokemonStorage shadowStorage = new ShadowPokemonStorage();
            shadowStorage.addShadowPokemon(pokemon.getUUID());
            RibbonHelper.addRibbon(pokemon, RibbonEnum.SHADOW.getRibbon());

            // Verificar espacio en el equipo
            if (storage.getTeam().size() >= 6) {
                ItemStack pokemonStack = SpriteItemHelper.getPhoto(pokemon);
                player.addItem(pokemonStack);
                source.sendSuccess(new StringTextComponent("¡Tu equipo está lleno! El Pokémon oscuro se ha enviado a tu inventario."), false);
            } else {
                storage.add(pokemon);
                source.sendSuccess(new StringTextComponent("¡Has recibido un Pokémon oscuro!"), false);
            }
        }
        return 1;
    }
}
