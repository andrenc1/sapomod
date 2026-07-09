package com.sapo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.MouseButtonEvent;

public class AlertHudEditorScreen extends Screen {

    private boolean isDraggingAlert = false;
    private boolean isDraggingAliveOrDead = false;
    private boolean isDraggingDps = false;
    private double dragOffsetX, dragOffsetY;

    public AlertHudEditorScreen(Component title) {
        super(title);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        
        graphics.fill(0, 0, this.width, this.height, 0x88000000);

        graphics.text(this.font, "Drag the texts to move them. Use + and - to change the size of the text under the mouse.", 10, 10, 0xFFFFFFFF, true);
        graphics.text(this.font, "Press ESC to save and exit. Texts will automatically snap to the center when dragged near it.", 10, 25, 0xFFAAAAAA, true);

        // Draw a center line if dragging
        if (isDraggingAlert || isDraggingAliveOrDead || isDraggingDps) {
            graphics.fill(this.width / 2, 0, this.width / 2 + 1, this.height, 0x44FFFFFF);
        }

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

        // Render DPS
        if (Config.dpsHudEnabled) {
            String dpsText = "DPS: 125.0"; // Placeholder text for editor
            int dpsWidth = this.font.width(dpsText);
            int dpsHeight = this.font.lineHeight;

            graphics.pose().pushMatrix();
            graphics.pose().translate(Config.dpsHudX, Config.dpsHudY);
            graphics.pose().scale(Config.dpsHudScale, Config.dpsHudScale);
            graphics.text(this.font, dpsText, 0, 0, Config.dpsHudColor | 0xFF000000, true);
            if (isMouseOver(mouseX, mouseY, Config.dpsHudX, Config.dpsHudY, dpsWidth, dpsHeight, Config.dpsHudScale)) {
                graphics.fill(-2, -2, dpsWidth + 2, dpsHeight + 2, 0x44FFFFFF);
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
            } else if (Config.dpsHudEnabled && isMouseOver(mouseX, mouseY, Config.dpsHudX, Config.dpsHudY, this.font.width("DPS: 125.0"), this.font.lineHeight, Config.dpsHudScale)) {
                isDraggingDps = true;
                dragOffsetX = mouseX - Config.dpsHudX;
                dragOffsetY = mouseY - Config.dpsHudY;
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
    
    private int snapX(int proposedX, int textWidth, float scale) {
        int center = this.width / 2;
        int scaledWidth = (int) (textWidth * scale);
        // Snap to center if within 15 pixels
        if (Math.abs(proposedX + scaledWidth / 2 - center) < 15) {
            return center - scaledWidth / 2;
        }
        return proposedX;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mouseX = event.x();
        double mouseY = event.y();

        if (isDraggingAliveOrDead) {
            int proposedX = (int) (mouseX - dragOffsetX);
            Config.aliveOrDeadX = snapX(proposedX, this.font.width("CROUCH!"), Config.aliveOrDeadScale);
            Config.aliveOrDeadY = (int) (mouseY - dragOffsetY);
            return true;
        } else if (isDraggingDps) {
            int proposedX = (int) (mouseX - dragOffsetX);
            Config.dpsHudX = snapX(proposedX, this.font.width("DPS: 125.0"), Config.dpsHudScale);
            Config.dpsHudY = (int) (mouseY - dragOffsetY);
            return true;
        } else if (isDraggingAlert) {
            int proposedX = (int) (mouseX - dragOffsetX);
            Config.alertX = snapX(proposedX, this.font.width(Config.alertText), Config.alertScale);
            Config.alertY = (int) (mouseY - dragOffsetY);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (isDraggingAlert || isDraggingAliveOrDead || isDraggingDps) {
            isDraggingAlert = false;
            isDraggingAliveOrDead = false;
            isDraggingDps = false;
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
        boolean overDps = Config.dpsHudEnabled && isMouseOver(mouseX, mouseY, Config.dpsHudX, Config.dpsHudY, this.font.width("DPS: 125.0"), this.font.lineHeight, Config.dpsHudScale);
        boolean overAlert = isMouseOver(mouseX, mouseY, Config.alertX, Config.alertY, this.font.width(Config.alertText), this.font.lineHeight, Config.alertScale);

        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_EQUAL || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ADD) {
            if (overVM) {
                Config.aliveOrDeadScale += 0.1f;
                if (Config.aliveOrDeadScale > 5.0f) Config.aliveOrDeadScale = 5.0f;
            } else if (overDps) {
                Config.dpsHudScale += 0.1f;
                if (Config.dpsHudScale > 5.0f) Config.dpsHudScale = 5.0f;
            } else if (overAlert || (!overVM && !overDps && !overAlert)) { // fallback to alert
                Config.alertScale += 0.1f;
                if (Config.alertScale > 5.0f) Config.alertScale = 5.0f;
            }
            Config.save();
            return true;
        } else if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_MINUS || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_SUBTRACT) {
            if (overVM) {
                Config.aliveOrDeadScale -= 0.1f;
                if (Config.aliveOrDeadScale < 0.5f) Config.aliveOrDeadScale = 0.5f;
            } else if (overDps) {
                Config.dpsHudScale -= 0.1f;
                if (Config.dpsHudScale < 0.5f) Config.dpsHudScale = 0.5f;
            } else if (overAlert || (!overVM && !overDps && !overAlert)) {
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

