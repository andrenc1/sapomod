package com.calcinhaminimalista;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Display;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SapoPuzzle {

    public static List<BlockPos> solucaoAtiva = new ArrayList<>();
    private static long tempoInicio;

    public static void escanearEResolver(Minecraft client) {
        solucaoAtiva.clear();
        if (client.level == null || client.player == null) return;

        char[][] grid = new char[10][10];
        
        // Preenche o grid vazio primeiro
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                grid[r][c] = '.';
            }
        }

        // Canto superior esquerdo exato do tabuleiro
        BlockPos topLeft = new BlockPos(-346, 43, 179);

        client.player.sendSystemMessage(Component.literal("§e[Sapo] Escaneando o tabuleiro Globalmente..."));
        System.out.println("[Sapo Debug] Iniciando varredura Global a partir de: " + topLeft.toShortString());

        // CAIXA GIGANTE: Engole a sala 10x10 inteira de uma vez!
        AABB roomBox = new AABB(
                topLeft.getX() - 1, topLeft.getY(), topLeft.getZ() - 1,
                topLeft.getX() + 11, topLeft.getY() + 4, topLeft.getZ() + 11
        );

        // Pega todos os hologramas da sala de uma vez só
        List<Entity> entities = client.level.getEntities(client.player, roomBox);

        boolean achouParede = false;

        for (Entity e : entities) {
            if (e instanceof Display.ItemDisplay itemDisplay) {
                
                // MÁGICA: Descobre EXATAMENTE a qual quadrado do grid esta entidade pertence
                BlockPos ePos = e.blockPosition();
                int c = ePos.getX() - topLeft.getX();
                int r = ePos.getZ() - topLeft.getZ();

                // Se a entidade estiver dentro do limite 10x10 do tabuleiro
                if (c >= 0 && c < 10 && r >= 0 && r < 10) {
                    String data = itemDisplay.getItemStack().getComponents().toString();

                    if (data.contains("number0")) grid[r][c] = '0';
                    else if (data.contains("number1")) grid[r][c] = '1';
                    else if (data.contains("number2")) grid[r][c] = '2';
                    else if (data.contains("number3")) grid[r][c] = '3';
                    else if (data.contains("number4")) grid[r][c] = '4';
                    else if (data.contains("colors=[0]")) {
                        // Se é preto absoluto e AINDA não tem um número guardado neste quadrado, marca como parede (X)
                        if (grid[r][c] == '.') grid[r][c] = 'X';
                    }
                }
            }
        }

        // Verifica se realmente leu algo sólido para o grid
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                if (grid[r][c] != '.') achouParede = true;
            }
        }

        System.out.println("[Sapo Debug] Tabuleiro lido com sucesso (Matriz 10x10):");
        for (int i = 0; i < 10; i++) {
            System.out.println("[Sapo Debug] " + new String(grid[i]));
        }

        if (!achouParede) {
            System.out.println("[Sapo Debug] AVISO CRÍTICO: Nenhuma entidade preta ou número detectado!");
            client.player.sendSystemMessage(Component.literal("§c[Sapo] Scan cancelado: Tabuleiro Holográfico parece vazio."));
            return;
        }

        System.out.println("[Sapo Debug] Iniciando o algoritmo Solver (Backtracking)...");
        tempoInicio = System.currentTimeMillis();

        boolean[][] bulbs = new boolean[10][10];
        boolean resolvido = solve(grid, bulbs, 0, 0);

        long tempoFim = System.currentTimeMillis();

        if (resolvido) {
            System.out.println("[Sapo Debug] SUCESSO! Resolvido em " + (tempoFim - tempoInicio) + "ms.");
            client.player.sendSystemMessage(Component.literal("§a[Sapo] Puzzle resolvido! Siga as partículas verdes."));
            for (int r = 0; r < 10; r++) {
                for (int c = 0; c < 10; c++) {
                    if (bulbs[r][c]) {
                        solucaoAtiva.add(topLeft.offset(c, 0, r));
                    }
                }
            }
        } else {
            System.out.println("[Sapo Debug] FALHA! O algoritmo não encontrou nenhuma solução possível.");
            client.player.sendSystemMessage(Component.literal("§c[Sapo] Falha ao resolver. Verifique o console."));
        }
    }

    private static boolean solve(char[][] grid, boolean[][] bulbs, int r, int c) {
        if (System.currentTimeMillis() - tempoInicio > 5000) return false;

        if (c == 10) {
            r++;
            c = 0;
        }
        
        if (isUnsolvable(grid, bulbs, r, c)) return false;

        if (r == 10) return isSolved(grid, bulbs);

        if (grid[r][c] != '.') return solve(grid, bulbs, r, c + 1);

        if (canPlace(grid, bulbs, r, c)) {
            bulbs[r][c] = true;
            if (solve(grid, bulbs, r, c + 1)) return true;
            bulbs[r][c] = false;
        }

        return solve(grid, bulbs, r, c + 1);
    }

    private static boolean isUnsolvable(char[][] grid, boolean[][] bulbs, int currentR, int currentC) {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                if (grid[r][c] >= '0' && grid[r][c] <= '4') {
                    int req = grid[r][c] - '0';
                    int adj = 0;
                    int possible = 0;

                    if (r > 0 && grid[r - 1][c] == '.') {
                        if (bulbs[r - 1][c]) { adj++; possible++; }
                        else if (!isDecided(r - 1, c, currentR, currentC)) possible++;
                    }
                    if (r < 9 && grid[r + 1][c] == '.') {
                        if (bulbs[r + 1][c]) { adj++; possible++; }
                        else if (!isDecided(r + 1, c, currentR, currentC)) possible++;
                    }
                    if (c > 0 && grid[r][c - 1] == '.') {
                        if (bulbs[r][c - 1]) { adj++; possible++; }
                        else if (!isDecided(r, c - 1, currentR, currentC)) possible++;
                    }
                    if (c < 9 && grid[r][c + 1] == '.') {
                        if (bulbs[r][c + 1]) { adj++; possible++; }
                        else if (!isDecided(r, c + 1, currentR, currentC)) possible++;
                    }

                    if (adj > req) return true;
                    if (possible < req) return true;
                }
            }
        }

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                if (grid[r][c] == '.') {
                    if (isDecided(r, c, currentR, currentC)) {
                        if (!isCellLit(grid, bulbs, r, c)) {
                            boolean canBeLit = false;
                            
                            for (int i = r + 1; i < 10; i++) {
                                if (grid[i][c] != '.') break;
                                if (!isDecided(i, c, currentR, currentC)) {
                                    canBeLit = true;
                                    break;
                                }
                            }
                            
                            if (!canBeLit) {
                                for (int i = c + 1; i < 10; i++) {
                                    if (grid[r][i] != '.') break;
                                    if (!isDecided(r, i, currentR, currentC)) {
                                        canBeLit = true;
                                        break;
                                    }
                                }
                            }

                            if (!canBeLit) return true; 
                        }
                    }
                }
            }
        }

        return false;
    }

    private static boolean isDecided(int r, int c, int currentR, int currentC) {
        return (r < currentR) || (r == currentR && c < currentC);
    }

    private static boolean canPlace(char[][] grid, boolean[][] bulbs, int r, int c) {
        for (int i = r - 1; i >= 0; i--) {
            if (grid[i][c] != '.') break;
            if (bulbs[i][c]) return false;
        }
        for (int i = r + 1; i < 10; i++) {
            if (grid[i][c] != '.') break;
            if (bulbs[i][c]) return false;
        }
        for (int i = c - 1; i >= 0; i--) {
            if (grid[r][i] != '.') break;
            if (bulbs[r][i]) return false;
        }
        for (int i = c + 1; i < 10; i++) {
            if (grid[r][i] != '.') break;
            if (bulbs[r][i]) return false;
        }
        return true;
    }

    private static boolean isCellLit(char[][] grid, boolean[][] bulbs, int r, int c) {
        if (bulbs[r][c]) return true;
        for (int i = r - 1; i >= 0; i--) {
            if (grid[i][c] != '.') break;
            if (bulbs[i][c]) return true;
        }
        for (int i = r + 1; i < 10; i++) {
            if (grid[i][c] != '.') break;
            if (bulbs[i][c]) return true;
        }
        for (int i = c - 1; i >= 0; i--) {
            if (grid[r][i] != '.') break;
            if (bulbs[r][i]) return true;
        }
        for (int i = c + 1; i < 10; i++) {
            if (grid[r][i] != '.') break;
            if (bulbs[r][i]) return true;
        }
        return false;
    }

    private static boolean isSolved(char[][] grid, boolean[][] bulbs) {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                if (grid[r][c] >= '0' && grid[r][c] <= '4') {
                    int req = grid[r][c] - '0';
                    int adj = 0;
                    if (r > 0 && bulbs[r - 1][c]) adj++;
                    if (r < 9 && bulbs[r + 1][c]) adj++;
                    if (c > 0 && bulbs[r][c - 1]) adj++;
                    if (c < 9 && bulbs[r][c + 1]) adj++;
                    if (adj != req) return false;
                }
            }
        }

        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                if (grid[r][c] == '.' && !isCellLit(grid, bulbs, r, c)) return false;
            }
        }
        return true;
    }
}
