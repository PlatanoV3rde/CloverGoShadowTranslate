// src/main/java/com/clovercard/clovergoshadow/commands/GiveRaidShadowCommand.java
package com.clovercard.clovergoshadow.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.Locale;

/**
 * Comando /clovergoshadow giveraidshadow <jugador> <pokemon>
 * Ej: /clovergoshadow giveraidshadow PlatanoV3rde Arceus
 */
public class GiveRaidShadowCommand {

    public GiveRaidShadowCommand(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
            Commands.literal("clovergoshadow")
                .requires(src -> src.hasPermission(2))
                .then(Commands.literal("giveraidshadow")
                    .then(Commands.argument("target", EntityArgument.player())
                    .then(Commands.argument("species", StringArgumentType.word())
                        .executes(ctx -> {
                            ServerPlayerEntity target =
                                EntityArgument.getPlayer(ctx, "target");
                            String rawSpecies =
                                StringArgumentType.getString(ctx, "species");
                            return giveRaidFlute(ctx.getSource(), target, rawSpecies);
                        })
                    ))
                )
        );
    }

    private int giveRaidFlute(CommandSource src,
                              ServerPlayerEntity target,
                              String rawSpecies) {
        // Normalizar a minúsculas
        String speciesKey = rawSpecies.toLowerCase(Locale.ROOT);

        // Creamos la Flauta de Raid Oscura
        ItemStack flute = new ItemStack(PixelmonItems.poke_flute.getItem());
        CompoundNBT tag = flute.getOrCreateTag();

        tag.putBoolean("clovergoshadowwishingpiece", true);
        tag.putString("clovergoshadowspecies", speciesKey);
        tag.putString("clovergoshadowform", "shadow");

        // Display name
        CompoundNBT display = new CompoundNBT();
        display.putString("Name",
            "{\"text\":\"Flauta de Raid Oscura\",\"color\":\"dark_purple\",\"italic\":false}"
        );

        // Lore
        ListNBT lore = new ListNBT();
        lore.add(StringNBT.valueOf(
            "{\"text\":\"Haz click derecho para iniciar una raid\",\"color\":\"gray\",\"italic\":false}"
        ));
        lore.add(StringNBT.valueOf(
            String.format(
                "{\"text\":\"Especie: %s\",\"color\":\"red\",\"italic\":false}",
                rawSpecies
            )
        ));
        display.put("Lore", lore);
        tag.put("display", display);

        // Entregar al jugador
        if (!target.inventory.add(flute)) {
            target.drop(flute, false);
            IFormattableTextComponent msg = new StringTextComponent("Inventario lleno, se ha soltado la Flauta en el suelo.")
                .withStyle(TextFormatting.YELLOW);
            target.sendMessage(msg, target.getUUID());
        }

        // Mensaje de confirmación al emisor
        IFormattableTextComponent confirm = new StringTextComponent("✔ ")
            .withStyle(TextFormatting.GREEN)
            .append(new StringTextComponent(
                "Se ha dado una Flauta de Raid Oscura de " + rawSpecies +
                " a " + target.getName().getString()
            ).withStyle(TextFormatting.AQUA));
        src.sendSuccess(confirm, false);

        return 1;
    }
}
