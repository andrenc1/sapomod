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

        // Replaced ENTITY_LOAD with tick check because entity metadata (Text/Name) 
        // usually arrives *after* the entity is loaded in the world!

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String chatText = message.getString();
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                String name = client.player.getName().getString();
                if (chatText.contains(name + " is attempting to solve the edenic light puzzle!")) {
                    SapoPuzzle.escanearEResolver(client);
                }
            }

            if (Config.soundTriggers != null && !Config.soundTriggers.isEmpty()) {
                // if (Config.devMode) System.out.println("[Sapo Debug] Message received (GAME): " + chatText);
                // if (Config.devMode) System.out.println("[Sapo Debug] Configured triggers: " + Config.soundTriggers);
                for (String trigger : Config.soundTriggers.split(",")) {
                    String cleanT = trigger.trim();
                    if (!cleanT.isEmpty() && chatText.toLowerCase().contains(cleanT.toLowerCase())) {
                        // if (Config.devMode) System.out.println("[Sapo Debug] Trigger activated! Playing external sound for: " + cleanT + " | Volume: " + Config.soundVolume);
                        playExternalSound();
                        break;
                    }
                }
            }

            if (Config.triggerText != null && !Config.triggerText.isEmpty()) {
                for (String trigger : Config.triggerText.split(",")) {
                    String cleanT = trigger.trim();
                    if (!cleanT.isEmpty() && chatText.contains(cleanT)) {
                        alertTimeRemaining = Config.alertTime;
                        aliveOrDeadMessage = Config.alertText;
                        aliveOrDeadColor = 0;
                        break;
                    }
                }
            }
        });

        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            String chatText = message.getString();
            Minecraft client = Minecraft.getInstance();
            if (client.player != null) {
                String name = client.player.getName().getString();
                if (chatText.contains(name + " is attempting to solve the edenic light puzzle!")) {
                    SapoPuzzle.escanearEResolver(client);
                }
            }

            if (Config.soundTriggers != null && !Config.soundTriggers.isEmpty()) {
                // if (Config.devMode) System.out.println("[Sapo Debug] Message received (CHAT): " + chatText);
                // if (Config.devMode) System.out.println("[Sapo Debug] Configured triggers: " + Config.soundTriggers);
                for (String trigger : Config.soundTriggers.split(",")) {
                    String cleanT = trigger.trim();
                    if (!cleanT.isEmpty() && chatText.toLowerCase().contains(cleanT.toLowerCase())) {
                        // if (Config.devMode) System.out.println("[Sapo Debug] Trigger activated! Playing external sound for: " + cleanT + " | Volume: " + Config.soundVolume);
                        playExternalSound();
                        break;
                    }
                }
            }

            if (Config.triggerText != null && !Config.triggerText.isEmpty()) {
                for (String trigger : Config.triggerText.split(",")) {
                    String cleanT = trigger.trim();
                    if (!cleanT.isEmpty() && chatText.contains(cleanT)) {
                        alertTimeRemaining = Config.alertTime;
                        aliveOrDeadMessage = Config.alertText;
                        aliveOrDeadColor = 0;
                        break;
                    }
                }
            }
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
                            // "§e/sapo inspecionar §7- Inspects block\n" +
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
                /* Commented out as requested
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("inspecionar")
                    .executes(context -> {
                        Minecraft client = Minecraft.getInstance();
                        if (client.player == null || client.level == null) return 0;
                        
                        HitResult hit = client.player.pick(50.0D, 0.0F, false);
                        if (hit == null || hit.getType() == HitResult.Type.MISS) {
                            client.player.sendSystemMessage(Component.literal("§c[Sapo] No block detected. Get closer or point to a block."));
                            return 0;
                        }
                        
                        Vec3 hitPos = hit.getLocation();
                        BlockPos blockPos;
                        if (hit instanceof BlockHitResult) {
                            blockPos = ((BlockHitResult) hit).getBlockPos();
                        } else {
                            blockPos = BlockPos.containing(hitPos);
                        }
                        BlockState state = client.level.getBlockState(blockPos);
                        
                        Identifier blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                        System.out.println("==================================");
                        System.out.println("[Sapo Inspect] Block: " + blockId.toString() + " at " + blockPos.toShortString());
                        
                        // Thin needle exactly on the pointed block (ignoring neighbors)
                        AABB box = new AABB(blockPos).inflate(0.1, 1.5, 0.1).move(0, 1, 0); 
                        List<Entity> entities = client.level.getEntities(client.player, box);
                        
                        if (entities.isEmpty()) {
                            System.out.println("[Sapo Inspect] No useful entity in this column.");
                        } else {
                            for (Entity entity : entities) {
                                // Skip invisible junk and effects
                                if (entity instanceof net.minecraft.world.entity.Interaction || 
                                    entity instanceof net.minecraft.world.entity.AreaEffectCloud) {
                                    continue;
                                }

                                if (entity instanceof Display.ItemDisplay itemDisplay) {
                                    net.minecraft.world.item.ItemStack stack = itemDisplay.getItemStack();
                                    String itemDesc = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
                                    
                                    if (itemDesc.contains("air")) continue; // Ignore air

                                    String itemModel = "None";
                                    String colors = "None";
                                    String dataStr = stack.getComponents().toString();
                                    
                                    if (dataStr.contains("minecraft:item_model=>")) {
                                        int modelStart = dataStr.indexOf("minecraft:item_model=>") + 22;
                                        int modelEnd = dataStr.indexOf(",", modelStart);
                                        if (modelEnd == -1) modelEnd = dataStr.indexOf("}", modelStart);
                                        if (modelEnd != -1) itemModel = dataStr.substring(modelStart, modelEnd);
                                    }
                                    
                                    if (dataStr.contains("colors=[")) {
                                        int colorStart = dataStr.indexOf("colors=[") + 8;
                                        int colorEnd = dataStr.indexOf("]", colorStart);
                                        if (colorEnd != -1) colors = dataStr.substring(colorStart, colorEnd);
                                    }

                                    System.out.println("-> Hologram: " + itemDesc + " | Model: " + itemModel + " | Color: " + colors);
                                    
                                } else if (entity instanceof Display.TextDisplay textDisplay) {
                                    if (textDisplay.getText() != null) {
                                        System.out.println("-> Floating Text: " + textDisplay.getText().getString());
                                    }
                                } else if (entity instanceof net.minecraft.world.entity.decoration.ArmorStand stand) {
                                    if (stand.hasCustomName()) {
                                        System.out.println("-> Armor Stand: " + stand.getCustomName().getString());
                                    }
                                }
                            }
                        }
                        System.out.println("==================================");
                        
                        client.player.sendSystemMessage(Component.literal("§e[Sapo] Looked Block: §f" + blockId.toString()));
                        client.player.sendSystemMessage(Component.literal("§e[Sapo] Coordinates: §bX=" + blockPos.getX() + " §aY=" + blockPos.getY() + " §bZ=" + blockPos.getZ()));
                        client.player.sendSystemMessage(Component.literal("§a[Sapo] Clean inspection completed! Check the console."));
                        return 1;
                    })
                )
                */
            );
        });
    }

    public static void playExternalSound() {
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

                clip.start();
            } else {
                // if (Config.devMode) System.out.println("[Sapo Debug] sapo_alerta.wav file not found in config folder: " + soundFile.getAbsolutePath());
            }
        } catch (Exception e) {
            // if (Config.devMode) System.out.println("[Sapo Debug] Error playing sapo_alerta.wav: " + e.getMessage());
        }
    }
}