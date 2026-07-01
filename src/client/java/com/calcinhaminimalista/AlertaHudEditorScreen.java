package com.calcinhaminimalista;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.MouseButtonEvent;

public class AlertaHudEditorScreen extends Screen {

    private boolean isDraggingAlerta = false;
    private boolean isDraggingVivoMorto = false;
    private double dragOffsetX, dragOffsetY;

    public AlertaHudEditorScreen(Component title) {
        super(title);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        
        graphics.fill(0, 0, this.width, this.height, 0x88000000);

        graphics.text(this.font, "Arraste os textos para mover. Use + e - para alterar o tamanho de quem estiver com o mouse em cima.", 10, 10, 0xFFFFFFFF, true);
        graphics.text(this.font, "Pressione ESC para salvar e sair.", 10, 25, 0xFFAAAAAA, true);

        // Render Alerta
        int alertaWidth = this.font.width(Config.textoAlerta);
        int alertaHeight = this.font.lineHeight;

        graphics.pose().pushMatrix();
        graphics.pose().translate(Config.alertaX, Config.alertaY);
        graphics.pose().scale(Config.alertaEscala, Config.alertaEscala);
        graphics.text(this.font, Config.textoAlerta, 0, 0, Config.alertaCor | 0xFF000000, true);
        if (isMouseOver(mouseX, mouseY, Config.alertaX, Config.alertaY, alertaWidth, alertaHeight, Config.alertaEscala)) {
            graphics.fill(-2, -2, alertaWidth + 2, alertaHeight + 2, 0x44FFFFFF);
        }
        graphics.pose().popMatrix();

        // Render Vivo ou Morto (Exemplo: AGACHE!)
        if (Config.modoVivoOuMorto) {
            String textoVm = "AGACHE!";
            int vmWidth = this.font.width(textoVm);
            int vmHeight = this.font.lineHeight;

            graphics.pose().pushMatrix();
            graphics.pose().translate(Config.vivoMortoX, Config.vivoMortoY);
            graphics.pose().scale(Config.vivoMortoEscala, Config.vivoMortoEscala);
            graphics.text(this.font, textoVm, 0, 0, 0xFF55FF55, true); // Verde como exemplo
            if (isMouseOver(mouseX, mouseY, Config.vivoMortoX, Config.vivoMortoY, vmWidth, vmHeight, Config.vivoMortoEscala)) {
                graphics.fill(-2, -2, vmWidth + 2, vmHeight + 2, 0x44FFFFFF);
            }
            graphics.pose().popMatrix();
        }
    }

    private boolean isMouseOver(double mouseX, double mouseY, int x, int y, int width, int height, float scale) {
        double scaledWidth = width * scale;
        double scaledHeight = height * scale;
        return mouseX >= x && mouseX <= x + scaledWidth &&
               mouseY >= y && mouseY <= y + scaledHeight;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        if (button == 0) { // Botão esquerdo
            if (Config.modoVivoOuMorto && isMouseOver(mouseX, mouseY, Config.vivoMortoX, Config.vivoMortoY, this.font.width("AGACHE!"), this.font.lineHeight, Config.vivoMortoEscala)) {
                isDraggingVivoMorto = true;
                dragOffsetX = mouseX - Config.vivoMortoX;
                dragOffsetY = mouseY - Config.vivoMortoY;
                return true;
            } else if (isMouseOver(mouseX, mouseY, Config.alertaX, Config.alertaY, this.font.width(Config.textoAlerta), this.font.lineHeight, Config.alertaEscala)) {
                isDraggingAlerta = true;
                dragOffsetX = mouseX - Config.alertaX;
                dragOffsetY = mouseY - Config.alertaY;
                return true;
            }
        }
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mouseX = event.x();
        double mouseY = event.y();

        if (isDraggingVivoMorto) {
            Config.vivoMortoX = (int) (mouseX - dragOffsetX);
            Config.vivoMortoY = (int) (mouseY - dragOffsetY);
            return true;
        } else if (isDraggingAlerta) {
            Config.alertaX = (int) (mouseX - dragOffsetX);
            Config.alertaY = (int) (mouseY - dragOffsetY);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (isDraggingAlerta || isDraggingVivoMorto) {
            isDraggingAlerta = false;
            isDraggingVivoMorto = false;
            Config.salvar();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        int keyCode = event.key();
        
        // Pega posicao atual do mouse
        double mouseX = this.minecraft.mouseHandler.xpos() * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
        double mouseY = this.minecraft.mouseHandler.ypos() * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();

        boolean overVM = Config.modoVivoOuMorto && isMouseOver(mouseX, mouseY, Config.vivoMortoX, Config.vivoMortoY, this.font.width("AGACHE!"), this.font.lineHeight, Config.vivoMortoEscala);
        boolean overAlerta = isMouseOver(mouseX, mouseY, Config.alertaX, Config.alertaY, this.font.width(Config.textoAlerta), this.font.lineHeight, Config.alertaEscala);

        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_EQUAL || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ADD) {
            if (overVM) {
                Config.vivoMortoEscala += 0.1f;
                if (Config.vivoMortoEscala > 5.0f) Config.vivoMortoEscala = 5.0f;
            } else if (overAlerta || (!overVM && !overAlerta)) { // fallback pro alerta
                Config.alertaEscala += 0.1f;
                if (Config.alertaEscala > 5.0f) Config.alertaEscala = 5.0f;
            }
            Config.salvar();
            return true;
        } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_MINUS || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_SUBTRACT) {
            if (overVM) {
                Config.vivoMortoEscala -= 0.1f;
                if (Config.vivoMortoEscala < 0.5f) Config.vivoMortoEscala = 0.5f;
            } else if (overAlerta || (!overVM && !overAlerta)) {
                Config.alertaEscala -= 0.1f;
                if (Config.alertaEscala < 0.5f) Config.alertaEscala = 0.5f;
            }
            Config.salvar();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
