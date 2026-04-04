package Luxfiro.instrucciones.client;

import Luxfiro.instrucciones.InstruccionesMod;
import Luxfiro.instrucciones.client.screen.PantallaEvento;
import Luxfiro.instrucciones.config.ConfigManager;
import Luxfiro.instrucciones.config.SaveData;
import Luxfiro.instrucciones.network.SyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

public class InstruccionesClient implements ClientModInitializer {

    public static SaveData clientConfig = new SaveData();
    public static long clientTimerEndTime = 0;
    public static boolean isTimerActive = false;

    public static boolean pendingScreenOpen = false;
    public static int pendingScreenDelay = 0;

    @Override
    public void onInitializeClient() {
        // Recibir paquete del servidor
        ClientPlayNetworking.registerGlobalReceiver(SyncPayload.TYPE, (payload, context) -> {
            context.client().execute(() -> {
                clientConfig = ConfigManager.fromJson(payload.jsonData());
                if (clientConfig != null) clientConfig.validate(); else clientConfig = new SaveData();

                if (payload.isDisplayCommand()) {
                    if (payload.durationLeftMs() > 0) {
                        clientTimerEndTime = System.currentTimeMillis() + payload.durationLeftMs();
                        isTimerActive = true;
                    } else {
                        clientTimerEndTime = 0;
                        isTimerActive = false;
                    }

                    Minecraft mc = Minecraft.getInstance();

                    if (payload.openScreen()) {
                        if (mc.level == null || mc.player == null) {
                            pendingScreenOpen = true;
                            pendingScreenDelay = 40;
                        } else {
                            if (!(mc.screen instanceof PantallaEvento)) evaluarFade(true);
                        }
                    } else {
                        pendingScreenOpen = false;
                        if (mc.screen instanceof PantallaEvento && PantallaEvento.fadePhase != 3 && PantallaEvento.fadePhase != 4) {
                            evaluarFade(false);
                        }
                    }
                }
            });
        });

        // Lógica de Ticks del Cliente (Reemplaza a TickEvent.ClientTickEvent)
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (pendingScreenOpen && mc.level != null && mc.player != null) {
                if (pendingScreenDelay > 0) {
                    pendingScreenDelay--;
                } else {
                    pendingScreenOpen = false;
                    if (!(mc.screen instanceof PantallaEvento)) evaluarFade(true);
                }
            }

            long current = System.currentTimeMillis();

            if (mc.screen instanceof PantallaEvento && (PantallaEvento.fadePhase == 2 || PantallaEvento.fadePhase == 3)) {
                if (mc.gameRenderer.currentEffect() == null) {
                    mc.gameRenderer.loadEffect(ResourceLocation.fromNamespaceAndPath(InstruccionesMod.MODID, "shaders/post/camara.json"));
                }
            }

            if (!(mc.screen instanceof PantallaEvento) && PantallaEvento.fadePhase != 0 && PantallaEvento.fadePhase != 4) {
                PantallaEvento.fadePhase = 0;
                if (mc.gameRenderer.currentEffect() != null) mc.gameRenderer.shutdownEffect();
            }

            // Transiciones de Fase
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
                    if (mc.gameRenderer.currentEffect() != null) mc.gameRenderer.shutdownEffect();
                }
            } else if (PantallaEvento.fadePhase == 4) {
                if (current - PantallaEvento.fadeStartTime >= PantallaEvento.FADE_DURATION) {
                    PantallaEvento.fadePhase = 0;
                    mc.setScreen(null);
                }
            }

            if (isTimerActive && PantallaEvento.fadePhase == 0 && mc.screen instanceof PantallaEvento) {
                if (current >= clientTimerEndTime) {
                    isTimerActive = false;
                    evaluarFade(false);
                }
            }

            // Bloqueo de F1
            if (clientConfig.blockF1 && mc.screen instanceof PantallaEvento) {
                mc.options.hideGui = false;
                for (KeyMapping key : mc.options.keyMappings) {
                    if (key.getName().equals("key.toggleGui")) {
                        while (key.consumeClick()) {}
                    }
                }
            }
        });
    }

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
}