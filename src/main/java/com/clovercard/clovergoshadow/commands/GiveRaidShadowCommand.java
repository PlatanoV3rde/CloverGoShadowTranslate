package com.clovercard.clovergoshadow.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.command.PixelCommand;
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
    public GiveRaidShadowCommand(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("clovergoshadow")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("giveraidshadow")
                .then(Commands.argument("target", EntityArgument.player())
                    .executes(context -> { // Versión aleatoria (original)
                        ServerPlayerEntity target = EntityArgument.getPlayer(context, "target");
                        return giveRandomLegendaryShadowRaid(context.getSource(), target);
                    })
                    .then(Commands.argument("pokemon", StringArgumentType.string())
                        .executes(context -> { // Versión con Pokémon especificado
                            ServerPlayerEntity target = EntityArgument.getPlayer(context, "target");
                            String pokemonSpec = StringArgumentType.getString(context, "pokemon");
                            return giveCustomShadowRaid(context.getSource(), target, pokemonSpec);
                        })
                )
            )
        );
    }

    // Método para Pokémon aleatorio (original)
    private int giveRandomLegendaryShadowRaid(CommandSource source, ServerPlayerEntity target) {
        List<Species> allLegendaries = PixelmonSpecies.getAll()
            .stream()
            .filter(Species::isLegendary)
            .collect(Collectors.toList());

        if (allLegendaries.isEmpty()) {
            source.sendFailure(new StringTextComponent(TextFormatting.RED +
                "No se encontraron Pokémon legendarios registrados!"));
            return 0;
        }

        Species randomLegendary = allLegendaries.get(new Random().nextInt(allLegendaries.size()));
        return createAndGiveFlute(source, target, randomLegendary.getName(), "shadow");
    }

    // Método para Pokémon personalizado (nuevo)
    private int giveCustomShadowRaid(CommandSource source, ServerPlayerEntity target, String pokemonSpec) {
        Pokemon pokemon = PixelCommand.createPokemon(pokemonSpec);
        if (pokemon == null) {
            source.sendFailure(new StringTextComponent(TextFormatting.RED +
                "¡Pokémon no válido! Usa formato como: pikachu, charizard level=50"));
            return 0;
        }

        return createAndGiveFlute(source, target, pokemon.getSpecies().getName(), "shadow");
    }

    // Método común para crear y entregar la flauta
    private int createAndGiveFlute(CommandSource source, ServerPlayerEntity target, String speciesName, String formName) {
        ItemStack raidItem = createShadowRaidItem(speciesName, formName);

        if (!target.inventory.add(raidItem)) {
            target.drop(raidItem, false);
            target.sendMessage(new StringTextComponent(TextFormatting.YELLOW +
                "Tu inventario estaba lleno, el ítem fue soltado en el suelo."), target.getUUID());
        }

        // Mensaje al operador
        String successMessage = TextFormatting.GREEN + "¡Raid oscura entregada a " +
            TextFormatting.AQUA + target.getName().getString() +
            TextFormatting.GREEN + "! Pokémon: " +
            TextFormatting.RED + speciesName;

        source.sendSuccess(new StringTextComponent(successMessage), true);

        // Mensaje al jugador
        target.sendMessage(new StringTextComponent(
            TextFormatting.GREEN + "¡Has recibido una raid oscura de " +
            TextFormatting.RED + speciesName +
            TextFormatting.GREEN + "!"), target.getUUID());

        return 1;
    }

    // Método original para crear el ítem (sin cambios)
    private ItemStack createShadowRaidItem(String species, String form) {
        ItemStack flute = new ItemStack(PixelmonItems.poke_flute.getItem());
        CompoundNBT nbt = flute.getOrCreateTag();

        nbt.putBoolean("clovergoshadowwishingpiece", true);
        nbt.putString("clovergoshadowspecies", species);
        nbt.putString("clovergoshadowform", form);

        CompoundNBT displayTag = new CompoundNBT();
        displayTag.putString("Name", "{\"text\":\"Flauta de Raid Legendaria Oscura\",\"color\":\"red\",\"italic\":false}");

        ListNBT lore = new ListNBT();
        lore.add(StringNBT.valueOf("{\"text\":\"Usa para iniciar una raid legendaria oscura\",\"italic\":false,\"color\":\"gray\"}"));
        lore.add(StringNBT.valueOf("{\"text\":\"Pokémon: " + species + "\",\"italic\":false,\"color\":\"red\"}"));
        lore.add(StringNBT.valueOf("{\"text\":\"Forma: " + form + "\",\"italic\":false,\"color\":\"red\"}"));

        displayTag.put("Lore", lore);
        nbt.put("display", displayTag);

        return flute;
    }
}
