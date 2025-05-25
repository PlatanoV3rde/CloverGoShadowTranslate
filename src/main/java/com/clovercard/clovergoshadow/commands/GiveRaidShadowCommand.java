package com.clovercard.clovergoshadow.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pixelmonmod.api.registry.RegistryValue;
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

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GiveRaidShadowCommand {

    public GiveRaidShadowCommand(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("clovergoshadow")
            .requires(src -> src.hasPermissionLevel(2))
            .then(Commands.literal("giveraidshadow")
                .then(Commands.argument("target", EntityArgument.player())
                    // Sin especificar species → aleatorio
                    .executes(ctx -> {
                        ServerPlayerEntity target = EntityArgument.getPlayer(ctx, "target");
                        return giveRandomLegendaryShadowRaid(ctx.getSource(), target);
                    })
                    // Con species explícito
                    .then(Commands.argument("species", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgument.getPlayer(ctx, "target");
                            String speciesName = StringArgumentType.getString(ctx, "species");
                            return giveSpecificShadowRaid(ctx.getSource(), target, speciesName);
                        })
                    )
                )
            )
        );
    }

    private int giveRandomLegendaryShadowRaid(CommandSource src, ServerPlayerEntity target) {
        // PixelmonSpecies.getAll() devuelve RegistryValue<Species>
        List<RegistryValue<Species>> allLegendariesRV = PixelmonSpecies.getAll()
            .stream()
            .filter(rv -> rv.get().isLegendary())
            .collect(Collectors.toList());

        if (allLegendariesRV.isEmpty()) {
            src.sendFailure(new StringTextComponent(TextFormatting.RED +
                "No se encontraron Pokémon legendarios registrados!"));
            return 0;
        }

        // Elegir uno al azar
        RegistryValue<Species> rv = allLegendariesRV.get(new Random().nextInt(allLegendariesRV.size()));
        Species species = rv.get();
        return giveRaidItem(src, target, species.getName(), "shadow");
    }

    private int giveSpecificShadowRaid(CommandSource src, ServerPlayerEntity target, String speciesNameLower) {
        RegistryValue<Species> rv = PixelmonSpecies.fromName(speciesNameLower.toLowerCase());
        if (rv == null || !rv.isRegistered()) {
            src.sendFailure(new StringTextComponent(TextFormatting.RED +
                "¡Pokémon no válido: " + speciesNameLower));
            return 0;
        }

        Species species = rv.get();
        return giveRaidItem(src, target, species.getName(), "shadow");
    }

    private int giveRaidItem(CommandSource src, ServerPlayerEntity target,
                             String speciesName, String form) {
        ItemStack flute = createShadowRaidItem(speciesName, form);

        if (!target.inventory.add(flute)) {
            target.drop(flute, false);
            target.sendMessage(new StringTextComponent(TextFormatting.YELLOW +
                "¡Tu inventario estaba lleno! El ítem se soltó en el suelo."), target.getUUID());
        }

        // Mensaje al operador
        src.sendSuccess(new StringTextComponent(TextFormatting.GREEN +
            "¡Flauta de raid oscura entregada a " +
            TextFormatting.AQUA + target.getName().getString() +
            TextFormatting.GREEN + "! Pokémon: " +
            TextFormatting.RED + speciesName), true);

        // Mensaje al receptor
        target.sendMessage(new StringTextComponent(TextFormatting.GREEN +
            "¡Has recibido una flauta de raid oscura de " +
            TextFormatting.RED + speciesName +
            TextFormatting.GREEN + "!"), target.getUUID());

        return 1;
    }

    private ItemStack createShadowRaidItem(String species, String form) {
        ItemStack flute = new ItemStack(PixelmonItems.poke_flute.getItem());
        CompoundNBT nbt = flute.getOrCreateTag();

        // Etiquetas personalizadas
        nbt.putBoolean("clovergoshadowwishingpiece", true);
        nbt.putString("clovergoshadowspecies", species);
        nbt.putString("clovergoshadowform", form);

        // Nombre y lore
        CompoundNBT display = new CompoundNBT();
        display.putString("Name",
            "{\"text\":\"Flauta de Raid Legendaria Oscura\",\"color\":\"red\",\"italic\":false}");

        ListNBT lore = new ListNBT();
        lore.add(StringNBT.valueOf("{\"text\":\"Usa para iniciar una raid legendaria oscura\",\"color\":\"gray\",\"italic\":false}"));
        lore.add(StringNBT.valueOf("{\"text\":\"Pokémon: " + species + "\",\"color\":\"red\",\"italic\":false}"));
        lore.add(StringNBT.valueOf("{\"text\":\"Forma: " + form + "\",\"color\":\"red\",\"italic\":false}"));

        display.put("Lore", lore);
        nbt.put("display", display);

        return flute;
    }
}
