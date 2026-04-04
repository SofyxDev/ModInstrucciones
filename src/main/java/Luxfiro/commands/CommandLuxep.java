package Luxfiro.instrucciones.commands;

import Luxfiro.instrucciones.InstruccionesMod;
import Luxfiro.instrucciones.config.ConfigManager;
import Luxfiro.instrucciones.network.SyncPayload;
import Luxfiro.instrucciones.util.ColorDictionary;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.File;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandLuxep {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("luxep").requires(s -> s.hasPermission(2))
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
                            File[] files = ConfigManager.CONFIG_DIR.listFiles((d, name) -> name.endsWith(".json"));
                            if (files == null || files.length == 0) { c.getSource().sendSuccess(() -> Component.literal("§cNo hay configuraciones guardadas."), false); return 1; }
                            c.getSource().sendSuccess(() -> Component.literal("§a--- Configuraciones ---"), false);
                            for (File f : files) c.getSource().sendSuccess(() -> Component.literal("§e- " + f.getName().replace(".json", "")), false);
                            return 1;
                        }))
                        .then(Commands.literal("guardar").then(Commands.argument("nombre", StringArgumentType.word()).executes(c -> {
                            String nombre = StringArgumentType.getString(c, "nombre");
                            ConfigManager.save(nombre);
                            c.getSource().sendSuccess(() -> Component.literal("§aConfiguración '" + nombre + "' guardada."), true);
                            return 1;
                        })))
                        .then(Commands.literal("cargar").then(Commands.argument("nombre", StringArgumentType.word()).executes(c -> {
                            if(ConfigManager.load(StringArgumentType.getString(c, "nombre"))) {
                                c.getSource().sendSuccess(() -> Component.literal("§aCargado en silencio (usa /luxep escena mostrar para verlo)."), true);
                            } else {
                                c.getSource().sendSuccess(() -> Component.literal("§cArchivo no existe o error al cargar."), false);
                            }
                            return 1;
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
                            InstruccionesMod.enviarActualizacionGlobalYMostrar(false, 0);
                            c.getSource().sendSuccess(() -> Component.literal("§cPantalla GLOBAL Cerrada"), true); return 1;
                        }))
                        .then(Commands.literal("bloquear_hud").then(Commands.argument("estado", BoolArgumentType.bool()).executes(c -> {
                            InstruccionesMod.serverConfig.blockF1 = BoolArgumentType.getBool(c, "estado"); InstruccionesMod.sincronizarDatosSilencioso();
                            c.getSource().sendSuccess(() -> Component.literal(InstruccionesMod.serverConfig.blockF1 ? "§aBloqueo F1 ON" : "§cBloqueo F1 OFF"), true); return 1;
                        })))
                )

                .then(Commands.literal("ui")
                        .then(Commands.literal("texto")
                                .then(Commands.literal("principal").then(Commands.argument("texto", StringArgumentType.greedyString()).executes(c -> {
                                    InstruccionesMod.serverConfig.titulo = formatearTexto(StringArgumentType.getString(c, "texto")); InstruccionesMod.sincronizarDatosSilencioso(); return 1;
                                })))
                                .then(Commands.literal("secundario").then(Commands.argument("texto", StringArgumentType.greedyString()).executes(c -> {
                                    InstruccionesMod.serverConfig.subtitulo = formatearTexto(StringArgumentType.getString(c, "texto")); InstruccionesMod.sincronizarDatosSilencioso(); return 1;
                                })))
                                .then(Commands.literal("instrucciones").then(Commands.argument("texto", StringArgumentType.greedyString()).executes(c -> {
                                    InstruccionesMod.serverConfig.instrucciones = formatearTexto(StringArgumentType.getString(c, "texto")); InstruccionesMod.sincronizarDatosSilencioso(); return 1;
                                })))
                        )
                        .then(Commands.literal("color")
                                .then(Commands.literal("fondo")
                                        .then(Commands.argument("color1", StringArgumentType.word()).suggests((context, builder) -> SharedSuggestionProvider.suggest(ColorDictionary.COLOR_MAP.keySet(), builder))
                                                .executes(c -> {
                                                    InstruccionesMod.serverConfig.colorFondo = ColorDictionary.parseColor(StringArgumentType.getString(c, "color1"), InstruccionesMod.serverConfig.colorFondo);
                                                    InstruccionesMod.serverConfig.colorFondo2 = InstruccionesMod.serverConfig.colorFondo;
                                                    InstruccionesMod.sincronizarDatosSilencioso(); return 1;
                                                })
                                                .then(Commands.argument("color2", StringArgumentType.word()).suggests((context, builder) -> SharedSuggestionProvider.suggest(ColorDictionary.COLOR_MAP.keySet(), builder))
                                                        .executes(c -> {
                                                            InstruccionesMod.serverConfig.colorFondo = ColorDictionary.parseColor(StringArgumentType.getString(c, "color1"), InstruccionesMod.serverConfig.colorFondo);
                                                            InstruccionesMod.serverConfig.colorFondo2 = ColorDictionary.parseColor(StringArgumentType.getString(c, "color2"), InstruccionesMod.serverConfig.colorFondo2);
                                                            InstruccionesMod.sincronizarDatosSilencioso(); return 1;
                                                        })
                                                )
                                        )
                                )
                                .then(Commands.literal("dominancia")
                                        .then(Commands.literal("mitad").executes(c -> { InstruccionesMod.serverConfig.dominioFondo = 0; InstruccionesMod.sincronizarDatosSilencioso(); c.getSource().sendSuccess(() -> Component.literal("§aFondo: 50/50"), true); return 1; }))
                                        .then(Commands.literal("color1").executes(c -> { InstruccionesMod.serverConfig.dominioFondo = 1; InstruccionesMod.sincronizarDatosSilencioso(); c.getSource().sendSuccess(() -> Component.literal("§aFondo: Color 1 Dominante"), true); return 1; }))
                                        .then(Commands.literal("color2").executes(c -> { InstruccionesMod.serverConfig.dominioFondo = 2; InstruccionesMod.sincronizarDatosSilencioso(); c.getSource().sendSuccess(() -> Component.literal("§aFondo: Color 2 Dominante"), true); return 1; }))
                                )
                                .then(Commands.literal("titulo").then(Commands.argument("color", StringArgumentType.word()).suggests((context, builder) -> SharedSuggestionProvider.suggest(ColorDictionary.COLOR_MAP.keySet(), builder)).executes(c -> { InstruccionesMod.serverConfig.colorTitulo = ColorDictionary.parseColor(StringArgumentType.getString(c, "color"), InstruccionesMod.serverConfig.colorTitulo); InstruccionesMod.sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("subtitulo").then(Commands.argument("color", StringArgumentType.word()).suggests((context, builder) -> SharedSuggestionProvider.suggest(ColorDictionary.COLOR_MAP.keySet(), builder)).executes(c -> { InstruccionesMod.serverConfig.colorSubtitulo = ColorDictionary.parseColor(StringArgumentType.getString(c, "color"), InstruccionesMod.serverConfig.colorSubtitulo); InstruccionesMod.sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("instrucciones").then(Commands.argument("color", StringArgumentType.word()).suggests((context, builder) -> SharedSuggestionProvider.suggest(ColorDictionary.COLOR_MAP.keySet(), builder)).executes(c -> { InstruccionesMod.serverConfig.colorInstrucciones = ColorDictionary.parseColor(StringArgumentType.getString(c, "color"), InstruccionesMod.serverConfig.colorInstrucciones); InstruccionesMod.sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("controles").then(Commands.argument("color", StringArgumentType.word()).suggests((context, builder) -> SharedSuggestionProvider.suggest(ColorDictionary.COLOR_MAP.keySet(), builder)).executes(c -> { InstruccionesMod.serverConfig.colorControles = ColorDictionary.parseColor(StringArgumentType.getString(c, "color"), InstruccionesMod.serverConfig.colorControles); InstruccionesMod.sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("lineas").then(Commands.argument("color", StringArgumentType.word()).suggests((context, builder) -> SharedSuggestionProvider.suggest(ColorDictionary.COLOR_MAP.keySet(), builder)).executes(c -> { InstruccionesMod.serverConfig.colorLineas = ColorDictionary.parseColor(StringArgumentType.getString(c, "color"), InstruccionesMod.serverConfig.colorLineas); InstruccionesMod.sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("reloj").then(Commands.argument("color", StringArgumentType.word()).suggests((context, builder) -> SharedSuggestionProvider.suggest(ColorDictionary.COLOR_MAP.keySet(), builder)).executes(c -> { InstruccionesMod.serverConfig.colorReloj = ColorDictionary.parseColor(StringArgumentType.getString(c, "color"), InstruccionesMod.serverConfig.colorReloj); InstruccionesMod.sincronizarDatosSilencioso(); return 1; })))
                        )
                        .then(Commands.literal("offset")
                                .then(Commands.literal("titulo")
                                        .then(Commands.argument("x", IntegerArgumentType.integer()).then(Commands.argument("y", IntegerArgumentType.integer()).executes(c -> {
                                            InstruccionesMod.serverConfig.offsetX_Titulo = IntegerArgumentType.getInteger(c, "x"); InstruccionesMod.serverConfig.offsetY_Titulo = IntegerArgumentType.getInteger(c, "y");
                                            InstruccionesMod.sincronizarDatosSilencioso(); return 1;
                                        }))))
                                .then(Commands.literal("subtitulo")
                                        .then(Commands.argument("x", IntegerArgumentType.integer()).then(Commands.argument("y", IntegerArgumentType.integer()).executes(c -> {
                                            InstruccionesMod.serverConfig.offsetX_Subtitulo = IntegerArgumentType.getInteger(c, "x"); InstruccionesMod.serverConfig.offsetY_Subtitulo = IntegerArgumentType.getInteger(c, "y");
                                            InstruccionesMod.sincronizarDatosSilencioso(); return 1;
                                        }))))
                                .then(Commands.literal("logo_principal")
                                        .then(Commands.argument("x", IntegerArgumentType.integer()).then(Commands.argument("y", IntegerArgumentType.integer()).executes(c -> {
                                            InstruccionesMod.serverConfig.offsetX_LogoI = IntegerArgumentType.getInteger(c, "x"); InstruccionesMod.serverConfig.offsetY_LogoI = IntegerArgumentType.getInteger(c, "y");
                                            InstruccionesMod.sincronizarDatosSilencioso(); return 1;
                                        }))))
                                .then(Commands.literal("logo_secundario")
                                        .then(Commands.argument("x", IntegerArgumentType.integer()).then(Commands.argument("y", IntegerArgumentType.integer()).executes(c -> {
                                            InstruccionesMod.serverConfig.offsetX_LogoD = IntegerArgumentType.getInteger(c, "x"); InstruccionesMod.serverConfig.offsetY_LogoD = IntegerArgumentType.getInteger(c, "y");
                                            InstruccionesMod.sincronizarDatosSilencioso(); return 1;
                                        }))))
                        )
                        .then(Commands.literal("escala")
                                .then(Commands.literal("titulo").then(Commands.argument("valor", FloatArgumentType.floatArg(0.1f, 10.0f)).executes(c -> { InstruccionesMod.serverConfig.escalaTitulo = FloatArgumentType.getFloat(c, "valor"); InstruccionesMod.sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("subtitulo").then(Commands.argument("valor", FloatArgumentType.floatArg(0.1f, 10.0f)).executes(c -> { InstruccionesMod.serverConfig.escalaSubtitulo = FloatArgumentType.getFloat(c, "valor"); InstruccionesMod.sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("instrucciones").then(Commands.argument("valor", FloatArgumentType.floatArg(0.1f, 10.0f)).executes(c -> { InstruccionesMod.serverConfig.escalaInstrucciones = FloatArgumentType.getFloat(c, "valor"); InstruccionesMod.sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("controles").then(Commands.argument("valor", FloatArgumentType.floatArg(0.1f, 10.0f)).executes(c -> { InstruccionesMod.serverConfig.escalaControles = FloatArgumentType.getFloat(c, "valor"); InstruccionesMod.sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("logo_principal").then(Commands.argument("valor", FloatArgumentType.floatArg(0.1f, 10.0f)).executes(c -> { InstruccionesMod.serverConfig.escalaLogoI = FloatArgumentType.getFloat(c, "valor"); InstruccionesMod.sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("logo_secundario").then(Commands.argument("valor", FloatArgumentType.floatArg(0.1f, 10.0f)).executes(c -> { InstruccionesMod.serverConfig.escalaLogoD = FloatArgumentType.getFloat(c, "valor"); InstruccionesMod.sincronizarDatosSilencioso(); return 1; })))
                        )
                        .then(Commands.literal("pantalla")
                                .then(Commands.literal("grosor_barras").then(Commands.argument("porcentaje", FloatArgumentType.floatArg(0.0f, 0.45f)).executes(c -> { InstruccionesMod.serverConfig.grosorBarras = FloatArgumentType.getFloat(c, "porcentaje"); InstruccionesMod.sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("mostrar_reloj").then(Commands.argument("estado", BoolArgumentType.bool()).executes(c -> { InstruccionesMod.serverConfig.mostrarReloj = BoolArgumentType.getBool(c, "estado"); InstruccionesMod.sincronizarDatosSilencioso(); return 1; })))
                                .then(Commands.literal("mostrar_lineas").then(Commands.argument("estado", BoolArgumentType.bool()).executes(c -> { InstruccionesMod.serverConfig.mostrarLineas = BoolArgumentType.getBool(c, "estado"); InstruccionesMod.sincronizarDatosSilencioso(); return 1; })))
                        )
                )

                .then(Commands.literal("controles")
                        .then(Commands.literal("agregar").then(Commands.argument("texto", StringArgumentType.greedyString()).executes(c -> {
                            InstruccionesMod.serverConfig.controles.add(formatearTexto(StringArgumentType.getString(c, "texto")));
                            InstruccionesMod.sincronizarDatosSilencioso(); c.getSource().sendSuccess(() -> Component.literal("§aControl agregado."), true); return 1;
                        })))
                        .then(Commands.literal("modificar").then(Commands.argument("indice", IntegerArgumentType.integer(1)).then(Commands.argument("texto", StringArgumentType.greedyString()).executes(c -> {
                            int index = IntegerArgumentType.getInteger(c, "indice") - 1;
                            if (index >= 0 && index < InstruccionesMod.serverConfig.controles.size()) {
                                InstruccionesMod.serverConfig.controles.set(index, formatearTexto(StringArgumentType.getString(c, "texto")));
                                InstruccionesMod.sincronizarDatosSilencioso(); c.getSource().sendSuccess(() -> Component.literal("§aControl " + (index+1) + " modificado."), true);
                            } else { c.getSource().sendFailure(Component.literal("§cÍndice fuera de rango.")); }
                            return 1;
                        }))))
                        .then(Commands.literal("eliminar").then(Commands.argument("indice", IntegerArgumentType.integer(1)).executes(c -> {
                            int index = IntegerArgumentType.getInteger(c, "indice") - 1;
                            if (index >= 0 && index < InstruccionesMod.serverConfig.controles.size()) {
                                InstruccionesMod.serverConfig.controles.remove(index);
                                InstruccionesMod.sincronizarDatosSilencioso(); c.getSource().sendSuccess(() -> Component.literal("§aControl " + (index+1) + " eliminado."), true);
                            } else { c.getSource().sendFailure(Component.literal("§cÍndice fuera de rango.")); }
                            return 1;
                        })))
                        .then(Commands.literal("limpiar").executes(c -> {
                            InstruccionesMod.serverConfig.controles.clear(); InstruccionesMod.sincronizarDatosSilencioso(); c.getSource().sendSuccess(() -> Component.literal("§aLista de controles limpiada."), true); return 1;
                        }))
                )
                .then(Commands.literal("logos")
                        .then(Commands.literal("principal").then(Commands.argument("ruta", StringArgumentType.string()).executes(c -> { InstruccionesMod.serverConfig.logoI = StringArgumentType.getString(c, "ruta"); InstruccionesMod.sincronizarDatosSilencioso(); return 1; })))
                        .then(Commands.literal("secundario").then(Commands.argument("ruta", StringArgumentType.string()).executes(c -> { InstruccionesMod.serverConfig.logoD = StringArgumentType.getString(c, "ruta"); InstruccionesMod.sincronizarDatosSilencioso(); return 1; })))
                        .then(Commands.literal("limpiar").executes(c -> { InstruccionesMod.serverConfig.logoI = null; InstruccionesMod.serverConfig.logoD = null; InstruccionesMod.sincronizarDatosSilencioso(); return 1; }))
                )
        );
    }

    private static int ejecutarMostrar(CommandSourceStack source, Collection<ServerPlayer> targets, String tiempoStr) {
        long timeMs = parseTime(tiempoStr);
        String msgEx = timeMs > 0 ? " por " + tiempoStr : " permanentemente";

        if (targets == null) {
            InstruccionesMod.serverTimerEndTime = timeMs > 0 ? System.currentTimeMillis() + timeMs : 0;
            InstruccionesMod.enviarActualizacionGlobalYMostrar(true, timeMs);
            source.sendSuccess(() -> Component.literal("§aPantalla GLOBAL Abierta" + msgEx), true);
        } else {
            SyncPayload showPacket = new SyncPayload(true, timeMs, ConfigManager.toJson(InstruccionesMod.serverConfig), true);
            for(ServerPlayer p : targets) ServerPlayNetworking.send(p, showPacket);
            source.sendSuccess(() -> Component.literal("§aPantalla mostrada a " + targets.size() + " jugador(es)" + msgEx), true);
        }
        return 1;
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
                colorHex = ColorDictionary.COLOR_MAP.getOrDefault(colorName.toLowerCase(), 0xFFFFFF) & 0x00FFFFFF;
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
}