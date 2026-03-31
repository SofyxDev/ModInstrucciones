package Luxfiro.instruccioens_eufonia;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Instruccioens_eufonia.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientForgeEvents {
    public static boolean pendingScreenOpen = false;
    public static int pendingScreenDelay = 0;

    public static void evaluarFade(boolean open) {
        Minecraft mc = Minecraft.getInstance();
        if (open) {
            if (mc.screen instanceof PantallaEvento) return;
            PantallaEvento.fadePhase = 1;
            PantallaEvento.fadeStartTime = System.currentTimeMillis();
            mc.setScreen(new PantallaEvento());
        } else {
            if (!(mc.screen instanceof PantallaEvento) || PantallaEvento.fadePhase == 3 || PantallaEvento.fadePhase == 4) return;
            PantallaEvento.fadePhase = 3;
            PantallaEvento.fadeStartTime = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();

        if (pendingScreenOpen && mc.level != null && mc.player != null) {
            if (pendingScreenDelay > 0) {
                pendingScreenDelay--;
            } else {
                pendingScreenOpen = false;
                if (!(mc.screen instanceof PantallaEvento)) {
                    evaluarFade(true);
                }
            }
        }

        long current = System.currentTimeMillis();

        if (mc.screen instanceof PantallaEvento && (PantallaEvento.fadePhase == 2 || PantallaEvento.fadePhase == 3)) {
            if (mc.gameRenderer.currentEffect() == null) {
                mc.gameRenderer.loadEffect(ResourceLocation.parse(Instruccioens_eufonia.MODID + ":shaders/post/camara.json"));
            }
        }

        if (!(mc.screen instanceof PantallaEvento) && PantallaEvento.fadePhase != 0 && PantallaEvento.fadePhase != 4) {
            PantallaEvento.fadePhase = 0;
            if (mc.gameRenderer.currentEffect() != null) {
                mc.gameRenderer.shutdownEffect();
            }
        }

        if (PantallaEvento.fadePhase == 1) {
            if (current - PantallaEvento.fadeStartTime >= PantallaEvento.FADE_DURATION) {
                PantallaEvento.fadePhase = 2;
                PantallaEvento.fadeStartTime = current;
            }
        } else if (PantallaEvento.fadePhase == 2) {
            if (current - PantallaEvento.fadeStartTime >= PantallaEvento.FADE_DURATION) {
                PantallaEvento.fadePhase = 0;
            }
        } else if (PantallaEvento.fadePhase == 3) {
            if (current - PantallaEvento.fadeStartTime >= PantallaEvento.FADE_DURATION) {
                PantallaEvento.fadePhase = 4;
                PantallaEvento.fadeStartTime = current;
                if (mc.gameRenderer.currentEffect() != null) {
                    mc.gameRenderer.shutdownEffect();
                }
            }
        } else if (PantallaEvento.fadePhase == 4) {
            if (current - PantallaEvento.fadeStartTime >= PantallaEvento.FADE_DURATION) {
                PantallaEvento.fadePhase = 0;
                mc.setScreen(null);
            }
        }

        if (Instruccioens_eufonia.isTimerActive && PantallaEvento.fadePhase == 0 && mc.screen instanceof PantallaEvento) {
            if (current >= Instruccioens_eufonia.clientTimerEndTime) {
                Instruccioens_eufonia.isTimerActive = false;
                evaluarFade(false);
            }
        }

        if (Instruccioens_eufonia.clientConfig.blockF1 && mc.screen instanceof PantallaEvento) {
            mc.options.hideGui = false;
            for (net.minecraft.client.KeyMapping key : mc.options.keyMappings) {
                if (key.getName().equals("key.toggleGui")) {
                    while (key.consumeClick()) {}
                }
            }
        }
    }
}