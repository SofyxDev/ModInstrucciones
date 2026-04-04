package Luxfiro.instrucciones.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import Luxfiro.instrucciones.InstruccionesMod;

public record SyncPayload(boolean openScreen, long durationLeftMs, String jsonData, boolean isDisplayCommand) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncPayload> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(InstruccionesMod.MODID, "sync"));

    public static final StreamCodec<FriendlyByteBuf, SyncPayload> STREAM_CODEC = CustomPacketPayload.codec(SyncPayload::write, SyncPayload::new);

    public SyncPayload(FriendlyByteBuf buffer) {
        this(buffer.readBoolean(), buffer.readLong(), buffer.readUtf(262144), buffer.readBoolean());
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.openScreen);
        buffer.writeLong(this.durationLeftMs);
        buffer.writeUtf(this.jsonData, 262144);
        buffer.writeBoolean(this.isDisplayCommand);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}