package Luxfiro.instruccioens_eufonia;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SyncPacket(boolean openScreen, long durationLeftMs, String jsonData, boolean isDisplayCommand) implements CustomPacketPayload {

    public static final Type<SyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(Instruccioens_eufonia.MODID, "sync"));

    public static final StreamCodec<FriendlyByteBuf, SyncPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SyncPacket::openScreen,
            ByteBufCodecs.VAR_LONG, SyncPacket::durationLeftMs,
            ByteBufCodecs.stringUtf8(262144), SyncPacket::jsonData,
            ByteBufCodecs.BOOL, SyncPacket::isDisplayCommand,
            SyncPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handleClient() {
        Instruccioens_eufonia.clientConfig = Instruccioens_eufonia.GSON.fromJson(this.jsonData, SaveData.class);
        if (Instruccioens_eufonia.clientConfig != null) Instruccioens_eufonia.clientConfig.validate();
        else Instruccioens_eufonia.clientConfig = new SaveData();

        if (this.isDisplayCommand) {
            if (this.durationLeftMs > 0) {
                Instruccioens_eufonia.clientTimerEndTime = System.currentTimeMillis() + this.durationLeftMs;
                Instruccioens_eufonia.isTimerActive = true;
            } else {
                Instruccioens_eufonia.clientTimerEndTime = 0;
                Instruccioens_eufonia.isTimerActive = false;
            }

            Minecraft mc = Minecraft.getInstance();

            if (this.openScreen) {
                if (mc.level == null || mc.player == null) {
                    ClientForgeEvents.pendingScreenOpen = true;
                    ClientForgeEvents.pendingScreenDelay = 40;
                } else {
                    if (!(mc.screen instanceof PantallaEvento)) {
                        ClientForgeEvents.evaluarFade(true);
                    }
                }
            } else {
                ClientForgeEvents.pendingScreenOpen = false;
                if (mc.screen instanceof PantallaEvento && PantallaEvento.fadePhase != 3 && PantallaEvento.fadePhase != 4) {
                    ClientForgeEvents.evaluarFade(false);
                }
            }
        }
    }
}