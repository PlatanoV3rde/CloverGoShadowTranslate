package com.clovercard.clovergoshadow.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
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

public class GiveRaidShadowCommand {
    public GiveRaidShadowCommand(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("clovergoshadow")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("giveraidshadow")
                .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.argument("pokemon", StringArgumentType.word())
                        .executes(context -> {
                            ServerPlayerEntity target = EntityArgument.getPlayer(context, "target");
                            String speciesName = StringArgumentType.getString(context, "pokemon");
                            return giveSpecificShadowRaid(context.getSource(), target, speciesName, "shadow");
                        })
                        .then(Commands.argument("form", StringArgumentType.word())
                            .executes(context -> {
                                ServerPlayerEntity target = EntityArgument.getPlayer(context, "target");
                                String speciesName = StringArgumentType.getString(context, "pokemon");
                                String form = StringArgumentType.getString(context, "form");
                                return giveSpecificShadowRaid(context.getSource(), target, speciesName, form);
                            })
                        )
                    )
                )
            )
        );
    }

    private int giveSpecificShadowRaid(CommandSource source, ServerPlayerEntity target, String speciesName, String form) {
        Species species;
        try {
            species = PixelmonSpecies.fromName(speciesName.toLowerCase());
            if (species == null || !species.isPresent()) {
                source.sendFailure(new StringTextComponent(TextFormatting.RED + "¡Pokémon no válido: " + speciesName));
                return 0;
            }
        } catch (Exception e) {
            source.sendFailure(new StringTextComponent(TextFormatting.RED + "¡Error al encontrar el Pokémon: " + speciesName));
            return 0;
        }

        ItemStack raidItem = createShadowRaidItem(species.getName(), form);

        if (!target.inventory.add(raidItem)) {
            target.drop(raidItem, false);
            target.sendMessage(new StringTextComponent(TextFormatting.YELLOW +
                "Tu inventario estaba lleno, el ítem fue soltado en el suelo."), target.getUUID());
        }

        // Mensaje para el operador
        source.sendSuccess(new StringTextComponent(TextFormatting.GREEN +
            "¡Flauta entregada a " + TextFormatting.AQUA + target.getName().getString() +
            TextFormatting.GREEN + "! Pokémon: " +
            TextFormatting.RED + species.getName() +
            TextFormatting.GREEN + ", Forma: " +
            TextFormatting.RED + form), true);

        // Mensaje para el jugador
        target.sendMessage(new StringTextComponent(TextFormatting.GREEN +
            "¡Has recibido una flauta de raid legendaria oscura de " +
            TextFormatting.RED + species.getName() +
            TextFormatting.GREEN + "!"), target.getUUID());

        return 1;
    }

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
