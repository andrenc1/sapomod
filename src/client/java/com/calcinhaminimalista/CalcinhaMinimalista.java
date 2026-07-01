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
import net.minecraft.resources.Identifier;

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
            );
        });
    }
}
