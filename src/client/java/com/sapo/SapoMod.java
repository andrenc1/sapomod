package com.sapo;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.FloatControl;
import java.io.File;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.Display;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Interaction;
import java.util.List;

public class SapoMod implements ClientModInitializer {
    public static int alertTimeRemaining = 0;
    public static String aliveOrDeadMessage = "";
    public static int aliveOrDeadColor = 0;

    @Override
    public void onInitializeClient() {

        Config.load();
        Sapo.registrar();

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            handleChatMessage(message.getString());
        });

        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            handleChatMessage(message.getString());
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (alertTimeRemaining > 0) {
                alertTimeRemaining--;
            }

            // DPS tracking logic is now in SapoDPS.java
            SapoDPS.onTick(client);

            // Render particles every 10 ticks (half a second) to avoid crashing the GPU
            if (client.level != null && client.player != null && !SapoPuzzle.activeSolution.isEmpty()) {
                if (client.player.tickCount % 10 == 0) { 
                    for (BlockPos pos : SapoPuzzle.activeSolution) {
                        client.level.addParticle(ParticleTypes.HAPPY_VILLAGER, 
                            pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 
                            0, 0, 0);
                    }
                }
            }
        });

        HudElementRegistry.addLast(Identifier.parse("sapo:alert"), (graphics, tracker) -> {
            if (alertTimeRemaining > 0) {
                String text = Config.aliveOrDeadMode && aliveOrDeadColor != 0 ? aliveOrDeadMessage : Config.alertText;
                String formattedText = "§l" + text;
                int renderColor = (Config.aliveOrDeadMode && aliveOrDeadColor != 0) ? aliveOrDeadColor : Config.alertColor;
                renderColor |= 0xFF000000;
                
                int posX = (Config.aliveOrDeadMode && aliveOrDeadColor != 0) ? Config.aliveOrDeadX : Config.alertX;
                int posY = (Config.aliveOrDeadMode && aliveOrDeadColor != 0) ? Config.aliveOrDeadY : Config.alertY;
                float scale = (Config.aliveOrDeadMode && aliveOrDeadColor != 0) ? Config.aliveOrDeadScale : Config.alertScale;

                graphics.pose().pushMatrix();
                graphics.pose().translate(posX, posY);
                graphics.pose().scale(scale, scale);
                graphics.text(Minecraft.getInstance().font, formattedText, 0, 0, renderColor, true);
                graphics.pose().popMatrix();
            }

            if (Config.dpsHudEnabled && Config.active) {
                double dps = SapoDPS.getCurrentDPS();
                String dpsText = "DPS: " + String.format("%.1f", dps);
                int renderColor = Config.dpsHudColor | 0xFF000000;
                
                graphics.pose().pushMatrix();
                graphics.pose().translate(Config.dpsHudX, Config.dpsHudY);
                graphics.pose().scale(Config.dpsHudScale, Config.dpsHudScale);
                graphics.text(Minecraft.getInstance().font, dpsText, 0, 0, renderColor, true);
                graphics.pose().popMatrix();
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("sapo")
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("debug")
                    .executes(context -> {
                        Config.devMode = !Config.devMode;
                        Config.save();
                        String state = Config.devMode ? "§2ON" : "§cOFF";
                        context.getSource().getPlayer().sendSystemMessage(
                                Component.literal("§a[Sapo] Dev Mode: " + state)
                        );
                        return 1;
                    })
                )

                .executes(context -> {
                    Minecraft.getInstance().execute(() -> {
                        Minecraft.getInstance().setScreen(new SapoModMenu().getModConfigScreenFactory().create(Minecraft.getInstance().screen));
                    });
                    return 1;
                })
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("help")
                    .executes(context -> {
                        context.getSource().getPlayer().sendSystemMessage(Component.literal(
                            "§a[Sapo] Commands:\n" +
                            "§e/sapo §7- Opens Mod Menu\n" +
                            "§e/sapo help §7- Shows this help\n" +
                            "§e/sapo editarHUD §7- Moves the on-screen texts\n" +
                            "§e/sapo testar §7- Tests the alerts\n" +
                            "§e/sapo resolver §7- Solves the Lights Up puzzle\n" +
                            "§e/sapo limpar §7- Clears particles\n" +
                            "§e/sapo debug §7- Toggles dev logs"
                        ));
                        return 1;
                    })
                )
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("testar")
                    .executes(context -> {
                        alertTimeRemaining = Config.alertTime;
                        aliveOrDeadMessage = Config.alertText;
                        context.getSource().getPlayer().sendSystemMessage(Component.literal("§a[Sapo] Alert test activated!"));
                        return 1;
                    })
                )

                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("editarHUD")
                    .executes(context -> {
                        Minecraft.getInstance().execute(() -> {
                            Minecraft.getInstance().setScreen(new AlertHudEditorScreen(Component.literal("HUD Editor")));
                        });
                        return 1;
                    })
                )

                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("resolver")
                    .executes(context -> {
                        Minecraft client = Minecraft.getInstance();
                        SapoPuzzle.escanearEResolver(client);
                        return 1;
                    })
                )
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("limpar")
                    .executes(context -> {
                        SapoPuzzle.activeSolution.clear();
                        context.getSource().getPlayer().sendSystemMessage(Component.literal("§a[Sapo] Particles cleared."));
                        return 1;
                    })
                )
            );
        });
    }

    private void handleChatMessage(String chatText) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            String name = client.player.getName().getString();
            if (chatText.contains(name + " is attempting to solve the edenic light puzzle!")) {
                SapoPuzzle.escanearEResolver(client);
            }
        }

        // Use pre-parsed arrays instead of doing regex splits every message
        if (Config.parsedSoundTriggers.length > 0) {
            String lowerChat = chatText.toLowerCase();
            for (String trigger : Config.parsedSoundTriggers) {
                if (!trigger.isEmpty() && lowerChat.contains(trigger)) {
                    playExternalSound();
                    break;
                }
            }
        }

        if (Config.parsedTextTriggers.length > 0) {
            for (String trigger : Config.parsedTextTriggers) {
                if (!trigger.isEmpty() && chatText.contains(trigger)) {
                    alertTimeRemaining = Config.alertTime;
                    aliveOrDeadMessage = Config.alertText;
                    aliveOrDeadColor = 0;
                    break;
                }
            }
        }
    }

    public static void playExternalSound() {
        // Run audio loading and playback in a separate thread to avoid freezing the game
        new Thread(() -> {
            try {
                File configFolder = new File(FabricLoader.getInstance().getConfigDir().toFile(), "sapo");
                File soundFile = new File(configFolder, "sapo_alerta.wav");
                if (soundFile.exists()) {
                    AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile);
                    Clip clip = AudioSystem.getClip();
                    clip.open(audioStream);

                    if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                        float normalizedVolume = Math.max(Config.soundVolume, 0.0001f);
                        float db = (float) (Math.log10(normalizedVolume) * 20.0f);
                        gainControl.setValue(db);
                    }

                    // Properly close resources when sound finishes to prevent memory leak
                    clip.addLineListener(event -> {
                        if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                            clip.close();
                            try { audioStream.close(); } catch (Exception ignored) {}
                        }
                    });

                    clip.start();
                }
            } catch (Exception e) {
                // Ignore audio errors in background thread
            }
        }, "SapoAudioThread").start();
    }
}