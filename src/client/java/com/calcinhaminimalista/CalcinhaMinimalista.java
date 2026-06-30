package com.calcinhaminimalista;

import net.fabricmc.api.ClientModInitializer;

public class CalcinhaMinimalista implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        Config.carregar();
        Sapo.registrar();
    }
}
