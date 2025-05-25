package com.clovercard.clovergoshadow.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.api.pokemon.PokemonSpecification;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GiveRaidShadowCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            Commands.literal("clovergoshadow")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("giveraidshadow")
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(context -> { // Versión aleatoria
                            ServerPlayerEntity target = EntityArgument.getPlayer(context, "target");
                            return darRaidLegendariaAleatoria(context.getSource(), target);
                        })
                        .then(Commands.argument("pokemon", StringArgumentType.string())
                            .executes(context -> { // Versión personalizada
                                ServerPlayerEntity target = EntityArgument.getPlayer(context, "target");
                                String pokemonSpec = StringArgumentType.getString(context, "pokemon");
                                return darRaidPersonalizada(context.getSource(), target, pokemonSpec);
                            })
                        )
                    )
                )
        );
    }

    private static int darRaidLegendariaAleatoria(CommandSource source, ServerPlayerEntity target) {
        List<Species> legendarios = PixelmonSpecies.getAll()
            .stream()
            .filter(Species::isLegendary)
            .collect(Collectors.toList());

        if (legendarios.isEmpty()) {
            source.sendFailure(new StringTextComponent(TextFormatting.RED +
                "¡No se encontraron Pokémon legendarios!"));
            return 0;
        }

        Species legendarioAleatorio = legendarios.get(new Random().nextInt(legendarios.size()));
        return crearYDarFlauta(source, target, legendarioAleatorio.getName(), "shadow");
    }

    private static int darRaidPersonalizada(CommandSource source, ServerPlayerEntity target, String pokemonSpec) {
        PokemonSpecification spec = PokemonSpecification.of(pokemonSpec);
        Pokemon pokemon = PokemonFactory.create(spec);

        if (pokemon == null) {
            source.sendFailure(new StringTextComponent(TextFormatting.RED +
                "¡Pokémon no válido! Usa formato como: pikachu, charizard level=50"));
            return 0;
        }

        return crearYDarFlauta(source, target, pokemon.getSpecies().getName(), "shadow");
    }

    private static int crearYDarFlauta(CommandSource source, ServerPlayerEntity target, String nombrePokemon, String forma) {
        ItemStack flauta = crearFlautaRaid(nombrePokemon, forma);

        if (!target.inventory.add(flauta)) {
            target.drop(flauta, false);
            target.sendMessage(new StringTextComponent(TextFormatting.YELLOW +
                "¡Tu inventario estaba lleno! El ítem se soltó en el suelo."), target.getUUID());
        }

        String mensajeExito = TextFormatting.GREEN + "¡Raid oscura entregada a " +
            TextFormatting.AQUA + target.getName().getString() +
            TextFormatting.GREEN + "! Pokémon: " +
            TextFormatting.RED + nombrePokemon;

        source.sendSuccess(new StringTextComponent(mensajeExito), true);

        target.sendMessage(new StringTextComponent(
            TextFormatting.GREEN + "¡Has recibido una raid oscura de " +
            TextFormatting.RED + nombrePokemon +
            TextFormatting.GREEN + "!"), target.getUUID());

        return 1;
    }

    private static ItemStack crearFlautaRaid(String pokemon, String forma) {
        ItemStack flauta = new ItemStack(PixelmonItems.poke_flute.getItem());
        CompoundNBT nbt = flauta.getOrCreateTag();

        nbt.putBoolean("clovergoshadowwishingpiece", true);
        nbt.putString("clovergoshadowspecies", pokemon);
        nbt.putString("clovergoshadowform", forma);

        CompoundNBT displayTag = new CompoundNBT();
        displayTag.putString("Name", "{\"text\":\"Flauta de Raid Legendaria Oscura\",\"color\":\"red\",\"italic\":false}");

        ListNBT lore = new ListNBT();
        lore.add(StringNBT.valueOf("{\"text\":\"Usa para iniciar una raid legendaria oscura\",\"italic\":false,\"color\":\"gray\"}"));
        lore.add(StringNBT.valueOf("{\"text\":\"Pokémon: " + pokemon + "\",\"italic\":false,\"color\":\"red\"}"));
        lore.add(StringNBT.valueOf("{\"text\":\"Forma: " + forma + "\",\"italic\":false,\"color\":\"red\"}"));

        displayTag.put("Lore", lore);
        nbt.put("display", displayTag);

        return flauta;
    }
}
