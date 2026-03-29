package Luxfiro.instruccioens_eufonia;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mod(Instruccioens_eufonia.MODID)
public class Instruccioens_eufonia {

    public static final String MODID = "instruccioens_eufonia";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<Block> EXAMPLE_BLOCK = BLOCKS.register("example_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE)));
    public static final RegistryObject<Item> EXAMPLE_BLOCK_ITEM = ITEMS.register("example_block", () -> new BlockItem(EXAMPLE_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<Item> EXAMPLE_ITEM = ITEMS.register("example_item", () -> new Item(new Item.Properties().food(new FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build())));

    // DICCIONARIO DE COLORES
    public static final Map<String, Integer> COLOR_MAP = new HashMap<>();
    static {
        Map<String, String> defaultColors = Map.ofEntries(
                Map.entry("white", "#FFFFFF"), Map.entry("red", "#FF5555"), Map.entry("green", "#55FF55"),
                Map.entry("blue", "#5555FF"), Map.entry("gold", "#FFAA00"), Map.entry("mint", "#98FF98"),
                Map.entry("lavender", "#E6E6FA"), Map.entry("sakura", "#FFB7C5"), Map.entry("crimson", "#DC143C"),
                Map.entry("electric_blue", "#7DF9FF"), Map.entry("neon_pink", "#FF10F0"),
                Map.entry("black", "#000000"), Map.entry("gray", "#808080"), Map.entry("silver", "#C0C0C0"),
                Map.entry("orange", "#FFA500"), Map.entry("yellow", "#FFFF00"), Map.entry("lime", "#00FF00"),
                Map.entry("cyan", "#00FFFF"), Map.entry("magenta", "#FF00FF"), Map.entry("purple", "#800080"),
                Map.entry("violet", "#EE82EE"), Map.entry("indigo", "#4B0082"), Map.entry("teal", "#008080"),
                Map.entry("turquoise", "#40E0D0"), Map.entry("coral", "#FF7F50"), Map.entry("salmon", "#FA8072"),
                Map.entry("pink", "#FFC0CB"), Map.entry("rose", "#FF007F"), Map.entry("maroon", "#800000"),
                Map.entry("navy", "#000080"), Map.entry("olive", "#808000"), Map.entry("khaki", "#F0E68C"),
                Map.entry("tan", "#D2B48C"), Map.entry("brown", "#8B4513"), Map.entry("chocolate", "#D2691E"),
                Map.entry("peru", "#CD853F"), Map.entry("tan_dark", "#6F4E37"), Map.entry("sienna", "#A0522D"),
                Map.entry("fire_orange", "#FF4500"), Map.entry("tomato", "#FF6347"), Map.entry("deep_pink", "#FF1493"),
                Map.entry("hot_pink", "#FF69B4"), Map.entry("light_pink", "#FFB6C1"), Map.entry("pale_violet_red", "#DB7093"),
                Map.entry("orchid", "#DA70D6"), Map.entry("medium_purple", "#9370DB"), Map.entry("slate_blue", "#6A5ACD"),
                Map.entry("dark_blue", "#00008B"), Map.entry("steel_blue", "#4682B4"), Map.entry("sky_blue", "#87CEEB"),
                Map.entry("light_blue", "#ADD8E6"), Map.entry("deep_sky_blue", "#00BFFF"), Map.entry("dodger_blue", "#1E90FF"),
                Map.entry("dark_cyan", "#008B8B"), Map.entry("light_cyan", "#E0FFFF"), Map.entry("medium_aquamarine", "#66CDAA"),
                Map.entry("dark_sea_green", "#8FBC8F"), Map.entry("medium_sea_green", "#3CB371"), Map.entry("sea_green", "#2E8B57"),
                Map.entry("forest_green", "#228B22"), Map.entry("dark_green", "#006400"), Map.entry("light_green", "#90EE90"),
                Map.entry("pale_green", "#98FB98"), Map.entry("spring_green", "#00FF7F"), Map.entry("medium_spring_green", "#00FA9A"),
                Map.entry("chartreuse", "#7FFF00"), Map.entry("lawn_green", "#7CFC00"), Map.entry("dark_goldenrod", "#B8860B"),
                Map.entry("goldenrod", "#DAA520"), Map.entry("light_goldenrod", "#FAFAD2"), Map.entry("wheat", "#F5DEB3"),
                Map.entry("burlywood", "#DEB887"), Map.entry("moccasin", "#FFE4B5"), Map.entry("dark_orange", "#FF8C00"),
                Map.entry("light_orange", "#FFCB69"), Map.entry("yellow_green", "#9ACD32"), Map.entry("olive_drab", "#6B8E23"),
                Map.entry("dark_slate_gray", "#2F4F4F"), Map.entry("slate_gray", "#708090"), Map.entry("light_slate_gray", "#778899"),
                Map.entry("dim_gray", "#696969"), Map.entry("light_gray", "#D3D3D3"), Map.entry("gainsboro", "#DCDCDC"),
                Map.entry("white_smoke", "#F5F5F5"), Map.entry("snow", "#FFFAFA"), Map.entry("ghost_white", "#F8F8FF"),
                Map.entry("floral_white", "#FFFAF0"), Map.entry("linen", "#FAF0E6"), Map.entry("antique_white", "#FAEBD7"),
                Map.entry("papaya_whip", "#FFEFD5"), Map.entry("blanched_almond", "#FFEBCD"), Map.entry("bisque", "#FFE4C4"),
                Map.entry("peach_puff", "#FFDAB9"), Map.entry("navajo_white", "#FFDEAD"), Map.entry("misty_rose", "#FFE4E1"),
                Map.entry("thistle", "#D8BFD8"), Map.entry("plum", "#DDA0DD"), Map.entry("honeydew", "#F0FFF0"),
                Map.entry("aqua", "#55FFFF"), Map.entry("dark_aqua", "#00AAAA"), Map.entry("dark_red", "#AA0000"),
                Map.entry("dark_purple", "#AA00AA"), Map.entry("light_purple", "#FF55FF")
        );
        defaultColors.forEach((k, v) -> COLOR_MAP.put(k, Integer.parseInt(v.replace("#", ""), 16) | 0xFF000000));
    }

    // ESTADOS GLOBALES
    public static SaveData serverConfig = new SaveData();
    public static boolean isGlobalActive = false;
    public static long serverTimerEndTime = 0;

    public static SaveData clientConfig = new SaveData();
    public static long clientTimerEndTime = 0;
    public static boolean isTimerActive = false;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final File CONFIG_DIR = FMLPaths.CONFIGDIR.get().resolve(MODID).toFile();

    public static final SimpleChannel CHANNEL = ChannelBuilder.named(ResourceLocation.fromNamespaceAndPath(MODID, "main"))
            .networkProtocolVersion(1)
            .acceptedVersions((status, version) -> true)
            .simpleChannel();

    public Instruccioens_eufonia() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        TABS.register(modEventBus);

        CHANNEL.messageBuilder(SyncPacket.class, 1)
                .encoder((packet, buf) -> SyncPacket.STREAM_CODEC.encode(buf, packet))
                .decoder(buf -> SyncPacket.STREAM_CODEC.decode(buf))
                .consumerNetworkThread((packet, context) -> {
                    context.enqueueWork(packet::handleClient);
                    context.setPacketHandled(true);
                })
                .add();

        MinecraftForge.EVENT_BUS.register(this);

        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
        clientConfig = serverConfig;
    }

    public static class SaveData {
        public String titulo = "SIN TITULO";
        public String subtitulo = "SIN SUBTITULO";
        public String instrucciones = "";
        public List<String> controles = new ArrayList<>();
        public String logoI = null;
        public String logoD = null;
        public boolean blockF1 = true;

        public float escalaTitulo = 1.0f;
        public float escalaSubtitulo = 2.0f;
        public float escalaInstrucciones = 0.85f;
        public float escalaControles = 0.85f;

        public float escalaLogoI = 1.0f;
        public float escalaLogoD = 1.0f;
        public int offsetX_LogoI = 0;
        public int offsetY_LogoI = 0;
        public int offsetX_LogoD = 0;
        public int offsetY_LogoD = 0;

        public int colorFondo = 0xFF000000;
        public int colorFondo2 = 0xFF000000;
        public int dominioFondo = 0;
        public int colorTitulo = 0xFFAAAAAA;
        public int colorSubtitulo = 0xFFFFFFFF;
        public int colorInstrucciones = 0xFFFFFFFF;
        public int colorControles = 0xFFFFFFFF;
        public int colorLineas = 0xFFFFFFFF;
        public int colorReloj = 0xFFFFFFFF;

        public float grosorBarras = 0.25f;
        public boolean mostrarReloj = true;
        public boolean mostrarLineas = true;

        public int offsetY_Titulo = 0;
        public int offsetX_Titulo = 0;
        public int offsetY_Subtitulo = 0;
        public int offsetX_Subtitulo = 0;

        public SaveData validate() {
            if (titulo == null) titulo = "";
            if (subtitulo == null) subtitulo = "";
            if (instrucciones == null) instrucciones = "";
            if (controles == null) controles = new ArrayList<>();
            if (escalaLogoI == 0.0f) escalaLogoI = 1.0f;
            if (escalaLogoD == 0.0f) escalaLogoD = 1.0f;
            return this;
        }
    }

    public record SyncPacket(boolean openScreen, long durationLeftMs, String jsonData, boolean isDisplayCommand) implements CustomPacketPayload {

        public static final Type<SyncPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MODID, "sync"));

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
            clientConfig = GSON.fromJson(this.jsonData, SaveData.class);
            if (clientConfig != null) clientConfig.validate(); else clientConfig = new SaveData();

            if (this.isDisplayCommand) {
                if (this.durationLeftMs > 0) {
                    clientTimerEndTime = System.currentTimeMillis() + this.durationLeftMs;
                    isTimerActive = true;
                } else {
                    clientTimerEndTime = 0;
                    isTimerActive = false;
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

    private void handlePlayerSync(ServerPlayer player) {
        long timeLeft = serverTimerEndTime > 0 ? (serverTimerEndTime - System.currentTimeMillis()) : 0;
        if (isGlobalActive && serverTimerEndTime > 0 && timeLeft <= 0) {
            isGlobalActive = false;
        }
        SyncPacket packet = new SyncPacket(isGlobalActive, timeLeft, GSON.toJson(serverConfig), true);
        CHANNEL.send(packet, PacketDistributor.PLAYER.with(player));
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) { handlePlayerSync(player); }
    }

    @SubscribeEvent
    public void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) { handlePlayerSync(player); }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) { handlePlayerSync(player); }
    }

    public static void sincronizarDatosSilencioso() {
        SyncPacket packet = new SyncPacket(isGlobalActive, 0, GSON.toJson(serverConfig), false);
        CHANNEL.send(packet, PacketDistributor.ALL.noArg());
    }

    public static void enviarActualizacionGlobalYMostrar(boolean abrir, long timeMs) {
        isGlobalActive = abrir;
        SyncPacket packet = new SyncPacket(abrir, timeMs, GSON.toJson(serverConfig), true);
        CHANNEL.send(packet, PacketDistributor.ALL.noArg());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("luxep").requires(s -> s.hasPermission(4))
                .then(Commands.literal("help").executes(c -> {
                    c.getSource().sendSuccess(() -> Component.literal("§6================§e Comandos Luxep §6================"), false);
                    c.getSource().sendSuccess(() -> Component.literal("§e/luxep escena §f<mostrar|mostrar_a|ocultar|bloquear_hud>"), false);
                    c.getSource().sendSuccess(() -> Component.literal("§e/luxep ui texto §f<principal|secundario|instrucciones> <texto>"), false);
                    c.getSource().sendSuccess(() -> Component.literal("§e/luxep ui color fondo §f<color1> [color2]"), false);
                    c.getSource().sendSuccess(() -> Component.literal("§e/luxep ui color dominancia §f<mitad|color1|color2>"), false);
                    c.getSource().sendSuccess(() -> Component.literal("§e/luxep ui color §f<titulo|subtitulo|instrucciones|controles> <color>"), false);
                    c.getSource().sendSuccess(() -> Component.literal("§e/luxep ui escala §f<titulo|subtitulo|instrucciones|controles|logo_principal|logo_secundario> <valor>"), false);
                    c.getSource().sendSuccess(() -> Component.literal("§e/luxep ui offset §f<titulo|subtitulo|logo_principal|logo_secundario> <x> <y>"), false);
                    c.getSource().sendSuccess(() -> Component.literal("§e/luxep ui pantalla §f<grosor_barras|mostrar_reloj|mostrar_lineas>"), false);
                    c.getSource().sendSuccess(() -> Component.literal("§e/luxep controles §f<agregar|modificar|eliminar|limpiar>"), false);
                    c.getSource().sendSuccess(() -> Component.literal("§e/luxep logos §f<principal|secundario|limpiar> <ruta>"), false);
                    c.getSource().sendSuccess(() -> Component.literal("§e/luxep archivo §f<guardar|cargar|listar>"), false);
                    c.getSource().sendSuccess(() -> Component.literal("§6=================================================="), false);
                    return 1;
                }))

                .then(Commands.literal("archivo")
                        .then(Commands.literal("listar").executes(c -> {
                            File[] files = CONFIG_DIR.listFiles((d, name) -> name.endsWith(".json"));
                            if (files == null || files.length == 0) { c.getSource().sendSuccess(() -> Component.literal("§cNo hay configuraciones guardadas."), false); return 1; }
                            c.getSource().sendSuccess(() -> Component.literal("§a--- Configuraciones ---"), false);
                            for (File f : files) c.getSource().sendSuccess(() -> Component.literal("§e- " + f.getName().replace(".json", "")), false);
                            return 1;
                        }))
                        .then(Commands.literal("guardar").then(Commands.argument("nombre", StringArgumentType.word()).executes(c -> {
                            String nombre = StringArgumentType.getString(c, "nombre");
                            try (FileWriter writer = new FileWriter(new File(CONFIG_DIR, nombre + ".json"))) {
                                GSON.toJson(serverConfig, writer);
                                c.getSource().sendSuccess(() -> Component.literal("§aConfiguración '" + nombre + "' guardada."), true);
                            } catch (Exception e) { LOGGER.error("[Eufonia] Error", e); } return 1;
                        })))
                        .then(Commands.literal("cargar").then(Commands.argument("nombre", StringArgumentType.word()).executes(c -> {
                            File f = new File(CONFIG_DIR, StringArgumentType.getString(c, "nombre") + ".json");
                            if (!f.exists()) { c.getSource().sendSuccess(() -> Component.literal("§cArchivo no existe."), false); return 0; }
                            try (FileReader reader = new FileReader(f)) {
                                serverConfig = GSON.fromJson(reader, SaveData.class);
                                if (serverConfig != null) serverConfig.validate(); else serverConfig = new SaveData();
                                sincronizarDatosSilencioso();
                                c.getSource().sendSuccess(() -> Component.literal("§aCargado en silencio (usa /luxep escena mostrar para verlo)."), true);
                            } catch (Exception e) { LOGGER.error("[Eufonia] Error", e); } return 1;
                        })))
                )

                .then(Commands.literal("escena")
                        .then(Commands.literal("mostrar")
                                .executes(c -> ejecutarMostrar(c.getSource(), null, "0"))
                                .then(Commands.argument("tiempo_hhmmss", StringArgumentType.string())
                                        .executes(c -> ejecutarMostrar(c.getSource(), null, StringArgumentType.getString(c, "tiempo_hhmmss")))))

                        .then(Commands.literal("mostrar_a")
                                .then(Commands.argument("jugadores", EntityArgument.players())
                                        .executes(c -> ejecutarMostrar(c.getSource(), EntityArgument.getPlayers(c, "jugadores"), "0"))
                                        .then(Commands.argument("tiempo_hhmmss", StringArgumentType.string())
                                                .executes(c -> ejecutarMostrar(c.getSource(), EntityArgument.getPlayers(c, "jugadores"), StringArgumentType.getString(c, "tiempo_hhmmss"))))))

                        .then(Commands.literal("ocultar").executes(c -> {
                            enviarActualizacionGlobalYMostrar(false, 0);
                            c.getSource().sendSuccess(() -> Component.literal("§cPantalla GLOBAL Cerrada"), true); return 1;
                        }))
                        .then(Commands.literal("bloquear_hud").then(Commands.argument("estado", BoolArgumentType.bool()).executes(c -> {
                            serverConfig.blockF1 = BoolArgumentType.getBool(c, "estado"); sincronizarDatosSilencioso();
                            c.getSource().sendSuccess(() -> Component.literal(serverConfig.blockF1 ? "§aBloqueo F1 ON" : "§cBloqueo F1 OFF"), true); return 1;
                        })))
                )

                .then(Commands.literal("ui")
                        .then(Commands.literal("texto")
                                .then(Commands.literal("principal").then(Commands.argument("texto", StringArgumentType.greedyString()).executes(c -> {
                                    serverConfig.titulo = formatearTexto(StringArgumentType.getString(c, "texto")); sincronizarDatosSilencioso(); return 1;
                                })))
                                .then(Commands.literal("secundario").then(Commands.argument("texto", StringArgumentType.greedyString()).executes(c -> {
                                    serverConfig.subtitulo = formatearTexto(StringArgumentType.getString(c, "texto")); sincronizarDatosSilencioso(); return 1;
                                })))
                                .then(Commands.literal("instrucciones").then(Commands.argument("texto", StringArgumentType.greedyString()).executes(c -> {
                                    serverConfig.instrucciones = formatearTexto(StringArgumentType.getString(c, "texto")); sincronizarDatosSilencioso(); return 1;
                                })))
                        )
                        .then(Commands.literal("color")
                                .then(Commands.literal("fondo")
                                        .then(Commands.argument("color1", StringArgumentType.word()).suggests((context, builder) -> SharedSuggestionProvider.suggest(COLOR_MAP.keySet(), builder))
                                                .executes(c -> {
                                                    serverConfig.colorFondo = parseColor(StringArgumentType.getString(c, "color1"), serverConfig.colorFondo);
                                                    serverConfig.colorFondo2 = serverConfig.colorFondo;
                                                    sincronizarDatosSilencioso(); return 1;
                                                })
                                                .then(Commands.argument("color2", StringArgumentType.word()).suggests((context, builder) -> SharedSuggestionProvider.suggest(COLOR_MAP.keySet(), builder))
                                                        .executes(c -> {
                                                            serverConfig.colorFondo = parseColor(StringArgumentType.getString(c, "color1"), serverConfig.colorFondo);
                                                            serverConfig.colorFondo2 = parseColor(StringArgumentType.getString(c, "color2"), serverConfig.colorFondo2);
                                                            sincronizarDatosSilencioso(); return 1;
                                                        })
                                                )
                                        )
                                )
                                .then(Commands.literal("dominancia")
                                        .then(Commands.literal("mitad").executes(c -> { serverConfig.dominioFondo = 0; sincronizarDatosSilencioso(); c.getSource().sendSuccess(() -> Component.literal("§aFondo: 50/50"), true); return 1; }))
                                        .then(Commands.literal("color1").executes(c -> { serverConfig.dominioFondo = 1; sincronizarDatosSilencioso(); c.getSource().sendSuccess(() -> Component.literal("§aFondo: Color 1 Dominante"), true); return 1; }))
                                        .then(Commands.literal("color2").executes(c -> { serverConfig.dominioFondo = 2; sincronizarDatosSilencioso(); c.getSource().sendSuccess(() -> Component.literal("§aFondo: Color 2 Dominante"), true); return 1; }))
                                )
                                .then(Commands.literal("titulo").then(Commands.argument("color", StringArgumentType.word()).suggests((context, builder) -> SharedSuggestionProvider.suggest(COLOR_MAP.keySet(), builder)).executes(c -> { serverConfig.colorTitulo = parseColor(StringArgumentType.getString(c, "color"), serverConfig.colorTitulo); sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("subtitulo").then(Commands.argument("color", StringArgumentType.word()).suggests((context, builder) -> SharedSuggestionProvider.suggest(COLOR_MAP.keySet(), builder)).executes(c -> { serverConfig.colorSubtitulo = parseColor(StringArgumentType.getString(c, "color"), serverConfig.colorSubtitulo); sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("instrucciones").then(Commands.argument("color", StringArgumentType.word()).suggests((context, builder) -> SharedSuggestionProvider.suggest(COLOR_MAP.keySet(), builder)).executes(c -> { serverConfig.colorInstrucciones = parseColor(StringArgumentType.getString(c, "color"), serverConfig.colorInstrucciones); sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("controles").then(Commands.argument("color", StringArgumentType.word()).suggests((context, builder) -> SharedSuggestionProvider.suggest(COLOR_MAP.keySet(), builder)).executes(c -> { serverConfig.colorControles = parseColor(StringArgumentType.getString(c, "color"), serverConfig.colorControles); sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("lineas").then(Commands.argument("color", StringArgumentType.word()).suggests((context, builder) -> SharedSuggestionProvider.suggest(COLOR_MAP.keySet(), builder)).executes(c -> { serverConfig.colorLineas = parseColor(StringArgumentType.getString(c, "color"), serverConfig.colorLineas); sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("reloj").then(Commands.argument("color", StringArgumentType.word()).suggests((context, builder) -> SharedSuggestionProvider.suggest(COLOR_MAP.keySet(), builder)).executes(c -> { serverConfig.colorReloj = parseColor(StringArgumentType.getString(c, "color"), serverConfig.colorReloj); sincronizarDatosSilencioso(); return 1; })))
                        )
                        .then(Commands.literal("offset")
                                .then(Commands.literal("titulo")
                                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                                .then(Commands.argument("y", IntegerArgumentType.integer()).executes(c -> {
                                                    serverConfig.offsetX_Titulo = IntegerArgumentType.getInteger(c, "x");
                                                    serverConfig.offsetY_Titulo = IntegerArgumentType.getInteger(c, "y");
                                                    sincronizarDatosSilencioso(); return 1;
                                                }))))
                                .then(Commands.literal("subtitulo")
                                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                                .then(Commands.argument("y", IntegerArgumentType.integer()).executes(c -> {
                                                    serverConfig.offsetX_Subtitulo = IntegerArgumentType.getInteger(c, "x");
                                                    serverConfig.offsetY_Subtitulo = IntegerArgumentType.getInteger(c, "y");
                                                    sincronizarDatosSilencioso(); return 1;
                                                }))))
                                .then(Commands.literal("logo_principal")
                                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                                .then(Commands.argument("y", IntegerArgumentType.integer()).executes(c -> {
                                                    serverConfig.offsetX_LogoI = IntegerArgumentType.getInteger(c, "x");
                                                    serverConfig.offsetY_LogoI = IntegerArgumentType.getInteger(c, "y");
                                                    sincronizarDatosSilencioso(); return 1;
                                                }))))
                                .then(Commands.literal("logo_secundario")
                                        .then(Commands.argument("x", IntegerArgumentType.integer())
                                                .then(Commands.argument("y", IntegerArgumentType.integer()).executes(c -> {
                                                    serverConfig.offsetX_LogoD = IntegerArgumentType.getInteger(c, "x");
                                                    serverConfig.offsetY_LogoD = IntegerArgumentType.getInteger(c, "y");
                                                    sincronizarDatosSilencioso(); return 1;
                                                }))))
                        )
                        .then(Commands.literal("escala")
                                .then(Commands.literal("titulo").then(Commands.argument("valor", FloatArgumentType.floatArg(0.1f, 10.0f)).executes(c -> { serverConfig.escalaTitulo = FloatArgumentType.getFloat(c, "valor"); sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("subtitulo").then(Commands.argument("valor", FloatArgumentType.floatArg(0.1f, 10.0f)).executes(c -> { serverConfig.escalaSubtitulo = FloatArgumentType.getFloat(c, "valor"); sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("instrucciones").then(Commands.argument("valor", FloatArgumentType.floatArg(0.1f, 10.0f)).executes(c -> { serverConfig.escalaInstrucciones = FloatArgumentType.getFloat(c, "valor"); sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("controles").then(Commands.argument("valor", FloatArgumentType.floatArg(0.1f, 10.0f)).executes(c -> { serverConfig.escalaControles = FloatArgumentType.getFloat(c, "valor"); sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("logo_principal").then(Commands.argument("valor", FloatArgumentType.floatArg(0.1f, 10.0f)).executes(c -> { serverConfig.escalaLogoI = FloatArgumentType.getFloat(c, "valor"); sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("logo_secundario").then(Commands.argument("valor", FloatArgumentType.floatArg(0.1f, 10.0f)).executes(c -> { serverConfig.escalaLogoD = FloatArgumentType.getFloat(c, "valor"); sincronizarDatosSilencioso(); return 1; })))
                        )
                        .then(Commands.literal("pantalla")
                                .then(Commands.literal("grosor_barras").then(Commands.argument("porcentaje", FloatArgumentType.floatArg(0.0f, 0.45f)).executes(c -> { serverConfig.grosorBarras = FloatArgumentType.getFloat(c, "porcentaje"); sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("mostrar_reloj").then(Commands.argument("estado", BoolArgumentType.bool()).executes(c -> { serverConfig.mostrarReloj = BoolArgumentType.getBool(c, "estado"); sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("mostrar_lineas").then(Commands.argument("estado", BoolArgumentType.bool()).executes(c -> { serverConfig.mostrarLineas = BoolArgumentType.getBool(c, "estado"); sincronizarDatosSilencioso(); return 1; })))
                        )
                        .then(Commands.literal("controles")
                                .then(Commands.literal("agregar").then(Commands.argument("texto", StringArgumentType.greedyString()).executes(c -> {
                                    serverConfig.controles.add(formatearTexto(StringArgumentType.getString(c, "texto")));
                                    sincronizarDatosSilencioso(); c.getSource().sendSuccess(() -> Component.literal("§aControl agregado."), true); return 1;
                                })))
                                .then(Commands.literal("modificar").then(Commands.argument("indice", IntegerArgumentType.integer(1)).then(Commands.argument("texto", StringArgumentType.greedyString()).executes(c -> {
                                    int index = IntegerArgumentType.getInteger(c, "indice") - 1;
                                    if (index >= 0 && index < serverConfig.controles.size()) {
                                        serverConfig.controles.set(index, formatearTexto(StringArgumentType.getString(c, "texto")));
                                        sincronizarDatosSilencioso(); c.getSource().sendSuccess(() -> Component.literal("§aControl " + (index+1) + " modificado."), true);
                                    } else { c.getSource().sendFailure(Component.literal("§cÍndice fuera de rango.")); }
                                    return 1;
                                }))))
                                .then(Commands.literal("eliminar").then(Commands.argument("indice", IntegerArgumentType.integer(1)).executes(c -> {
                                    int index = IntegerArgumentType.getInteger(c, "indice") - 1;
                                    if (index >= 0 && index < serverConfig.controles.size()) {
                                        serverConfig.controles.remove(index);
                                        sincronizarDatosSilencioso(); c.getSource().sendSuccess(() -> Component.literal("§aControl " + (index+1) + " eliminado."), true);
                                    } else { c.getSource().sendFailure(Component.literal("§cÍndice fuera de rango.")); }
                                    return 1;
                                })))
                                .then(Commands.literal("limpiar").executes(c -> {
                                    serverConfig.controles.clear(); sincronizarDatosSilencioso(); c.getSource().sendSuccess(() -> Component.literal("§aLista de controles limpiada."), true); return 1;
                                }))
                        )
                        .then(Commands.literal("logos")
                                .then(Commands.literal("principal").then(Commands.argument("ruta", StringArgumentType.string()).executes(c -> { serverConfig.logoI = StringArgumentType.getString(c, "ruta"); sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("secundario").then(Commands.argument("ruta", StringArgumentType.string()).executes(c -> { serverConfig.logoD = StringArgumentType.getString(c, "ruta"); sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("limpiar").executes(c -> { serverConfig.logoI = null; serverConfig.logoD = null; sincronizarDatosSilencioso(); return 1; }))
                        )
                )
        );
    }

    private static int ejecutarMostrar(CommandSourceStack source, Collection<ServerPlayer> targets, String tiempoStr) {
        long timeMs = parseTime(tiempoStr);
        String msgEx = timeMs > 0 ? " por " + tiempoStr : " permanentemente";

        if (targets == null) {
            serverTimerEndTime = timeMs > 0 ? System.currentTimeMillis() + timeMs : 0;
            enviarActualizacionGlobalYMostrar(true, timeMs);
            source.sendSuccess(() -> Component.literal("§aPantalla GLOBAL Abierta" + msgEx), true);
        } else {
            SyncPacket showPacket = new SyncPacket(true, timeMs, GSON.toJson(serverConfig), true);
            for(ServerPlayer p : targets) CHANNEL.send(showPacket, PacketDistributor.PLAYER.with(p));
            source.sendSuccess(() -> Component.literal("§aPantalla mostrada a " + targets.size() + " jugador(es)" + msgEx), true);
        }
        return 1;
    }

    private static int parseColor(String input, int fallback) {
        if (input.startsWith("#")) {
            try { return (int) Long.parseLong(input.replace("#", ""), 16) | 0xFF000000; }
            catch (Exception e) { return fallback; }
        }
        return COLOR_MAP.getOrDefault(input.toLowerCase(), fallback);
    }

    public static String formatearTexto(String input) {
        if (input == null || input.isEmpty()) return "";
        String procesado = input.replace(";", "\n");

        Matcher unicodeMatcher = Pattern.compile("\\\\u([0-9a-fA-F]{4})").matcher(procesado);
        StringBuffer usb = new StringBuffer();
        while (unicodeMatcher.find()) {
            try {
                char decoded = (char) Integer.parseInt(unicodeMatcher.group(1), 16);
                unicodeMatcher.appendReplacement(usb, String.valueOf(decoded));
            } catch (Exception e) {
                unicodeMatcher.appendReplacement(usb, unicodeMatcher.group(0));
            }
        }
        unicodeMatcher.appendTail(usb);
        procesado = usb.toString();

        Pattern pattern = Pattern.compile("([a-zA-Z_]+|#[0-9a-fA-F]{6})#(.*?)#");
        Matcher matcher = pattern.matcher(procesado);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String colorName = matcher.group(1);
            String text = matcher.group(2);
            int colorHex;

            if (colorName.startsWith("#")) {
                colorHex = Integer.parseInt(colorName.substring(1), 16);
            } else {
                colorHex = COLOR_MAP.getOrDefault(colorName.toLowerCase(), 0xFFFFFF) & 0x00FFFFFF;
            }

            StringBuilder mcColor = new StringBuilder("§x");
            for (char c : String.format("%06X", colorHex).toCharArray()) {
                mcColor.append('§').append(Character.toLowerCase(c));
            }

            matcher.appendReplacement(sb, mcColor.toString() + text + "§r");
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private static long parseTime(String timeStr) {
        if (timeStr == null || timeStr.equals("0")) return 0;
        timeStr = timeStr.replace("\"", "").replace("'", "");
        String[] parts = timeStr.split(":");
        long ms = 0;
        try {
            if (parts.length == 3) { ms += Integer.parseInt(parts[0]) * 3600000L; ms += Integer.parseInt(parts[1]) * 60000L; ms += Integer.parseInt(parts[2]) * 1000L; }
            else if (parts.length == 2) { ms += Integer.parseInt(parts[0]) * 60000L; ms += Integer.parseInt(parts[1]) * 1000L; }
            else if (parts.length == 1) { ms += Integer.parseInt(parts[0]) * 1000L; }
        } catch (Exception ignored) {}
        return ms;
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientForgeEvents {
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
                    mc.gameRenderer.loadEffect(ResourceLocation.parse(MODID + ":shaders/post/camara.json"));
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

            if (isTimerActive && PantallaEvento.fadePhase == 0 && mc.screen instanceof PantallaEvento) {
                if (current >= clientTimerEndTime) {
                    isTimerActive = false;
                    evaluarFade(false);
                }
            }

            if (clientConfig.blockF1 && mc.screen instanceof PantallaEvento) {
                mc.options.hideGui = false;
                for (net.minecraft.client.KeyMapping key : mc.options.keyMappings) {
                    if (key.getName().equals("key.toggleGui")) {
                        while (key.consumeClick()) {}
                    }
                }
            }
        }
    }

    public static class PantallaEvento extends Screen {

        public static int fadePhase = 0;
        public static long fadeStartTime = 0;
        public static final long FADE_DURATION = 2500L;

        protected PantallaEvento() { super(Component.literal("Pantalla Evento")); }

        private int aplicarAlpha(int colorOriginal, float alphaMod) {
            int a = (colorOriginal >> 24) & 0xFF;
            a = (int)(a * alphaMod);
            return (a << 24) | (colorOriginal & 0x00FFFFFF);
        }

        private int interpolateColor(int c1, int c2, float fraction, int dominio) {
            if (dominio == 1) { fraction = (float) Math.pow(fraction, 6.0); }
            else if (dominio == 2) { fraction = (float) Math.pow(fraction, 0.16); }

            int a1 = (c1 >> 24) & 0xFF, r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
            int a2 = (c2 >> 24) & 0xFF, r2 = (c2 >> 16) & 0xFF, g2 = (c2 >> 8) & 0xFF, b2 = c2 & 0xFF;
            int a = (int)(a1 + (a2 - a1) * fraction);
            int r = (int)(r1 + (r2 - r1) * fraction);
            int g = (int)(g1 + (g2 - g1) * fraction);
            int b = (int)(b1 + (b2 - b1) * fraction);
            return (a << 24) | (r << 16) | (g << 8) | b;
        }

        @Override
        public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            // Vacío intencionalmente para evitar el fondo por defecto
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            Minecraft mc = Minecraft.getInstance();
            if (clientConfig.blockF1) mc.options.hideGui = false;

            int width = this.width; int height = this.height;

            long elapsedFade = System.currentTimeMillis() - fadeStartTime;
            float progress = Math.min(1.0f, (float)elapsedFade / FADE_DURATION);

            float overlayAlpha = 0.0f;
            boolean drawUI = false;

            if (fadePhase == 1) { overlayAlpha = progress; drawUI = false; }
            else if (fadePhase == 2) { overlayAlpha = 1.0f - progress; drawUI = true; }
            else if (fadePhase == 3) { overlayAlpha = progress; drawUI = true; }
            else if (fadePhase == 4) { overlayAlpha = 1.0f - progress; drawUI = false; }
            else { overlayAlpha = 0.0f; drawUI = true; }

            if (drawUI) {
                float sTitulo = clientConfig.escalaTitulo;
                float sSubtitulo = clientConfig.escalaSubtitulo;
                float sInst = clientConfig.escalaInstrucciones;
                float sControles = clientConfig.escalaControles;

                int topUI = (int)(height * clientConfig.grosorBarras);
                int bottomUI = (int)(height * (1.0f - clientConfig.grosorBarras));
                int midUI = (int)(width * 0.50);

                int camTop = topUI + (int)(height * 0.02f);
                int camBottom = bottomUI - (int)(height * 0.02f);
                int camLeft = (int)(width * 0.52); int camRight = (int)(width * 0.98);

                int pad = 30;

                int tituloX = (width / 2) + clientConfig.offsetX_Titulo;
                int tituloY = (topUI / 2 - 15) + clientConfig.offsetY_Titulo;
                int subtituloX = (width / 2) + clientConfig.offsetX_Subtitulo;
                int subtituloY = (topUI / 2 + 5) + clientConfig.offsetY_Subtitulo;

                int controlesCabeceraY = topUI + 15; int controlesListaY = topUI + 32;
                int instCabeceraY = bottomUI + 15; int instTextoY = bottomUI + 32;

                int cFondo1 = aplicarAlpha(clientConfig.colorFondo, 1.0f);
                int cFondo2 = aplicarAlpha(clientConfig.colorFondo2, 1.0f);
                int cLineas = aplicarAlpha(clientConfig.colorLineas, 1.0f);

                RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc(); RenderSystem.disableDepthTest();

                float fracTop = (float)camTop / height;
                float fracBottom = (float)camBottom / height;

                int cMidTop = interpolateColor(cFondo1, cFondo2, fracTop, clientConfig.dominioFondo);
                int cMidBottom = interpolateColor(cFondo1, cFondo2, fracBottom, clientConfig.dominioFondo);

                graphics.fillGradient(0, 0, width, camTop, cFondo1, cMidTop);
                graphics.fillGradient(0, camBottom, width, height, cMidBottom, cFondo2);
                graphics.fillGradient(0, camTop, camLeft, camBottom, cMidTop, cMidBottom);
                graphics.fillGradient(camRight, camTop, width, camBottom, cMidTop, cMidBottom);

                if (clientConfig.mostrarLineas) {
                    graphics.fill(0, topUI - 1, width, topUI + 1, cLineas); graphics.fill(0, bottomUI - 1, width, bottomUI + 1, cLineas);
                    graphics.fill(midUI - 1, topUI, midUI + 1, bottomUI, cLineas);
                    graphics.fill(camLeft - 1, camTop - 1, camRight + 1, camTop, cLineas); graphics.fill(camLeft - 1, camBottom, camRight + 1, camBottom + 1, cLineas);
                    graphics.fill(camLeft - 1, camTop, camLeft, camBottom, cLineas); graphics.fill(camRight, camTop, camRight + 1, camBottom, cLineas);
                }

                if (clientConfig.logoI != null) {
                    graphics.pose().pushPose();
                    graphics.pose().translate(20 + clientConfig.offsetX_LogoI, 20 + clientConfig.offsetY_LogoI, 0);
                    graphics.pose().scale(clientConfig.escalaLogoI, clientConfig.escalaLogoI, clientConfig.escalaLogoI);
                    graphics.blit(ResourceLocation.fromNamespaceAndPath(MODID, clientConfig.logoI), 0, 0, 0, 0, 64, 64, 64, 64);
                    graphics.pose().popPose();
                }

                if (clientConfig.logoD != null) {
                    graphics.pose().pushPose();
                    graphics.pose().translate((width - 84) + clientConfig.offsetX_LogoD, 20 + clientConfig.offsetY_LogoD, 0);
                    graphics.pose().scale(clientConfig.escalaLogoD, clientConfig.escalaLogoD, clientConfig.escalaLogoD);
                    graphics.blit(ResourceLocation.fromNamespaceAndPath(MODID, clientConfig.logoD), 0, 0, 0, 0, 64, 64, 64, 64);
                    graphics.pose().popPose();
                }

                if (isTimerActive && clientConfig.mostrarReloj) {
                    long timeLeft = Math.max(0, clientTimerEndTime - System.currentTimeMillis());
                    long ts = timeLeft / 1000;
                    String timeStr = String.format("%02d:%02d:%02d", ts / 3600, (ts % 3600) / 60, ts % 60);
                    graphics.drawCenteredString(this.font, timeStr, width / 2, 10, clientConfig.colorReloj);
                }

                graphics.pose().pushPose(); graphics.pose().scale(sTitulo, sTitulo, sTitulo);
                String[] lineasTitulo = clientConfig.titulo.split("\n");
                for (int i = 0; i < lineasTitulo.length; i++) graphics.drawCenteredString(this.font, lineasTitulo[i], (int) (tituloX / sTitulo), (int) ((tituloY + (i * 12)) / sTitulo), clientConfig.colorTitulo);
                graphics.pose().popPose();

                graphics.pose().pushPose(); graphics.pose().scale(sSubtitulo, sSubtitulo, sSubtitulo);
                String[] lineasSubtitulo = clientConfig.subtitulo.split("\n");
                for (int i = 0; i < lineasSubtitulo.length; i++) graphics.drawCenteredString(this.font, lineasSubtitulo[i], (int) (subtituloX / sSubtitulo), (int) ((subtituloY + (i * 12)) / sSubtitulo), clientConfig.colorSubtitulo);
                graphics.pose().popPose();

                int colorTextoBase = 0xFFFFFFFF;

                graphics.pose().pushPose(); graphics.pose().scale(1.1f, 1.1f, 1.1f);
                graphics.drawString(this.font, "Controles Especiales", (int)(pad / 1.1f), (int)(controlesCabeceraY / 1.1f), clientConfig.colorControles);
                graphics.pose().popPose();

                graphics.pose().pushPose(); graphics.pose().scale(sControles, sControles, sControles);
                for (int i = 0; i < clientConfig.controles.size(); i++) {
                    String[] subLineas = clientConfig.controles.get(i).split("\n");
                    for (int j = 0; j < subLineas.length; j++) graphics.drawString(this.font, subLineas[j], (int)(pad / sControles), (int)((controlesListaY + (i * 15) + (j * 10)) / sControles), colorTextoBase);
                }
                graphics.pose().popPose();

                graphics.pose().pushPose(); graphics.pose().scale(1.1f, 1.1f, 1.1f);
                graphics.drawString(this.font, "Instrucciones", (int)(pad / 1.1f), (int)(instCabeceraY / 1.1f), clientConfig.colorInstrucciones);
                graphics.pose().popPose();

                graphics.pose().pushPose(); graphics.pose().scale(sInst, sInst, sInst);
                int wrapWidth = (int)((width - (pad * 2)) / sInst);
                graphics.drawWordWrap(this.font, Component.literal(clientConfig.instrucciones), (int)(pad / sInst), (int)(instTextoY / sInst), wrapWidth, colorTextoBase);
                graphics.pose().popPose();

                RenderSystem.enableDepthTest(); RenderSystem.disableBlend();
            }

            if (overlayAlpha > 0.0f) {
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableDepthTest();
                int alphaInt = (int)(overlayAlpha * 255.0f);
                int blackARGB = (alphaInt << 24) | 0x000000;

                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, 5000);
                graphics.fill(0, 0, width, height, blackARGB);
                graphics.pose().popPose();

                RenderSystem.enableDepthTest();
                RenderSystem.disableBlend();
            }
        }

        public boolean shouldCloseOnEsc() { return false; }
        public boolean isPauseScreen() { return false; }
    }
}