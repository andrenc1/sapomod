package com.calcinhaminimalista;

import net.fabricmc.api.ClientModInitializer;
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

public class CalcinhaMinimalista implements ClientModInitializer {
    public static int alertaTempoRestante = 0;
    public static String mensagemVivoMorto = "";
    public static int corVivoMorto = 0;

    @Override
    public void onInitializeClient() {
        Config.carregar();
        Sapo.registrar();

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            String textoChat = message.getString();
            if (Config.textoGatilho != null && !Config.textoGatilho.isEmpty()) {
                for (String gatilho : Config.textoGatilho.split(",")) {
                    String gLimpo = gatilho.trim();
                    if (!gLimpo.isEmpty() && textoChat.contains(gLimpo)) {
                        alertaTempoRestante = Config.alertaTempo;
                        mensagemVivoMorto = Config.textoAlerta;
                        corVivoMorto = 0;
                        break;
                    }
                }
            }
        });

        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            String textoChat = message.getString();
            if (Config.textoGatilho != null && !Config.textoGatilho.isEmpty()) {
                for (String gatilho : Config.textoGatilho.split(",")) {
                    String gLimpo = gatilho.trim();
                    if (!gLimpo.isEmpty() && textoChat.contains(gLimpo)) {
                        alertaTempoRestante = Config.alertaTempo;
                        mensagemVivoMorto = Config.textoAlerta;
                        corVivoMorto = 0;
                        break;
                    }
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (alertaTempoRestante > 0) {
                alertaTempoRestante--;
            }

            // Renderiza as partículas nos locais onde a luz deve ser colocada
            if (client.level != null && !SapoPuzzle.solucaoAtiva.isEmpty()) {
                for (BlockPos pos : SapoPuzzle.solucaoAtiva) {
                    client.level.addParticle(ParticleTypes.HAPPY_VILLAGER, 
                        pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 
                        0, 0, 0);
                }
            }
        });

        HudElementRegistry.addLast(Identifier.parse("calcinhaminimalista:alerta"), (graphics, tracker) -> {
            if (alertaTempoRestante > 0) {
                String texto = Config.modoVivoOuMorto && corVivoMorto != 0 ? mensagemVivoMorto : Config.textoAlerta;
                String textFormatado = "§l" + texto;
                int renderCor = (Config.modoVivoOuMorto && corVivoMorto != 0) ? corVivoMorto : Config.alertaCor;
                renderCor |= 0xFF000000;
                
                int posX = (Config.modoVivoOuMorto && corVivoMorto != 0) ? Config.vivoMortoX : Config.alertaX;
                int posY = (Config.modoVivoOuMorto && corVivoMorto != 0) ? Config.vivoMortoY : Config.alertaY;
                float escala = (Config.modoVivoOuMorto && corVivoMorto != 0) ? Config.vivoMortoEscala : Config.alertaEscala;

                graphics.pose().pushMatrix();
                graphics.pose().translate(posX, posY);
                graphics.pose().scale(escala, escala);
                graphics.text(Minecraft.getInstance().font, textFormatado, 0, 0, renderCor, true);
                graphics.pose().popMatrix();
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("sapo")
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("debug")
                    .executes(context -> {
                        Config.modoDev = !Config.modoDev;
                        Config.salvar();
                        String estado = Config.modoDev ? "§2LIGADO" : "§cDESLIGADO";
                        context.getSource().getPlayer().sendSystemMessage(
                                Component.literal("§a[Sapo] Modo Dev de Sons: " + estado)
                        );
                        return 1;
                    })
                )
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("gatilho")
                    .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("texto", StringArgumentType.greedyString())
                        .executes(context -> {
                            String texto = StringArgumentType.getString(context, "texto");
                            Config.textoGatilho = texto;
                            Config.salvar();
                            context.getSource().getPlayer().sendSystemMessage(Component.literal("§a[Sapo] Gatilho definido para: " + texto));
                            return 1;
                        })
                    )
                )
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("alerta")
                    .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("texto", StringArgumentType.greedyString())
                        .executes(context -> {
                            String texto = StringArgumentType.getString(context, "texto");
                            Config.textoAlerta = texto;
                            Config.salvar();
                            context.getSource().getPlayer().sendSystemMessage(Component.literal("§a[Sapo] Alerta definido para: " + texto));
                            return 1;
                        })
                    )
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
                            "§a[Sapo] Comandos:\n" +
                            "§e/sapo §7- Abre o Mod Menu\n" +
                            "§e/sapo help §7- Mostra essa ajuda\n" +
                            "§e/sapo editarHUD §7- Move os textos na tela\n" +
                            "§e/sapo testar §7- Testa os alertas\n" +
                            "§e/sapo simular <texto> §7- Simula mensagem\n" +
                            "§e/sapo alerta <texto> §7- Muda texto do alerta\n" +
                            "§e/sapo cor <hex> §7- Muda a cor do alerta\n" +
                            "§e/sapo gatilho <texto> §7- Muda o gatilho\n" +
                            "§e/sapo tempo <segs> §7- Muda o tempo\n" +
                            "§e/sapo debug §7- Alterna logs de som"
                        ));
                        return 1;
                    })
                )
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("testar")
                    .executes(context -> {
                        alertaTempoRestante = Config.alertaTempo;
                        mensagemVivoMorto = Config.textoAlerta;
                        context.getSource().getPlayer().sendSystemMessage(Component.literal("§a[Sapo] Teste do alerta ativado!"));
                        return 1;
                    })
                )
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("simular")
                    .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("texto", StringArgumentType.greedyString())
                        .executes(context -> {
                            String texto = StringArgumentType.getString(context, "texto");
                            boolean ativou = false;
                            if (Config.textoGatilho != null && !Config.textoGatilho.isEmpty()) {
                                for (String gatilho : Config.textoGatilho.split(",")) {
                                    String gLimpo = gatilho.trim();
                                    if (!gLimpo.isEmpty() && texto.contains(gLimpo)) {
                                        alertaTempoRestante = Config.alertaTempo;
                                        mensagemVivoMorto = Config.textoAlerta;
                                        corVivoMorto = 0;
                                        ativou = true;
                                        break;
                                    }
                                }
                            }
                            if (ativou) {
                                context.getSource().getPlayer().sendSystemMessage(Component.literal("§a[Sapo] A simulação ativou o gatilho!"));
                            } else {
                                context.getSource().getPlayer().sendSystemMessage(Component.literal("§c[Sapo] A simulação não ativou o gatilho. Verifique o texto configurado."));
                            }
                            return 1;
                        })
                    )
                )
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("editarHUD")
                    .executes(context -> {
                        Minecraft.getInstance().execute(() -> {
                            Minecraft.getInstance().setScreen(new AlertaHudEditorScreen(Component.literal("Editor de HUD")));
                        });
                        return 1;
                    })
                )
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("cor")
                    .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("hex", StringArgumentType.string())
                        .executes(context -> {
                            String hexStr = StringArgumentType.getString(context, "hex");
                            try {
                                if (hexStr.startsWith("#")) hexStr = hexStr.substring(1);
                                if (hexStr.length() == 6) hexStr = "FF" + hexStr; // Add fully opaque alpha
                                Config.alertaCor = (int) Long.parseLong(hexStr, 16);
                                Config.salvar();
                                context.getSource().getPlayer().sendSystemMessage(Component.literal("§a[Sapo] Cor atualizada!"));
                            } catch (Exception e) {
                                context.getSource().getPlayer().sendSystemMessage(Component.literal("§c[Sapo] Cor inválida. Use um formato hexadecimal (ex: FF0000)"));
                            }
                            return 1;
                        })
                    )
                )
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("tempo")
                    .then(RequiredArgumentBuilder.<FabricClientCommandSource, String>argument("segundos", StringArgumentType.string())
                        .executes(context -> {
                            String segStr = StringArgumentType.getString(context, "segundos");
                            try {
                                int segundos = Integer.parseInt(segStr);
                                if (segundos < 1) segundos = 1;
                                Config.alertaTempo = segundos * 20; // 20 ticks por segundo
                                Config.salvar();
                                context.getSource().getPlayer().sendSystemMessage(Component.literal("§a[Sapo] Tempo configurado para " + segundos + " segundos!"));
                            } catch (Exception e) {
                                context.getSource().getPlayer().sendSystemMessage(Component.literal("§c[Sapo] Tempo inválido. Insira o número de segundos (ex: 5)."));
                            }
                            return 1;
                        })
                    )
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
                        SapoPuzzle.solucaoAtiva.clear();
                        context.getSource().getPlayer().sendSystemMessage(Component.literal("§a[Sapo] Partículas apagadas."));
                        return 1;
                    })
                )
                .then(LiteralArgumentBuilder.<FabricClientCommandSource>literal("inspecionar")
                    .executes(context -> {
                        Minecraft client = Minecraft.getInstance();
                        if (client.player == null || client.level == null || client.hitResult == null) return 0;
                        
                        Vec3 hitPos = client.hitResult.getLocation();
                        BlockPos blockPos = BlockPos.containing(hitPos);
                        BlockState state = client.level.getBlockState(blockPos);
                        
                        Identifier blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
                        System.out.println("[Sapo Inspecionar] Bloco: " + blockId.toString());
                        
                        AABB box = new AABB(hitPos.x - 1.0, hitPos.y - 1.0, hitPos.z - 1.0, hitPos.x + 1.0, hitPos.y + 1.0, hitPos.z + 1.0);
                        List<Entity> entities = client.level.getEntities(client.player, box);
                        
                        if (entities.isEmpty()) {
                            System.out.println("[Sapo Inspecionar] Nenhuma entidade próxima.");
                        } else {
                            for (Entity entity : entities) {
                                Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                                String extra = "";
                                
                                if (entity instanceof Display.TextDisplay) {
                                    Display.TextDisplay textDisplay = (Display.TextDisplay) entity;
                                    if (textDisplay.getText() != null) {
                                        extra = " - Texto: " + textDisplay.getText().getString();
                                    }
                                } else if (entity instanceof ArmorStand) {
                                    ArmorStand stand = (ArmorStand) entity;
                                    if (stand.hasCustomName()) {
                                        extra = " - Nome: " + stand.getCustomName().getString();
                                    }
                                } else if (entity instanceof ItemFrame) {
                                    ItemFrame frame = (ItemFrame) entity;
                                    if (!frame.getItem().isEmpty()) {
                                        extra = " - Item: " + BuiltInRegistries.ITEM.getKey(frame.getItem().getItem()).toString();
                                    }
                                } else if (entity instanceof Display.ItemDisplay) {
                                    Display.ItemDisplay itemDisplay = (Display.ItemDisplay) entity;
                                    net.minecraft.world.item.ItemStack stack = itemDisplay.getItemStack();
                                    String dataStr = !stack.getComponents().isEmpty() ? stack.getComponents().toString() : "Sem Componentes";
                                    extra = " - ItemDisplay: " + stack.getItem().getDescriptionId() + " | Dados: " + dataStr;
                                } else if (entity instanceof Interaction) {
                                    extra = " (Caixa de Clique - Ignorar)";
                                }
                                
                                System.out.println("[Sapo Inspecionar] Entidade: " + entityId.toString() + extra);
                            }
                        }
                        
                        client.player.sendSystemMessage(Component.literal("§a[Sapo] Scan concluído! Resultados impressos no console."));
                        return 1;
                    })
                )
            );
        });
    }
}
