// src/main/java/com/clovercard/clovergoshadow/listeners/ShadowLifetimeHandler.java
package com.clovercard.clovergoshadow.listeners;

import com.clovercard.clovergoshadow.enums.RibbonEnum;
import com.clovercard.clovergoshadow.helpers.RibbonHelper;
import com.pixelmonmod.pixelmon.api.pokemon.ribbon.type.RibbonType;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = "clovergoshadow")
public class ShadowLifetimeHandler {
    private static final int TICKS_PER_SECOND = 20;
    private static final int DEFAULT_LIFETIME_MINUTES = 5;
    private static final long MAX_LIFETIME_TICKS =
        DEFAULT_LIFETIME_MINUTES * 60L * TICKS_PER_SECOND;

    private static final Map<UUID, Long> shadowPokemonTracker = new ConcurrentHashMap<>();

    /** Registra Pokémon sombra cuando aparecen */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinWorldEvent event) {
        if (!(event.getEntity() instanceof PixelmonEntity)) {
            return;
        }
        PixelmonEntity pixelmon = (PixelmonEntity) event.getEntity();

        RibbonType shadowRibbon = RibbonHelper.getRibbonTypeIfExists(RibbonEnum.SHADOW_RIBBON.getRibbonId());
        if (shadowRibbon == null) {
            return;
        }

        if (pixelmon.getPokemon().hasRibbon(shadowRibbon)) {
            shadowPokemonTracker.put(
                pixelmon.getUUID(),
                event.getWorld().getGameTime()
            );
        }
    }

    /** Verifica y elimina Pokémon sombra que han excedido su tiempo de vida */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        long currentTime = server.getAllLevels().iterator().next().getGameTime();

        Iterator<Map.Entry<UUID, Long>> iter = shadowPokemonTracker.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<UUID, Long> entry = iter.next();
            UUID uuid = entry.getKey();
            long spawnTick = entry.getValue();

            PixelmonEntity found = null;
            for (ServerWorld world : server.getAllLevels()) {
                if (world.getEntity(uuid) instanceof PixelmonEntity) {
                    found = (PixelmonEntity) world.getEntity(uuid);
                    break;
                }
            }

            if (found == null || !found.isAlive()) {
                iter.remove();
                continue;
            }

            if (currentTime - spawnTick >= MAX_LIFETIME_TICKS) {
                found.remove();
                iter.remove();
            }
        }
    }
}
