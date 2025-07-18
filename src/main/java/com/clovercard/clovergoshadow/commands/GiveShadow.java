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
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

public class GiveShadow {
    public GiveShadow(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("clovergoshadow")
                        .then(
                                Commands.literal("giveshadow").requires((src) -> CommandHelper.hasPermission(src, 2, "clovergoshadow.command.giveshadow"))
                                        .then(
                                                Commands.argument("player", StringArgumentType.string())
                                                        .then(
                                                                Commands.argument("specs", StringArgumentType.greedyString())
                                                                        .executes(cmd -> giveShadow(cmd.getSource(), StringArgumentType.getString(cmd, "player"), StringArgumentType.getString(cmd, "specs")))
                                                        )
                                        )
                        )
        );
    }

    public int giveShadow(CommandSource src, String target, String specs) {
        ServerPlayerEntity sender = null;
        if (src.getEntity() instanceof ServerPlayerEntity) {
            sender = (ServerPlayerEntity) src.getEntity();
        }

        ServerPlayerEntity receiver = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(target);

        if (receiver == null) {
            sendErrorMessage(src, "clovergoshadow.giveshadow.error1", "¡No se pudo encontrar al jugador especificado!");
            return 1;
        }

        PokemonSpecification spec = PokemonSpecificationProxy.create(specs);
        if (spec == null) {
            sendErrorMessage(src, "clovergoshadow.giveshadow.error2", "No se pudieron crear las especificaciones del Pokémon.");
            return 1;
        }

        Pokemon pokemon = PokemonFactory.create(spec);
        if (pokemon == null) {
            sendErrorMessage(src, "clovergoshadow.giveshadow.error3", "No se pudo encontrar el Pokémon especificado.");
            return 1;
        }

        RibbonType ribbon = RibbonHelper.getRibbonTypeIfExists(RibbonEnum.SHADOW_RIBBON.getRibbonId());
        if (ribbon == null) {
            sendErrorMessage(src, "clovergoshadow.giveshadow.error4", "¡No se encontró la cinta de tipo Sombrío! ¿Fue eliminada?");
            return 1;
        }

        pokemon.addRibbon(ribbon);
        StorageProxy.getParty(receiver).add(pokemon);

        sendSuccessMessages(src, receiver, pokemon);

        return 0;
    }

    private void sendErrorMessage(CommandSource src, String translationKey, String defaultMessage) {
        IFormattableTextComponent errorMsg = Config.CONFIG.isUseTranslatables()
                ? new TranslationTextComponent(translationKey)
                : new StringTextComponent(defaultMessage);
        errorMsg.setStyle(errorMsg.getStyle().applyFormat(TextFormatting.RED));
        src.sendFailure(errorMsg);
    }

    private void sendSuccessMessages(CommandSource src, ServerPlayerEntity receiver, Pokemon pokemon) {
        String pokemonNameStr = Config.CONFIG.isUseTranslatables()
                ? pokemon.getSpecies().getTranslatedName().getString()
                : pokemon.getSpecies().getName();

        IFormattableTextComponent pokemonNameComponent = new StringTextComponent(pokemonNameStr)
                .setStyle(Style.EMPTY.applyFormat(TextFormatting.YELLOW));

        IFormattableTextComponent receiverNameComponent = new StringTextComponent(receiver.getName().getString())
                .setStyle(Style.EMPTY.applyFormat(TextFormatting.AQUA));

        if (Config.CONFIG.isUseTranslatables()) {
            IFormattableTextComponent successMsgR = new TranslationTextComponent("clovergoshadow.giveshadow.successreceiver")
                    .append(pokemonNameComponent)
                    .setStyle(Style.EMPTY.applyFormat(TextFormatting.GREEN));

            IFormattableTextComponent successMsgG = new TranslationTextComponent("clovergoshadow.giveshadow.successgiver")
                    .append(receiverNameComponent)
                    .append(new StringTextComponent(" "))
                    .append(pokemonNameComponent)
                    .setStyle(Style.EMPTY.applyFormat(TextFormatting.GREEN));

            receiver.sendMessage(successMsgR, Util.NIL_UUID);
            src.sendSuccess(successMsgG, false);
        } else {
            IFormattableTextComponent successMsgR = new StringTextComponent("¡Has recibido un ")
                    .append(pokemonNameComponent)
                    .append(" Oscuro!")
                    .setStyle(Style.EMPTY.applyFormat(TextFormatting.GREEN));

            IFormattableTextComponent successMsgG = new StringTextComponent("¡Le enviaste a ")
                    .append(receiverNameComponent)
                    .append(" un ")
                    .append(pokemonNameComponent)
                    .append(" Oscuro!")
                    .setStyle(Style.EMPTY.applyFormat(TextFormatting.GREEN));

            receiver.sendMessage(successMsgR, Util.NIL_UUID);
            src.sendSuccess(successMsgG, false);
        }
    }
}