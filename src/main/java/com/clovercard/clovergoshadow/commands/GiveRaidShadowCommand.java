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
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.server.command.CommandSourceStack;

import java.util.Random;

public class GiveRaidShadowCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("clovergoshadow")
            .then(Commands.literal("giveraidshadow")
                .then(Commands.argument("jugador", EntityArgument.player())
                    .executes(context -> {
                        ServerPlayerEntity player = EntityArgument.getPlayer(context, "jugador");

                        // Crear flauta
                        ItemStack flute = new ItemStack(PixelmonItems.poke_flute.getItem());

                        // Asignar NBT para raid oscura
                        CompoundNBT nbt = flute.getOrCreateTag();
                        nbt.putBoolean("clovergoshadowwishingpiece", true);

                        // Especie aleatoria
                        String[] posiblesEspecies = new String[] {
                            "Umbreon", "Gengar", "Tyranitar", "Lucario", "Hydreigon"
                        };
                        String especie = posiblesEspecies[new Random().nextInt(posiblesEspecies.length)];
                        nbt.putString("clovergoshadowspecies", especie);
                        nbt.putString("clovergoshadowform", "shadow"); // o el nombre de la forma

                        // Dar el ítem al jugador
                        if (!player.inventory.add(flute)) {
                            player.drop(flute, false);
                        }

                        context.getSource().sendFeedback(new StringTextComponent(
                            "Flauta de raid oscura entregada a " + player.getName().getString() + " con especie: " + especie), true);
                        return 1;
                    })
                )
            )
        );
    }
}
