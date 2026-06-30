package com.calcinhaminimalista;

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
    private static long ultimoCroac = 0;
    private static long intervaloAtual = 0;
    private static final Random r = new Random();
    private static KeyMapping toggleKey;
    private static final KeyMapping.Category CATEGORIA_SAPO = KeyMapping.Category.register(Identifier.parse("calcinhaminimalista:sapo"));

    public static void registrar() {
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "Alternar Sapo", 
            InputConstants.Type.KEYSYM, 
            GLFW.GLFW_KEY_V, 
            CATEGORIA_SAPO
        ));

        ClientTickEvents.END_CLIENT_TICK.register(Sapo::verificarLirio);
    }

    private static void verificarLirio(Minecraft client) {
        if (toggleKey != null) {
            while (toggleKey.consumeClick()) {
                Config.ativo = !Config.ativo;
                Config.salvar();
                if (client.player != null) {
                    client.player.sendSystemMessage(Component.literal("Sapo " + (Config.ativo ? "§aAtivado" : "§cDesativado")));
                }
            }
        }

        if (!Config.ativo || client.screen != null || client.player == null) {
            return;
        }

        long windowHandle = client.getWindow().handle();
        boolean espaco = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;

        if (espaco && client.player.onGround()) {
            long tempoAtual = System.currentTimeMillis();
            
            if (tempoAtual - ultimoCroac >= intervaloAtual) {
                // Ação solicitada ofuscada
                client.player.jumpFromGround();
                
                // Calcula próximo intervalo baseado na frequência dos "croacs" (CPS)
                int cps = Config.minCroac;
                if (Config.maxCroac > Config.minCroac) {
                    cps += r.nextInt(Config.maxCroac - Config.minCroac + 1);
                } else if (cps <= 0) {
                    cps = 1;
                }
                
                intervaloAtual = 1000L / cps;
                ultimoCroac = tempoAtual;
            }
        }
    }
}
