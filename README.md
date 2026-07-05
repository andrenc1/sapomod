<div align="center">
  <h1>🐸 Mod Sapo </h1>
  <p><i>Um mod utilitário para Fabric focado em qualidade de vida, alertas no chat e agora com um poderoso <b>Solver Automático</b> para puzzles Akari (Light Up)!</i></p>
  
  ![Fabric](https://img.shields.io/badge/Fabric-1.21-dbb87d?style=for-the-badge&logo=fabric)
  ![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
</div>

<br/>

## ✨ Features

- **🔔 Alertas Personalizados (HUD)**: Configure palavras-chave (gatilhos) que, quando digitadas no chat, exibem um alerta vibrante e customizável diretamente na sua tela!
- **🧩 Solver de Puzzle Akari Automático**: Um sistema inteligente (usando Backtracking) que escaneia um tabuleiro 10x10 diretamente no mundo do jogo, detecta paredes/números e calcula a solução exata.
- **✨ Feedback Visual com Partículas**: A solução do puzzle é desenhada no mundo usando partículas verdes (`Happy Villager`) para que você saiba exatamente onde clicar/colocar a luz.
- **⚙️ Totalmente Configurável**: Mod Menu integrado e uma interface gráfica (Editor de HUD) para você arrastar e soltar o alerta onde quiser na tela.
- **🔍 Ferramenta de Inspeção**: Comandos embutidos para inspecionar entidades, Hitboxes e blocos diretamente in-game, com saída detalhada no console.

---

## 🚀 Como Usar os Comandos

A base de todos os comandos do mod é o `/sapo`. Aqui está a lista do que você pode fazer:

### 🧩 Puzzle Akari (Light Up)
- `/sapo resolver` - Escaneia a sala na coordenada configurada, processa o puzzle 10x10 com o algoritmo Backtracking e exibe a solução através de partículas verdes no chão.
- `/sapo limpar` - Remove todas as partículas do puzzle ativo.

### 🔔 Alertas HUD
- `/sapo gatilho <texto>` - Define a palavra que, ao aparecer no chat, vai acionar o alerta (ex: "vivo", "morto", "[Server]").
- `/sapo alerta <texto>` - Define a mensagem que vai aparecer grande na tela.
- `/sapo cor <hex>` - Altera a cor do alerta (ex: `FF0000` para vermelho).
- `/sapo tempo <segundos>` - Quanto tempo o texto vai ficar na tela.
- `/sapo testar` - Testa as configurações de HUD atuais.
- `/sapo simular <texto>` - Simula uma mensagem chegando no chat para testar seus gatilhos.
- `/sapo editarHUD` - Abre a tela interativa para reposicionar o alerta com o mouse.

### ⚙️ Utilitários
- `/sapo` - Abre a tela de Configurações do Mod Menu.
- `/sapo help` - Exibe a lista completa de comandos no chat.
- `/sapo inspecionar` - Olhe para um bloco ou entidade e use esse comando para gerar um Log de Debug completo no console (útil para descobrir Nomes, Tags e ItemDisplays).
- `/sapo debug` - Liga/desliga o modo desenvolvedor.

---

## 🛠️ Como o Solver Funciona?

O **SapoPuzzle** é o cérebro por trás da mágica:
1. **Varredura (Scan)**: O mod checa um grid 10x10 a partir de uma quina fixa no mundo (`X=-346, Y=44, Z=179`). Ele lê os `BlockStates` para encontrar as paredes pretas e usa uma `Hitbox` (`AABB`) expandida para ler os textos (`ItemDisplay`) das dicas numéricas flutuantes (0 a 4).
2. **Backtracking (Resolução)**: Uma matriz é montada virtualmente. O algoritmo testa a colocação das "lâmpadas" em espaços vazios. Se as regras do jogo *Akari* forem violadas (ex: duas luzes cruzando o mesmo corredor ou parede com quantidade errada), ele volta um passo e tenta outro caminho até encontrar a resposta validada.
3. **Renderização**: As posições corretas são enviadas para o `ClientTickEvents`, que renderiza magicamente as partículas para o jogador.

> *Dica: Ao usar o `/sapo resolver`, abra o console (Output) do seu client. O mod imprime a representação visual da matriz 10x10 que ele leu e o tempo que demorou para encontrar a solução em milissegundos!*

---

## 💻 Instalação / Ambiente Dev

Este projeto utiliza o template **Fabric** e requer **Mojang Mappings (Mojmap)**.

Para compilar o mod localmente:
```bash
# Clone o repositório e rode o Gradle:
./gradlew build
```
O `.jar` final estará na pasta `build/libs/`.

---

<div align="center">
  Feito com 💚 para automatizar os momentos mais insanos do servidor!
</div>
