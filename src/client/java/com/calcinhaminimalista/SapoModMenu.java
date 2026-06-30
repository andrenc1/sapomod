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

            builder.setSavingRunnable(Config::salvar);

            return builder.build();
        };
    }
}
