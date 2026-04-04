package Luxfiro.instrucciones.client.screen;

import Luxfiro.instrucciones.InstruccionesMod;
import Luxfiro.instrucciones.client.InstruccionesClient;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PantallaEvento extends Screen {

    public static int fadePhase = 0;
    public static long fadeStartTime = 0;
    public static final long FADE_DURATION = 2500L;

    public PantallaEvento() { super(Component.literal("Pantalla Evento")); }

    private int aplicarAlpha(int colorOriginal, float alphaMod) {
        int a = (colorOriginal >> 24) & 0xFF;
        a = (int)(a * alphaMod);
        return (a << 24) | (colorOriginal & 0x00FFFFFF);
    }

    private int interpolateColor(int c1, int c2, float fraction, int dominio) {
        if (dominio == 1) fraction = (float) Math.pow(fraction, 6.0);
        else if (dominio == 2) fraction = (float) Math.pow(fraction, 0.16);

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
        // En 1.21.1, renderBackground pide 4 parametros. Lo dejamos vacío para mantener tu UI.
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        if (InstruccionesClient.clientConfig.blockF1) mc.options.hideGui = false;

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
            float sTitulo = InstruccionesClient.clientConfig.escalaTitulo;
            float sSubtitulo = InstruccionesClient.clientConfig.escalaSubtitulo;
            float sInst = InstruccionesClient.clientConfig.escalaInstrucciones;
            float sControles = InstruccionesClient.clientConfig.escalaControles;

            int topUI = (int)(height * InstruccionesClient.clientConfig.grosorBarras);
            int bottomUI = (int)(height * (1.0f - InstruccionesClient.clientConfig.grosorBarras));
            int midUI = (int)(width * 0.50);

            int camTop = topUI + (int)(height * 0.02f);
            int camBottom = bottomUI - (int)(height * 0.02f);
            int camLeft = (int)(width * 0.52); int camRight = (int)(width * 0.98);
            int pad = 30;

            int tituloX = (width / 2) + InstruccionesClient.clientConfig.offsetX_Titulo;
            int tituloY = (topUI / 2 - 15) + InstruccionesClient.clientConfig.offsetY_Titulo;
            int subtituloX = (width / 2) + InstruccionesClient.clientConfig.offsetX_Subtitulo;
            int subtituloY = (topUI / 2 + 5) + InstruccionesClient.clientConfig.offsetY_Subtitulo;

            int controlesCabeceraY = topUI + 15; int controlesListaY = topUI + 32;
            int instCabeceraY = bottomUI + 15; int instTextoY = bottomUI + 32;

            int cFondo1 = aplicarAlpha(InstruccionesClient.clientConfig.colorFondo, 1.0f);
            int cFondo2 = aplicarAlpha(InstruccionesClient.clientConfig.colorFondo2, 1.0f);
            int cLineas = aplicarAlpha(InstruccionesClient.clientConfig.colorLineas, 1.0f);

            RenderSystem.enableBlend(); RenderSystem.defaultBlendFunc(); RenderSystem.disableDepthTest();

            float fracTop = (float)camTop / height;
            float fracBottom = (float)camBottom / height;

            int cMidTop = interpolateColor(cFondo1, cFondo2, fracTop, InstruccionesClient.clientConfig.dominioFondo);
            int cMidBottom = interpolateColor(cFondo1, cFondo2, fracBottom, InstruccionesClient.clientConfig.dominioFondo);

            graphics.fillGradient(0, 0, width, camTop, cFondo1, cMidTop);
            graphics.fillGradient(0, camBottom, width, height, cMidBottom, cFondo2);
            graphics.fillGradient(0, camTop, camLeft, camBottom, cMidTop, cMidBottom);
            graphics.fillGradient(camRight, camTop, width, camBottom, cMidTop, cMidBottom);

            if (InstruccionesClient.clientConfig.mostrarLineas) {
                graphics.fill(0, topUI - 1, width, topUI + 1, cLineas); graphics.fill(0, bottomUI - 1, width, bottomUI + 1, cLineas);
                graphics.fill(midUI - 1, topUI, midUI + 1, bottomUI, cLineas);
                graphics.fill(camLeft - 1, camTop - 1, camRight + 1, camTop, cLineas); graphics.fill(camLeft - 1, camBottom, camRight + 1, camBottom + 1, cLineas);
                graphics.fill(camLeft - 1, camTop, camLeft, camBottom, cLineas); graphics.fill(camRight, camTop, camRight + 1, camBottom, cLineas);
            }

            if (InstruccionesClient.clientConfig.logoI != null) {
                graphics.pose().pushPose();
                graphics.pose().translate(20 + InstruccionesClient.clientConfig.offsetX_LogoI, 20 + InstruccionesClient.clientConfig.offsetY_LogoI, 0);
                graphics.pose().scale(InstruccionesClient.clientConfig.escalaLogoI, InstruccionesClient.clientConfig.escalaLogoI, InstruccionesClient.clientConfig.escalaLogoI);
                graphics.blit(ResourceLocation.fromNamespaceAndPath(InstruccionesMod.MODID, InstruccionesClient.clientConfig.logoI), 0, 0, 0, 0, 64, 64, 64, 64);
                graphics.pose().popPose();
            }

            if (InstruccionesClient.clientConfig.logoD != null) {
                graphics.pose().pushPose();
                graphics.pose().translate((width - 84) + InstruccionesClient.clientConfig.offsetX_LogoD, 20 + InstruccionesClient.clientConfig.offsetY_LogoD, 0);
                graphics.pose().scale(InstruccionesClient.clientConfig.escalaLogoD, InstruccionesClient.clientConfig.escalaLogoD, InstruccionesClient.clientConfig.escalaLogoD);
                graphics.blit(ResourceLocation.fromNamespaceAndPath(InstruccionesMod.MODID, InstruccionesClient.clientConfig.logoD), 0, 0, 0, 0, 64, 64, 64, 64);
                graphics.pose().popPose();
            }

            if (InstruccionesClient.isTimerActive && InstruccionesClient.clientConfig.mostrarReloj) {
                long timeLeft = Math.max(0, InstruccionesClient.clientTimerEndTime - System.currentTimeMillis());
                long ts = timeLeft / 1000;
                String timeStr = String.format("%02d:%02d:%02d", ts / 3600, (ts % 3600) / 60, ts % 60);
                graphics.drawCenteredString(this.font, timeStr, width / 2, 10, InstruccionesClient.clientConfig.colorReloj);
            }

            graphics.pose().pushPose(); graphics.pose().scale(sTitulo, sTitulo, sTitulo);
            String[] lineasTitulo = InstruccionesClient.clientConfig.titulo.split("\n");
            for (int i = 0; i < lineasTitulo.length; i++) graphics.drawCenteredString(this.font, lineasTitulo[i], (int) (tituloX / sTitulo), (int) ((tituloY + (i * 12)) / sTitulo), InstruccionesClient.clientConfig.colorTitulo);
            graphics.pose().popPose();

            graphics.pose().pushPose(); graphics.pose().scale(sSubtitulo, sSubtitulo, sSubtitulo);
            String[] lineasSubtitulo = InstruccionesClient.clientConfig.subtitulo.split("\n");
            for (int i = 0; i < lineasSubtitulo.length; i++) graphics.drawCenteredString(this.font, lineasSubtitulo[i], (int) (subtituloX / sSubtitulo), (int) ((subtituloY + (i * 12)) / sSubtitulo), InstruccionesClient.clientConfig.colorSubtitulo);
            graphics.pose().popPose();

            int colorTextoBase = 0xFFFFFFFF;

            graphics.pose().pushPose(); graphics.pose().scale(1.1f, 1.1f, 1.1f);
            graphics.drawString(this.font, "Controles Especiales", (int)(pad / 1.1f), (int)(controlesCabeceraY / 1.1f), InstruccionesClient.clientConfig.colorControles);
            graphics.pose().popPose();

            graphics.pose().pushPose(); graphics.pose().scale(sControles, sControles, sControles);
            for (int i = 0; i < InstruccionesClient.clientConfig.controles.size(); i++) {
                String[] subLineas = InstruccionesClient.clientConfig.controles.get(i).split("\n");
                for (int j = 0; j < subLineas.length; j++) graphics.drawString(this.font, subLineas[j], (int)(pad / sControles), (int)((controlesListaY + (i * 15) + (j * 10)) / sControles), colorTextoBase);
            }
            graphics.pose().popPose();

            graphics.pose().pushPose(); graphics.pose().scale(1.1f, 1.1f, 1.1f);
            graphics.drawString(this.font, "Instrucciones", (int)(pad / 1.1f), (int)(instCabeceraY / 1.1f), InstruccionesClient.clientConfig.colorInstrucciones);
            graphics.pose().popPose();

            graphics.pose().pushPose(); graphics.pose().scale(sInst, sInst, sInst);
            int wrapWidth = (int)((width - (pad * 2)) / sInst);
            graphics.drawWordWrap(this.font, Component.literal(InstruccionesClient.clientConfig.instrucciones), (int)(pad / sInst), (int)(instTextoY / sInst), wrapWidth, colorTextoBase);
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

    @Override
    public boolean shouldCloseOnEsc() { return false; }
    @Override
    public boolean isPauseScreen() { return false; }
}