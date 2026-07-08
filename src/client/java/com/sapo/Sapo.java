package com.sapo;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;

import java.util.Random;

public class Sapo {
    private static long lastCroak = 0;
    private static long currentInterval = 0;
    private static final Random r = new Random();
    private static KeyMapping toggleKey;
    private static final KeyMapping.Category CATEGORY_SAPO = KeyMapping.Category.register(Identifier.parse("sapo:sapo"));

    public static void registrar() {
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "Toggle Sapo", 
            InputConstants.Type.KEYSYM, 
            GLFW.GLFW_KEY_V, 
            CATEGORY_SAPO
        ));

        ClientTickEvents.END_CLIENT_TICK.register(Sapo::checkLilypad);
    }

    private static void checkLilypad(Minecraft client) {
        if (toggleKey != null) {
            while (toggleKey.consumeClick()) {
                Config.active = !Config.active;
                Config.save();
                if (client.player != null) {
                    client.player.sendSystemMessage(Component.literal("Sapo " + (Config.active ? "§aEnabled" : "§cDisabled")));
                }
            }
        }

        if (!Config.active || client.screen != null || client.player == null) {
            return;
        }

        long windowHandle = client.getWindow().handle();
        boolean space = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;

        if (space && client.player.onGround()) {
            long currentTime = System.currentTimeMillis();
            
            if (currentTime - lastCroak >= currentInterval) {
                // Requested action obfuscated
                client.player.jumpFromGround();
                
                // Calculate next interval based on croaks per second (CPS)
                int cps = Config.minCroaks;
                if (Config.maxCroaks > Config.minCroaks) {
                    cps += r.nextInt(Config.maxCroaks - Config.minCroaks + 1);
                } else if (cps <= 0) {
                    cps = 1;
                }
                
                currentInterval = 1000L / cps;
                lastCroak = currentTime;
            }
        }
    }
}

