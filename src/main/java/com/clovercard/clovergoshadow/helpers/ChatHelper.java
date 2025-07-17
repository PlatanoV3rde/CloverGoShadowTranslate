// src/main/java/com/clovercard/clovergoshadow/helpers/ChatHelper.java
package com.clovercard.clovergoshadow.helpers;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class ChatHelper {

    // Ancho aproximado de chat en caracteres (ajusta si quieres otro tamaño)
    private static final int CHAT_WIDTH = 50;

    /**
     * Envía al jugador varias líneas, intentando centrar cada línea de `text`.
     * Cada línea puede incluir códigos de color ‘§x’.
     */
    public static void sendCentered(ServerPlayerEntity player, String text) {
        String[] lines = text.split("\n");
        for (String line : lines) {
            String stripped = stripColorCodes(line);
            int pad = Math.max((CHAT_WIDTH - stripped.length()) / 2, 0);
            String prefix = new String(new char[pad]).replace('\0', ' ');
            player.sendMessage(new StringTextComponent(prefix + line), player.getUUID());
        }
    }

    /** Quita los códigos “§x” para medir longitud real */
    private static String stripColorCodes(String s) {
        return s.replaceAll("§[0-9A-FK-ORa-fk-or]", "");
    }
}
