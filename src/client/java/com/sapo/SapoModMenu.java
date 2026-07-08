package com.sapo;

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
                    .setTitle(Component.literal("Sapo Configuration"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            ConfigCategory category = builder.getOrCreateCategory(Component.literal("General"));

            category.addEntry(entryBuilder.startBooleanToggle(Component.literal("Sapo Active"), Config.active)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> Config.active = newValue)
                    .build());

            category.addEntry(entryBuilder.startIntField(Component.literal("Minimum Croaks (CPS)"), Config.minCroaks)
                    .setDefaultValue(8)
                    .setMin(1)
                    .setMax(30)
                    .setSaveConsumer(newValue -> Config.minCroaks = newValue)
                    .build());

            category.addEntry(entryBuilder.startIntField(Component.literal("Maximum Croaks (CPS)"), Config.maxCroaks)
                    .setDefaultValue(14)
                    .setMin(1)
                    .setMax(30)
                    .setSaveConsumer(newValue -> Config.maxCroaks = newValue)
                    .build());

            ConfigCategory alerts = builder.getOrCreateCategory(Component.literal("Alerts"));

            alerts.addEntry(entryBuilder.startBooleanToggle(Component.literal("Alive or Dead Mode"), Config.aliveOrDeadMode)
                    .setDefaultValue(false)
                    .setSaveConsumer(newValue -> Config.aliveOrDeadMode = newValue)
                    .build());

            alerts.addEntry(entryBuilder.startStrField(Component.literal("Trigger Text(s) (use comma)"), Config.triggerText)
                    .setDefaultValue("")
                    .setSaveConsumer(newValue -> Config.triggerText = newValue)
                    .build());

            alerts.addEntry(entryBuilder.startStrField(Component.literal("On-Screen Alert Message"), Config.alertText)
                    .setDefaultValue("CAUTION!")
                    .setSaveConsumer(newValue -> Config.alertText = newValue)
                    .build());

            alerts.addEntry(entryBuilder.startColorField(Component.literal("Alert Color"), Config.alertColor)
                    .setDefaultValue(0xFF5555)
                    .setSaveConsumer(newValue -> Config.alertColor = newValue)
                    .build());

            alerts.addEntry(entryBuilder.startIntField(Component.literal("Alert Time (Seconds)"), Config.alertTime / 20)
                    .setDefaultValue(5)
                    .setMin(1)
                    .setMax(60)
                    .setSaveConsumer(newValue -> Config.alertTime = newValue * 20)
                    .build());

            ConfigCategory sounds = builder.getOrCreateCategory(Component.literal("Sounds"));

            sounds.addEntry(entryBuilder.startStrField(Component.literal("Sound Triggers (use comma)"), Config.soundTriggers)
                    .setDefaultValue("")
                    .setTooltip(Component.literal("Messages that will trigger the alert sound"))
                    .setSaveConsumer(newValue -> Config.soundTriggers = newValue)
                    .build());

            sounds.addEntry(entryBuilder.startFloatField(Component.literal("Sound Volume"), Config.soundVolume)
                    .setDefaultValue(1.0f)
                    .setMin(0.0f)
                    .setMax(2.0f)
                    .setTooltip(Component.literal("Custom sound volume (0.0 to 2.0)"))
                    .setSaveConsumer(newValue -> Config.soundVolume = newValue)
                    .build());

            builder.setSavingRunnable(Config::save);

            return builder.build();
        };
    }
}

