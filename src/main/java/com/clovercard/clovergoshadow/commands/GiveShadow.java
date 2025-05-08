// ... (previous imports remain the same)

public class GiveShadow {
    // ... (command registration remains the same)

    public int giveShadow(CommandSource src, String target, String specs) {
        // ... (previous validation checks remain the same until the success messages)

        if(Config.CONFIG.isUseTranslatables()) {
            // Create styled components for translatable version
            IFormattableTextComponent pokemonNameComponent = new StringTextComponent(pokemon.getSpecies().getTranslatedName())
                .setStyle(Style.EMPTY.applyFormat(TextFormatting.YELLOW));
            IFormattableTextComponent receiverNameComponent = new StringTextComponent(receiver.getName().getString())
                .setStyle(Style.EMPTY.applyFormat(TextFormatting.AQUA));
            
            successMsgR = new TranslationTextComponent("clovergoshadow.giveshadow.successreceiver")
                .append(pokemonNameComponent);
            successMsgG = new TranslationTextComponent("clovergoshadow.giveshadow.successgiver")
                .append(receiverNameComponent)
                .append(new StringTextComponent(" "))
                .append(pokemonNameComponent);
            
            successMsgR.setStyle(successMsgR.getStyle().applyFormat(TextFormatting.GREEN));
            successMsgG.setStyle(successMsgG.getStyle().applyFormat(TextFormatting.GREEN));
        }
        else {
            // Create styled components for hardcoded Spanish version
            IFormattableTextComponent pokemonNameComponent = new StringTextComponent(pokemon.getSpecies().getName())
                .setStyle(Style.EMPTY.applyFormat(TextFormatting.YELLOW));
            IFormattableTextComponent receiverNameComponent = new StringTextComponent(receiver.getName().getString())
                .setStyle(Style.EMPTY.applyFormat(TextFormatting.AQUA));
            
            // Receiver message: "¡Has recibido un [Pikachu] Sombrío!"
            successMsgR = new StringTextComponent("¡Has recibido un ")
                .append(pokemonNameComponent)
                .append(" Oscuro!");
            
            // Giver message: "¡Le enviaste a [Notch] un [Pikachu] Sombrío!"
            successMsgG = new StringTextComponent("¡Le enviaste a ")
                .append(receiverNameComponent)
                .append(" un ")
                .append(pokemonNameComponent)
                .append(" Oscuro!");
            
            successMsgR.setStyle(successMsgR.getStyle().applyFormat(TextFormatting.GREEN));
            successMsgG.setStyle(successMsgG.getStyle().applyFormat(TextFormatting.GREEN));
        }
        
        receiver.sendMessage(successMsgR, Util.NIL_UUID);
        player.sendMessage(successMsgG, Util.NIL_UUID);
        
        return 0;
    }
}
