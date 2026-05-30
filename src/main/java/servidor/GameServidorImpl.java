package servidor;

import interfaces.GameCliente;
import interfaces.GameServidor;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GameServidorImpl extends UnicastRemoteObject implements GameServidor {

    private final GameCliente[] clientes = new GameCliente[2];
    private final String[] tabuleiro = new String[9];
    private int jogadorAtual = 1;
    private boolean jogoEncerrado = false;
    // simplificação, apenas um contador de votos pra revanche
    private int votosRevanche = 0;

    public GameServidorImpl() throws RemoteException {
        super();
        resetarTabuleiro();
    }

    @Override
    public synchronized int registrarCliente(GameCliente cliente) throws RemoteException {
        if (clientes[0] == null) {
            clientes[0] = cliente;
            System.out.println("Jogador 1 conectado. Aguardando jogador 2...");
            return 1;
        } else if (clientes[1] == null) {
            clientes[1] = cliente;
            System.out.println("Jogador 2 conectado. Iniciando jogo!");
            iniciarJogo();
            return 2;
        } else {
            throw new RemoteException("Servidor cheio. Já existem 2 jogadores conectados.");
        }
    }

    @Override
    public void realizarJogada(int indice, int idJogador) throws RemoteException {
        final String[] snapshot;
        final String vencedor;
        final boolean empate;
        final int proximoJogador;

        synchronized (this) {
            if (jogoEncerrado) return;
            if (idJogador != jogadorAtual) return;
            if (indice < 0 || indice > 8) return;

            // VALIDAÇÃO NO SERVIDOR: Se a posição estiver ocupada, devolvemos o turno!
            if (!tabuleiro[indice].isEmpty()) {
                clientes[idJogador - 1].exibirMensagem("Posição já ocupada! Tente novamente.");
                clientes[idJogador - 1].notificarTurno(true); // Destrava o cliente
                return; // Encerra o método sem alterar nada
            }

            tabuleiro[indice] = (idJogador == 1) ? "O" : "X";
            snapshot = tabuleiro.clone();
            vencedor = verificarVencedor(tabuleiro);
            empate = (vencedor == null) && verificarEmpate(tabuleiro);

            if (vencedor != null || empate) {
                jogoEncerrado = true;
                proximoJogador = -1;
            } else {
                jogadorAtual = (jogadorAtual == 1) ? 2 : 1;
                proximoJogador = jogadorAtual;
            }
        }

        for (GameCliente c : clientes) {
            if (c != null) c.atualizarTela(snapshot);
        }

        if (vencedor != null) {
            System.out.println("Jogo encerrado! Vencedor: Jogador " + idJogador + " (" + vencedor + ").");
            clientes[idJogador - 1].exibirMensagem("Você venceu!");
            clientes[2 - idJogador].exibirMensagem("Você perdeu.");
            for (GameCliente c : clientes) {
                if (c != null) c.solicitarRevanche();
            }
            return;
        }

        if (empate) {
            System.out.println("Jogo encerrado! Empate.");
            for (GameCliente c : clientes) {
                if (c != null) {
                    c.exibirMensagem("Empate!");
                    c.solicitarRevanche();
                }
            }
            return;
        }

        System.out.println("Vez do Jogador " + proximoJogador + ".");
        clientes[proximoJogador - 1].notificarTurno(true);
        clientes[2 - proximoJogador].notificarTurno(false);
    }

    @Override
    public void confirmarRevanche(int idJogador, boolean aceita) throws RemoteException {
        // Se alguém recusar, derruba a sessão para todos
        if (!aceita) {
            System.out.println("Jogador " + idJogador + " recusou a revanche.");
            for (GameCliente cliente : clientes) {
                if (cliente != null) {
                    try {
                        cliente.exibirMensagem("Um jogador recusou a revanche. Sessão encerrada.");
                        cliente.encerrarSessao();
                    } catch (RemoteException e) { /* Ignora se o cliente já tiver caído */ }
                }
            }
            System.exit(0);
            return;
        }

        // Se aceitou, contabiliza o voto
        synchronized (this) {
            votosRevanche++;
            if (votosRevanche == 2) {
                System.out.println("Ambos aceitaram. Iniciando nova rodada!");
                votosRevanche = 0; // Zera para o próximo jogo
                iniciarJogo();     // Inicia a partida como se fosse a primeira vez
            } else {
                clientes[idJogador - 1].exibirMensagem("Aguardando o oponente aceitar a revanche...");
            }
        }
    }

    private void iniciarJogo() throws RemoteException {
        final String[] snapshot;

        synchronized (this) {
            resetarTabuleiro();
            jogoEncerrado = false;
            jogadorAtual = 1;
            snapshot = tabuleiro.clone();
        }

        for (GameCliente cliente : clientes) {
            if (cliente != null) cliente.atualizarTela(snapshot);
        }
        clientes[0].notificarTurno(true);
        clientes[1].notificarTurno(false);
        System.out.println("Nova rodada iniciada! Vez do Jogador 1.");
    }

    private void resetarTabuleiro() {
        for (int i = 0; i < 9; i++) {
            tabuleiro[i] = "";
        }
    }

    static String verificarVencedor(String[] tab) {
        int[][] combinacoes = {
            { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, // linhas
            { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 }, // colunas
            { 0, 4, 8 }, { 2, 4, 6 } // diagonais
        };
        for (int[] comb : combinacoes) {
            String a = tab[comb[0]];
            if (!a.isEmpty() && a.equals(tab[comb[1]]) && a.equals(tab[comb[2]])) {
                return a;
            }
        }
        return null;
    }

    static boolean verificarEmpate(String[] tab) {
        for (String casa : tab) {
            if (casa.isEmpty()) return false;
        }
        return verificarVencedor(tab) == null;
    }
}
