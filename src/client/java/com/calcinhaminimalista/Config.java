package com.calcinhaminimalista;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {
    // Variáveis do mod
    public static boolean ativo = true;
    public static int minCroac = 8;
    public static int maxCroac = 14;

    // Ferramentas para ler/escrever o JSON
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File ARQUIVO = new File(FabricLoader.getInstance().getConfigDir().toFile(), "sapo_config.json");

    // Método para carregar ao abrir o jogo
    public static void carregar() {
        if (ARQUIVO.exists()) {
            try (FileReader leitor = new FileReader(ARQUIVO)) {
                SapoDados dados = GSON.fromJson(leitor, SapoDados.class);
                ativo = dados.ativo;
                minCroac = dados.minCroac;
                maxCroac = dados.maxCroac;
            } catch (IOException e) {
                System.out.println("Erro ao carregar as configurações do Sapo.");
            }
        } else {
            salvar(); // Cria o arquivo padrão se não existir
        }
    }

    // Método para salvar ao fechar o menu
    public static void salvar() {
        try (FileWriter escritor = new FileWriter(ARQUIVO)) {
            SapoDados dados = new SapoDados(ativo, minCroac, maxCroac);
            GSON.toJson(dados, escritor);
        } catch (IOException e) {
            System.out.println("Erro ao salvar as configurações do Sapo.");
        }
    }

    // Classe auxiliar interna apenas para formatar o JSON
    private static class SapoDados {
        boolean ativo;
        int minCroac;
        int maxCroac;

        SapoDados(boolean ativo, int minCroac, int maxCroac) {
            this.ativo = ativo;
            this.minCroac = minCroac;
            this.maxCroac = maxCroac;
        }
    }
}
