// src/main/java/com/clovercard/clovergoshadow/listeners/ShadowLifetimeHandler.java
package com.clovercard.clovergoshadow.listeners;

import com.clovercard.clovergoshadow.enums.RibbonEnum;
import com.clovercard.clovergoshadow.helpers.RibbonHelper;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.api.pokemon.ribbon.type.RibbonType;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "clovergoshadow")
public class ShadowLifetimeHandler {
    private static final int    TICKS_PER_SECOND     = 20;
    private static final int    DEFAULT_LIFETIME_MIN = 5;
    private static final long   MAX_LIFETIME_TICKS   = DEFAULT_LIFETIME_MIN * 60L * TICKS_PER_SECOND;

    /** 
     * Guardamos directamente la entidad junto al tick global de spawn.
     * Esto evita buscar por UUID o iterar mundos completos.
     */
    private static final Map<PixelmonEntity, Long> tracker = new ConcurrentHashMap<>();

    /** 
     * Al unirse cualquier Pixelmon al mundo, si lleva el ribbon Shadow lo trackeamos.
     */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent evt) {
        if (!(evt.getEntity() instanceof PixelmonEntity)) {
            return;
        }
        PixelmonEntity pkm = (PixelmonEntity) evt.getEntity();

        RibbonType shadowRibbon = RibbonHelper.getRibbonTypeIfExists(RibbonEnum.SHADOW_RIBBON.getRibbonId());
        if (shadowRibbon == null) {
            return;
        }
        if (!pkm.getPokemon().hasRibbon(shadowRibbon)) {
            return;
        }

        // Tomamos el tick global del servidor
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        long now = server.getTickCount();
        tracker.put(pkm, now);
    }

    /**
     * Una vez por segundo (cada 20 ticks) limpiamos el tracker
     * y despawneamos los PixelmonShadow que superen su tiempo de vida.
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent evt) {
        if (evt.phase != TickEvent.Phase.END) {
            return;
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        long now = server.getTickCount();

        // Ejecutamos solo cada segundo
        if (now % TICKS_PER_SECOND != 0) {
            return;
        }

        Iterator<Map.Entry<PixelmonEntity, Long>> it = tracker.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<PixelmonEntity, Long> entry = it.next();
            PixelmonEntity pkm     = entry.getKey();
            long           spawnT  = entry.getValue();

            // Si ya murió o no existe, simplemente eliminamos el tracking
            if (!pkm.isAlive()) {
                it.remove();
                continue;
            }

            // Si superó los 5 minutos, lo despawneamos y limpiamos el tracking
            if (now - spawnT >= MAX_LIFETIME_TICKS) {
                pkm.remove();
                it.remove();
            }
        }
    }
}
