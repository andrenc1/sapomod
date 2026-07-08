package com.sapo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.MouseButtonEvent;

public class AlertHudEditorScreen extends Screen {

    private boolean isDraggingAlert = false;
    private boolean isDraggingAliveOrDead = false;
    private double dragOffsetX, dragOffsetY;

    public AlertHudEditorScreen(Component title) {
        super(title);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        
        graphics.fill(0, 0, this.width, this.height, 0x88000000);

        graphics.text(this.font, "Drag the texts to move them. Use + and - to change the size of the text under the mouse.", 10, 10, 0xFFFFFFFF, true);
        graphics.text(this.font, "Press ESC to save and exit.", 10, 25, 0xFFAAAAAA, true);

        // Render Alert
        int alertWidth = this.font.width(Config.alertText);
        int alertHeight = this.font.lineHeight;

        graphics.pose().pushMatrix();
        graphics.pose().translate(Config.alertX, Config.alertY);
        graphics.pose().scale(Config.alertScale, Config.alertScale);
        graphics.text(this.font, Config.alertText, 0, 0, Config.alertColor | 0xFF000000, true);
        if (isMouseOver(mouseX, mouseY, Config.alertX, Config.alertY, alertWidth, alertHeight, Config.alertScale)) {
            graphics.fill(-2, -2, alertWidth + 2, alertHeight + 2, 0x44FFFFFF);
        }
        graphics.pose().popMatrix();

        // Render Alive or Dead (Example: CROUCH!)
        if (Config.aliveOrDeadMode) {
            String textVm = "CROUCH!";
            int vmWidth = this.font.width(textVm);
            int vmHeight = this.font.lineHeight;

            graphics.pose().pushMatrix();
            graphics.pose().translate(Config.aliveOrDeadX, Config.aliveOrDeadY);
            graphics.pose().scale(Config.aliveOrDeadScale, Config.aliveOrDeadScale);
            graphics.text(this.font, textVm, 0, 0, 0xFF55FF55, true); // Green as an example
            if (isMouseOver(mouseX, mouseY, Config.aliveOrDeadX, Config.aliveOrDeadY, vmWidth, vmHeight, Config.aliveOrDeadScale)) {
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

        if (button == 0) { // Left button
            if (Config.aliveOrDeadMode && isMouseOver(mouseX, mouseY, Config.aliveOrDeadX, Config.aliveOrDeadY, this.font.width("CROUCH!"), this.font.lineHeight, Config.aliveOrDeadScale)) {
                isDraggingAliveOrDead = true;
                dragOffsetX = mouseX - Config.aliveOrDeadX;
                dragOffsetY = mouseY - Config.aliveOrDeadY;
                return true;
            } else if (isMouseOver(mouseX, mouseY, Config.alertX, Config.alertY, this.font.width(Config.alertText), this.font.lineHeight, Config.alertScale)) {
                isDraggingAlert = true;
                dragOffsetX = mouseX - Config.alertX;
                dragOffsetY = mouseY - Config.alertY;
                return true;
            }
        }
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mouseX = event.x();
        double mouseY = event.y();

        if (isDraggingAliveOrDead) {
            Config.aliveOrDeadX = (int) (mouseX - dragOffsetX);
            Config.aliveOrDeadY = (int) (mouseY - dragOffsetY);
            return true;
        } else if (isDraggingAlert) {
            Config.alertX = (int) (mouseX - dragOffsetX);
            Config.alertY = (int) (mouseY - dragOffsetY);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (isDraggingAlert || isDraggingAliveOrDead) {
            isDraggingAlert = false;
            isDraggingAliveOrDead = false;
            Config.save();
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        int keyCode = event.key();
        
        // Get current mouse position
        double mouseX = this.minecraft.mouseHandler.xpos() * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getScreenWidth();
        double mouseY = this.minecraft.mouseHandler.ypos() * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getScreenHeight();

        boolean overVM = Config.aliveOrDeadMode && isMouseOver(mouseX, mouseY, Config.aliveOrDeadX, Config.aliveOrDeadY, this.font.width("CROUCH!"), this.font.lineHeight, Config.aliveOrDeadScale);
        boolean overAlert = isMouseOver(mouseX, mouseY, Config.alertX, Config.alertY, this.font.width(Config.alertText), this.font.lineHeight, Config.alertScale);

        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_EQUAL || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ADD) {
            if (overVM) {
                Config.aliveOrDeadScale += 0.1f;
                if (Config.aliveOrDeadScale > 5.0f) Config.aliveOrDeadScale = 5.0f;
            } else if (overAlert || (!overVM && !overAlert)) { // fallback to alert
                Config.alertScale += 0.1f;
                if (Config.alertScale > 5.0f) Config.alertScale = 5.0f;
            }
            Config.save();
            return true;
        } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_MINUS || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_SUBTRACT) {
            if (overVM) {
                Config.aliveOrDeadScale -= 0.1f;
                if (Config.aliveOrDeadScale < 0.5f) Config.aliveOrDeadScale = 0.5f;
            } else if (overAlert || (!overVM && !overAlert)) {
                Config.alertScale -= 0.1f;
                if (Config.alertScale < 0.5f) Config.alertScale = 0.5f;
            }
            Config.save();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}

