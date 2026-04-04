package Luxfiro.instrucciones.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import Luxfiro.instrucciones.InstruccionesMod;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ConfigManager {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final File CONFIG_DIR = FabricLoader.getInstance().getConfigDir().resolve(InstruccionesMod.MODID).toFile();

    public static void init() {
        if (!CONFIG_DIR.exists()) CONFIG_DIR.mkdirs();
    }

    public static String toJson(SaveData data) {
        return GSON.toJson(data);
    }

    public static SaveData fromJson(String json) {
        return GSON.fromJson(json, SaveData.class);
    }

    public static void save(String nombre) {
        try (FileWriter writer = new FileWriter(new File(CONFIG_DIR, nombre + ".json"))) {
            GSON.toJson(InstruccionesMod.serverConfig, writer);
        } catch (Exception e) { InstruccionesMod.LOGGER.error("[Eufonia] Error al guardar", e); }
    }

    public static boolean load(String nombre) {
        File f = new File(CONFIG_DIR, nombre + ".json");
        if (!f.exists()) return false;
        try (FileReader reader = new FileReader(f)) {
            InstruccionesMod.serverConfig = GSON.fromJson(reader, SaveData.class);
            if (InstruccionesMod.serverConfig != null) InstruccionesMod.serverConfig.validate();
            else InstruccionesMod.serverConfig = new SaveData();
            InstruccionesMod.sincronizarDatosSilencioso();
            return true;
        } catch (Exception e) {
            InstruccionesMod.LOGGER.error("[Eufonia] Error al cargar", e);
            return false;
        }
    }
}