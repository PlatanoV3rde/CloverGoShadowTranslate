package com.clovercard.clovergoshadow.listeners;

import com.clovercard.clovergoshadow.config.Config;
import com.clovercard.clovergoshadow.enums.RibbonEnum;
import com.clovercard.clovergoshadow.helpers.RibbonHelper;
import com.pixelmonmod.api.registry.RegistryValue;
import com.pixelmonmod.pixelmon.api.events.PokemonSendOutEvent;
import com.pixelmonmod.pixelmon.api.pokemon.ribbon.Ribbon;
import com.pixelmonmod.pixelmon.api.pokemon.ribbon.type.RibbonType;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SendOutPokemon {
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onSendOut(PokemonSendOutEvent event) {
        if (Config.CONFIG.isUseTranslatables()) return;

        Ribbon ribbon = event.getPokemon().getDisplayedRibbon();

        if (ribbon == null) {
            // No ribbon, simplemente retornar
            return;
        }

        RibbonType shadow = RibbonHelper.getRibbonTypeIfExists(RibbonEnum.SHADOW_RIBBON.getRibbonId());
        RibbonType purified = RibbonHelper.getRibbonTypeIfExists(RibbonEnum.PURIFIED_RIBBON.getRibbonId());
        if (shadow == null || purified == null) return;

        RegistryValue<RibbonType> reg = ribbon.getType();
        if (!reg.getValue().isPresent()) return;

        RibbonType equipped = reg.getValueUnsafe();

        if (equipped.equals(shadow)) {
            ribbon.setDisplayName(new StringTextComponent("Oscuro").setStyle(Style.EMPTY.withColor(TextFormatting.DARK_PURPLE)));
            ribbon.getRibbonData().setSuffix(StringTextComponent.EMPTY);
        } else if (equipped.equals(purified)) {
            ribbon.setDisplayName(new StringTextComponent("Purificado").setStyle(Style.EMPTY.withColor(TextFormatting.AQUA)));
            ribbon.getRibbonData().setSuffix(StringTextComponent.EMPTY);
        } else {
            // Limpiar sufijo y nombre visible para otros ribbons
            ribbon.setDisplayName(StringTextComponent.EMPTY);
            ribbon.getRibbonData().setSuffix(StringTextComponent.EMPTY);
        }
    }
}
