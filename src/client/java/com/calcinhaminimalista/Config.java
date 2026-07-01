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
    public static boolean modoDev = false;
    public static String textoGatilho = "";
    public static String textoAlerta = "CUIDADO!";
    public static int alertaX = 100;
    public static int alertaY = 50;
    public static float alertaEscala = 2.0f;
    public static int alertaTempo = 100;
    public static int alertaCor = 0xFF5555;
    public static boolean modoVivoOuMorto = false;

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
                modoDev = dados.modoDev;
                if (dados.textoGatilho != null) textoGatilho = dados.textoGatilho;
                if (dados.textoAlerta != null) textoAlerta = dados.textoAlerta;
                alertaX = dados.alertaX != 0 ? dados.alertaX : 100;
                alertaY = dados.alertaY != 0 ? dados.alertaY : 50;
                alertaEscala = dados.alertaEscala != 0.0f ? dados.alertaEscala : 2.0f;
                alertaTempo = dados.alertaTempo != 0 ? dados.alertaTempo : 100;
                alertaCor = dados.alertaCor != 0 ? dados.alertaCor : 0xFF5555;
                modoVivoOuMorto = dados.modoVivoOuMorto;
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
            SapoDados dados = new SapoDados(ativo, minCroac, maxCroac, modoDev, textoGatilho, textoAlerta, alertaX, alertaY, alertaEscala, alertaTempo, alertaCor, modoVivoOuMorto);
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
        boolean modoDev;
        String textoGatilho;
        String textoAlerta;
        int alertaX;
        int alertaY;
        float alertaEscala;
        int alertaTempo;
        int alertaCor;
        boolean modoVivoOuMorto;

        SapoDados(boolean ativo, int minCroac, int maxCroac, boolean modoDev, String textoGatilho, String textoAlerta, int alertaX, int alertaY, float alertaEscala, int alertaTempo, int alertaCor, boolean modoVivoOuMorto) {
            this.ativo = ativo;
            this.minCroac = minCroac;
            this.maxCroac = maxCroac;
            this.modoDev = modoDev;
            this.textoGatilho = textoGatilho;
            this.textoAlerta = textoAlerta;
            this.alertaX = alertaX;
            this.alertaY = alertaY;
            this.alertaEscala = alertaEscala;
            this.alertaTempo = alertaTempo;
            this.alertaCor = alertaCor;
            this.modoVivoOuMorto = modoVivoOuMorto;
        }
    }
}
