package com.clovercard.clovergoshadow.helpers;

import com.clovercard.clovergoshadow.config.Config;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class ItemHelper {
    public static ItemStack makeSpawner() {
        ItemStack itemStack = new ItemStack(PixelmonItems.poke_flute);
        int attempts = 10;
        Species rand = PixelmonSpecies.getRandomSpecies(true, true, false);
        while (Config.CONFIG.getShadowBlackList().contains(rand.getName())) {
            if (attempts <= 0) return null;
            rand = PixelmonSpecies.getRandomSpecies(true, true, false);
            attempts--;
        }
        List<Stats> forms = rand.getForms(false);
        Stats form = forms.get((int) (Math.random() * forms.size()));
        attempts = 10;
        while (Config.CONFIG.getShadowFormBlackList().contains(form.getName()) || form.isTemporary()) {
            if (attempts <= 0) {
                form = rand.getDefaultForm();
                if (Config.CONFIG.getShadowFormBlackList().contains(form.getName())) return null;
                else break;
            } else {
                form = forms.get((int) (Math.random() * forms.size()));
                attempts--;
            }
        }
        String formName = form.getName();
        if (rand.getDefaultForm().is(form)) formName = "Default";
        IFormattableTextComponent itemName;
        if (Config.CONFIG.isUseTranslatables())
            itemName = new TranslationTextComponent("clovergoshadow.spawner", formName, rand.getName());
        else {
            itemName = new StringTextComponent("Oscuro " + formName + " " + rand.getName() + " Pieza Deseo");
        }
        itemStack.setHoverName(itemName);
        itemStack.getOrCreateTag().putBoolean("clovergoshadowwishingpiece", true);
        itemStack.getOrCreateTag().putString("clovergoshadowspecies", rand.getName());
        itemStack.getOrCreateTag().putString("clovergoshadowform", form.getName());

        // Lore visual
        CompoundNBT displayTag = new CompoundNBT();
        displayTag.putString("Name", "{\"text\":\"Flauta de Raid Oscura\",\"color\":\"dark_purple\",\"italic\":false}");

        ListNBT lore = new ListNBT();
        lore.add(StringNBT.valueOf("{\"text\":\"Usa para iniciar una raid oscura\",\"italic\":false,\"color\":\"gray\"}"));
        lore.add(StringNBT.valueOf("{\"text\":\"Pokémon: " + rand.getName() + "\",\"italic\":false,\"color\":\"red\"}"));
        lore.add(StringNBT.valueOf("{\"text\":\"Forma: " + form.getName() + "\",\"italic\":false,\"color\":\"dark_purple\"}"));

        displayTag.put("Lore", lore);
        itemStack.getOrCreateTag().put("display", displayTag);

        return itemStack;
    }

    public static ItemStack makeLegendSpawner() {
        ItemStack itemStack = new ItemStack(PixelmonItems.poke_flute);
        int attempts = 10;
        Species rand = PixelmonSpecies.getRandomLegendary();
        while (Config.CONFIG.getShadowBlackList().contains(rand.getName())) {
            if (attempts <= 0) return null;
            rand = PixelmonSpecies.getRandomLegendary();
            attempts--;
        }
        List<Stats> forms = rand.getForms(false);
        Stats form = forms.get((int) (Math.random() * forms.size()));
        attempts = 10;
        while (Config.CONFIG.getShadowFormBlackList().contains(form.getName()) || form.isTemporary()) {
            if (attempts <= 0) {
                form = rand.getDefaultForm();
                if (Config.CONFIG.getShadowFormBlackList().contains(form.getName())) return null;
                else break;
            } else {
                form = forms.get((int) (Math.random() * forms.size()));
                attempts--;
            }
        }
        String formName = form.getName();
        if (rand.getDefaultForm().is(form)) formName = "Default";
        IFormattableTextComponent itemName;
        if (Config.CONFIG.isUseTranslatables())
            itemName = new TranslationTextComponent("clovergoshadow.spawner", formName, rand.getName());
        else {
            itemName = new StringTextComponent("Oscuro " + formName + " " + rand.getName() + " Pieza Deseo");
        }
        itemStack.setHoverName(itemName);
        itemStack.getOrCreateTag().putBoolean("clovergoshadowwishingpiece", true);
        itemStack.getOrCreateTag().putString("clovergoshadowspecies", rand.getName());
        itemStack.getOrCreateTag().putString("clovergoshadowform", form.getName());

        // Lore visual
        CompoundNBT displayTag = new CompoundNBT();
        displayTag.putString("Name", "{\"text\":\"Flauta de Raid Legendaria Oscura\",\"color\":\"red\",\"italic\":false}");

        ListNBT lore = new ListNBT();
        lore.add(StringNBT.valueOf("{\"text\":\"Usa para iniciar una raid legendaria oscura\",\"italic\":false,\"color\":\"gray\"}"));
        lore.add(StringNBT.valueOf("{\"text\":\"Pokémon: " + rand.getName() + "\",\"italic\":false,\"color\":\"red\"}"));
        lore.add(StringNBT.valueOf("{\"text\":\"Forma: " + form.getName() + "\",\"italic\":false,\"color\":\"red\"}"));

        displayTag.put("Lore", lore);
        itemStack.getOrCreateTag().put("display", displayTag);

        return itemStack;
    }
}
