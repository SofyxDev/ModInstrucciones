package Luxfiro.instruccioens_eufonia;

import java.util.ArrayList;
import java.util.List;

public class SaveData {
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