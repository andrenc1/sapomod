package com.calcinhaminimalista;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

public class SapoModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Component.literal("Configuração do Sapo"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            ConfigCategory category = builder.getOrCreateCategory(Component.literal("Geral"));

            category.addEntry(entryBuilder.startBooleanToggle(Component.literal("Sapo Ativo"), Config.ativo)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> Config.ativo = newValue)
                    .build());

            category.addEntry(entryBuilder.startIntField(Component.literal("Mínimo de Croacs (CPS)"), Config.minCroac)
                    .setDefaultValue(8)
                    .setMin(1)
                    .setMax(30)
                    .setSaveConsumer(newValue -> Config.minCroac = newValue)
                    .build());

            category.addEntry(entryBuilder.startIntField(Component.literal("Máximo de Croacs (CPS)"), Config.maxCroac)
                    .setDefaultValue(14)
                    .setMin(1)
                    .setMax(30)
                    .setSaveConsumer(newValue -> Config.maxCroac = newValue)
                    .build());

            ConfigCategory alertas = builder.getOrCreateCategory(Component.literal("Alertas"));

            alertas.addEntry(entryBuilder.startBooleanToggle(Component.literal("Modo Vivo ou Morto"), Config.modoVivoOuMorto)
                    .setDefaultValue(false)
                    .setSaveConsumer(newValue -> Config.modoVivoOuMorto = newValue)
                    .build());

            alertas.addEntry(entryBuilder.startStrField(Component.literal("Texto(s) Gatilho(s) (use vírgula)"), Config.textoGatilho)
                    .setDefaultValue("")
                    .setSaveConsumer(newValue -> Config.textoGatilho = newValue)
                    .build());

            alertas.addEntry(entryBuilder.startStrField(Component.literal("Mensagem de Alerta na Tela"), Config.textoAlerta)
                    .setDefaultValue("CUIDADO!")
                    .setSaveConsumer(newValue -> Config.textoAlerta = newValue)
                    .build());

            alertas.addEntry(entryBuilder.startColorField(Component.literal("Cor do Alerta"), Config.alertaCor)
                    .setDefaultValue(0xFF5555)
                    .setSaveConsumer(newValue -> Config.alertaCor = newValue)
                    .build());

            alertas.addEntry(entryBuilder.startIntField(Component.literal("Tempo do Alerta (Segundos)"), Config.alertaTempo / 20)
                    .setDefaultValue(5)
                    .setMin(1)
                    .setMax(60)
                    .setSaveConsumer(newValue -> Config.alertaTempo = newValue * 20)
                    .build());

            builder.setSavingRunnable(Config::salvar);

            return builder.build();
        };
    }
}
