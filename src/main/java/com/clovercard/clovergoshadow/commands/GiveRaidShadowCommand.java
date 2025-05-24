package com.clovercard.clovergoshadow.commands;

import com.mojang.brigadier.CommandDispatcher;
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
                    .executes(context -> {
                        ServerPlayerEntity target = EntityArgument.getPlayer(context, "target");
                        return giveRandomLegendaryShadowRaid(context.getSource(), target);
                    })
                )
            );
    }

    private int giveRandomLegendaryShadowRaid(CommandSource source, ServerPlayerEntity target) {
        List<RegistryValue<Species>> allLegendaries = PixelmonSpecies.getAll()
            .stream()
            .filter(reg -> reg.getValue()
                .map(species -> species.isLegendary() || species.isMythical())
                .orElse(false))
            .collect(Collectors.toList());

        if (allLegendaries.isEmpty()) {
            source.sendFailure(new StringTextComponent(TextFormatting.RED + 
                "No se encontraron Pokémon legendarios registrados!"));
            return 0;
        }

        RegistryValue<Species> randomLegendary = allLegendaries.get(
            new Random().nextInt(allLegendaries.size()));

        String speciesName = randomLegendary.getNameOrId();
        String formName = "shadow";

        ItemStack raidItem = createShadowRaidItem(speciesName, formName);

        if (!target.inventory.add(raidItem)) {
            target.drop(raidItem, false);
            target.sendMessage(
                new StringTextComponent(TextFormatting.YELLOW + 
                    "Tu inventario estaba lleno, el ítem fue soltado en el suelo."),
                target.getUUID()
            );
        }

        String successMessage = String.format("%s¡Raid oscura legendaria entregada a %s%s! Pokémon: %s%s",
            TextFormatting.GREEN,
            TextFormatting.AQUA,
            target.getName().getString(),
            TextFormatting.DARK_PURPLE,
            speciesName
        );

        source.sendSuccess(new StringTextComponent(successMessage), true);
        target.sendMessage(
            new StringTextComponent(TextFormatting.DARK_PURPLE + 
                "¡Has recibido una raid oscura legendaria de " + speciesName + "!"),
            target.getUUID()
        );

        return 1;
    }

    private ItemStack createShadowRaidItem(String species, String form) {
        ItemStack flute = new ItemStack(PixelmonItems.poke_flute.getItem());
        CompoundNBT nbt = flute.getOrCreateTag();
        
        nbt.putBoolean("clovergoshadowwishingpiece", true);
        nbt.putString("clovergoshadowspecies", species);
        nbt.putString("clovergoshadowform", form);
        
        CompoundNBT displayTag = new CompoundNBT();
        displayTag.putString("Name", "{\"text\":\"Flauta de Raid Legendaria Oscura\",\"color\":\"dark_purple\",\"italic\":false}");
        
        CompoundNBT loreTag = new CompoundNBT();
        loreTag.addTag(0, new StringTextComponent("Usa para iniciar una raid legendaria oscura").serializeNBT());
        loreTag.addTag(1, new StringTextComponent("Pokémon: " + species).serializeNBT());
        loreTag.addTag(2, new StringTextComponent("Forma: " + form).serializeNBT());
        displayTag.put("Lore", loreTag);
        
        nbt.put("display", displayTag);
        
        return flute;
    }
}
