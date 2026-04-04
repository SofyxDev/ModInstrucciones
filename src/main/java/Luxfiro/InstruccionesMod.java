package Luxfiro.instrucciones;

import Luxfiro.instrucciones.commands.CommandLuxep;
import Luxfiro.instrucciones.config.ConfigManager;
import Luxfiro.instrucciones.config.SaveData;
import Luxfiro.instrucciones.network.SyncPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InstruccionesMod implements ModInitializer {
    public static final String MODID = "instrucciones_eufonia";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    // Estados Globales
    public static SaveData serverConfig = new SaveData();
    public static boolean isGlobalActive = false;
    public static long serverTimerEndTime = 0;
    public static MinecraftServer currentServer;

    public static final Block EXAMPLE_BLOCK = new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
    public static final Item EXAMPLE_BLOCK_ITEM = new BlockItem(EXAMPLE_BLOCK, new Item.Properties());
    public static final Item EXAMPLE_ITEM = new Item(new Item.Properties().food(new FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build()));

    @Override
    public void onInitialize() {
        ConfigManager.init();

        // Registro de Bloques e Ítems
        Registry.register(BuiltInRegistries.BLOCK, ResourceLocation.fromNamespaceAndPath(MODID, "example_block"), EXAMPLE_BLOCK);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "example_block"), EXAMPLE_BLOCK_ITEM);
        Registry.register(BuiltInRegistries.ITEM, ResourceLocation.fromNamespaceAndPath(MODID, "example_item"), EXAMPLE_ITEM);

        // Registro de Payload (Red 1.21.1)
        PayloadTypeRegistry.playS2C().register(SyncPayload.TYPE, SyncPayload.STREAM_CODEC);

        // Eventos del Servidor
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            currentServer = server;
            handlePlayerSync(handler.player);
        });

        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> {
            handlePlayerSync(player);
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            handlePlayerSync(newPlayer);
        });

        // Registro de Comandos
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CommandLuxep.register(dispatcher);
        });
    }

    private void handlePlayerSync(ServerPlayer player) {
        long timeLeft = serverTimerEndTime > 0 ? (serverTimerEndTime - System.currentTimeMillis()) : 0;
        if (isGlobalActive && serverTimerEndTime > 0 && timeLeft <= 0) {
            isGlobalActive = false;
        }
        SyncPayload payload = new SyncPayload(isGlobalActive, timeLeft, ConfigManager.toJson(serverConfig), true);
        ServerPlayNetworking.send(player, payload);
    }

    public static void sincronizarDatosSilencioso() {
        if (currentServer == null) return;
        SyncPayload payload = new SyncPayload(isGlobalActive, 0, ConfigManager.toJson(serverConfig), false);
        for (ServerPlayer player : currentServer.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public static void enviarActualizacionGlobalYMostrar(boolean abrir, long timeMs) {
        if (currentServer == null) return;
        isGlobalActive = abrir;
        SyncPayload payload = new SyncPayload(abrir, timeMs, ConfigManager.toJson(serverConfig), true);
        for (ServerPlayer player : currentServer.getPlayerList().getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }
}