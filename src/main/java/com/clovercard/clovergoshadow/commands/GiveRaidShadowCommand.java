// src/main/java/com/clovercard/clovergoshadow/commands/GiveRaidShadowCommand.java
package com.clovercard.clovergoshadow.commands;

import com.mojang.brigadier.CommandDispatcher;
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
import net.minecraft.util.text.*;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GiveRaidShadowCommand {
    private static final Random RNG = new Random();

    public GiveRaidShadowCommand(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            Commands.literal("clovergoshadow")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("giveraidshadow")
                    .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayerEntity target = EntityArgument.getPlayer(ctx, "target");
                            return giveRandomLegendaryShadowRaid(ctx.getSource(), target);
                        })
                    )
                )
        );
    }

    private int giveRandomLegendaryShadowRaid(CommandSource source, ServerPlayerEntity target) {
        List<Species> legendaries = PixelmonSpecies.getAll().stream()
            .filter(Species::isLegendary)
            .collect(Collectors.toList());

        if (legendaries.isEmpty()) {
            source.sendFailure(createStyled("No se encontraron Pokémon legendarios registrados.", TextFormatting.RED));
            return 0;
        }

        Species chosen = legendaries.get(RNG.nextInt(legendaries.size()));
        String speciesName = chosen.getName();
        String formName = "shadow";

        ItemStack raidFlute = createShadowRaidItem(speciesName, formName);

        if (!target.inventory.add(raidFlute)) {
            target.drop(raidFlute, false);
            target.sendMessage(createStyled(
                "Inventario lleno. Se ha soltado la Flauta de Raid Legendaria Oscura.",
                TextFormatting.YELLOW
            ), target.getUUID());
        }

        // Mensaje al operador
        IFormattableTextComponent opMsg = TextComponentUtils.concat(
            createStyled("✔ ", TextFormatting.GREEN),
            new StringTextComponent("Entrega completa: "),
            createStyled(target.getName().getString(), TextFormatting.AQUA),
            new StringTextComponent(" recibe "),
            createStyled("Flauta de Raid Legendaria Oscura", TextFormatting.GOLD),
            new StringTextComponent(" de "),
            createStyled(speciesName, TextFormatting.RED),
            new StringTextComponent(".")
        );
        source.sendSuccess(opMsg, true);

        // Mensaje al jugador
        IFormattableTextComponent playerMsg = TextComponentUtils.concat(
            createStyled("🎉 ", TextFormatting.GOLD),
            new StringTextComponent("¡Hola "),
            createStyled(target.getName().getString(), TextFormatting.AQUA),
            new StringTextComponent("! Has recibido "),
            createStyled("Flauta de Raid Legendaria Oscura", TextFormatting.GOLD),
            new StringTextComponent(" de "),
            createStyled(speciesName, TextFormatting.RED),
            new StringTextComponent(". ¡Buena suerte!")
        );
        target.sendMessage(playerMsg, target.getUUID());

        return 1;
    }

    private ItemStack createShadowRaidItem(String species, String form) {
        ItemStack flute = new ItemStack(PixelmonItems.poke_flute.getItem());
        CompoundNBT nbt = flute.getOrCreateTag();

        nbt.putBoolean("clovergoshadowwishingpiece", true);
        nbt.putString("clovergoshadowspecies", species);
        nbt.putString("clovergoshadowform", form);

        CompoundNBT display = new CompoundNBT();
        // Nombre del item
        display.putString("Name", serializeComponent(
            new StringTextComponent("Flauta de Raid Legendaria Oscura")
                .withStyle(Style.EMPTY.withColor(TextFormatting.RED).withItalic(false))
        ));

        // Lore
        ListNBT lore = new ListNBT();
        lore.add(StringNBT.valueOf(serializeComponent(
            new StringTextComponent("Usa para iniciar una raid legendaria oscura")
                .withStyle(Style.EMPTY.withColor(TextFormatting.GRAY).withItalic(false))
        )));
        lore.add(StringNBT.valueOf(serializeComponent(
            new StringTextComponent("Pokémon: " + species)
                .withStyle(Style.EMPTY.withColor(TextFormatting.RED))
        )));
        lore.add(StringNBT.valueOf(serializeComponent(
            new StringTextComponent("Forma: " + form)
                .withStyle(Style.EMPTY.withColor(TextFormatting.RED))
        )));

        display.put("Lore", lore);
        nbt.put("display", display);

        return flute;
    }

    /** Serializa un componente de texto a JSON válido para NBT */
    private String serializeComponent(ITextComponent component) {
        return ITextComponent.Serializer.toJson(component);
    }

    /** Crea un componente de texto con color fijo */
    private IFormattableTextComponent createStyled(String text, TextFormatting color) {
        return new StringTextComponent(text).withStyle(Style.EMPTY.withColor(color));
    }

    /** Utilidad para concatenar varios componentes */
    private static class TextComponentUtils {
        static IFormattableTextComponent concat(IFormattableTextComponent... parts) {
            IFormattableTextComponent result = new StringTextComponent("");
            for (IFormattableTextComponent p : parts) {
                result.append(p);
            }
            return result;
        }
    }
}
