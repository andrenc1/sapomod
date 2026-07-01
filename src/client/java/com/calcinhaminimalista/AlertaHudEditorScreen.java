package com.calcinhaminimalista;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.MouseButtonEvent;

public class AlertaHudEditorScreen extends Screen {

    private boolean isDragging = false;
    private boolean isScaling = false;
    private double dragOffsetX, dragOffsetY;
    private float dragOffsetScale;

    public AlertaHudEditorScreen(Component title) {
        super(title);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        
        graphics.fill(0, 0, this.width, this.height, 0x88000000);

        graphics.text(this.font, "Arraste o texto para mover. Use + e - para alterar o tamanho.", 10, 10, 0xFFFFFFFF, true);
        graphics.text(this.font, "Pressione ESC para salvar e sair.", 10, 25, 0xFFAAAAAA, true);

        int textWidth = this.font.width(Config.textoAlerta);
        int textHeight = this.font.lineHeight;

        graphics.pose().pushMatrix();
        graphics.pose().translate(Config.alertaX, Config.alertaY);
        graphics.pose().scale(Config.alertaEscala, Config.alertaEscala);

        graphics.text(this.font, Config.textoAlerta, 0, 0, Config.alertaCor, true);

        if (isMouseOverText(mouseX, mouseY, textWidth, textHeight)) {
            graphics.fill(-2, -2, textWidth + 2, textHeight + 2, 0x44FFFFFF);
        }

        graphics.pose().popMatrix();
    }

    private boolean isMouseOverText(double mouseX, double mouseY, int textWidth, int textHeight) {
        double scaledWidth = textWidth * Config.alertaEscala;
        double scaledHeight = textHeight * Config.alertaEscala;
        return mouseX >= Config.alertaX && mouseX <= Config.alertaX + scaledWidth &&
               mouseY >= Config.alertaY && mouseY <= Config.alertaY + scaledHeight;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        int textWidth = this.font.width(Config.textoAlerta);
        int textHeight = this.font.lineHeight;
        
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.button();

        if (isMouseOverText(mouseX, mouseY, textWidth, textHeight)) {
            if (button == 0) { // Botão esquerdo
                isDragging = true;
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

        if (isDragging) {
            Config.alertaX = (int) (mouseX - dragOffsetX);
            Config.alertaY = (int) (mouseY - dragOffsetY);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (isDragging) {
            isDragging = false;
            Config.salvar();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_EQUAL || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ADD) {
            Config.alertaEscala += 0.1f;
            if (Config.alertaEscala > 5.0f) Config.alertaEscala = 5.0f;
            Config.salvar();
            return true;
        } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_MINUS || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_SUBTRACT) {
            Config.alertaEscala -= 0.1f;
            if (Config.alertaEscala < 0.5f) Config.alertaEscala = 0.5f;
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
