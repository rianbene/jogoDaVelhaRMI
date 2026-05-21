package servidor;

import interfaces.GameCliente;
import interfaces.GameServidor;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class GameServidorImpl extends UnicastRemoteObject implements GameServidor {

    private final GameCliente[] clientes = new GameCliente[2];
    private final String[] tabuleiro = new String[9];
    private int jogadorAtual = 1;
    private final boolean[] respostasRevanche = new boolean[2];
    private int respostasRecebidas = 0;
    private boolean jogoEncerrado = false;

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
    public synchronized void realizarJogada(int indice, int idJogador) throws RemoteException {
        if (jogoEncerrado) return;
        if (idJogador != jogadorAtual) return;
        if (indice < 0 || indice > 8) return;
        if (!tabuleiro[indice].isEmpty()) return;

        tabuleiro[indice] = (idJogador == 1) ? "X" : "O";

        String[] snapshot = tabuleiro.clone();
        for (GameCliente c : clientes) {
            if (c != null) c.atualizarTela(snapshot);
        }

        String vencedor = verificarVencedor(tabuleiro);
        if (vencedor != null) {
            jogoEncerrado = true;
            String msg = "Jogador " + idJogador + " (" + vencedor + ") venceu!";
            for (GameCliente c : clientes) {
                if (c != null) {
                    c.exibirMensagem(msg);
                    c.solicitarRevanche();
                }
            }
            return;
        }

        if (verificarEmpate(tabuleiro)) {
            jogoEncerrado = true;
            for (GameCliente c : clientes) {
                if (c != null) {
                    c.exibirMensagem("Empate!");
                    c.solicitarRevanche();
                }
            }
            return;
        }

        jogadorAtual = (jogadorAtual == 1) ? 2 : 1;
        clientes[jogadorAtual - 1].notificarTurno(true);
        clientes[2 - jogadorAtual].notificarTurno(false);
    }

    @Override
    public synchronized void confirmarRevanche(int idJogador, boolean aceita) throws RemoteException {
        respostasRevanche[idJogador - 1] = aceita;
        respostasRecebidas++;

        if (respostasRecebidas < 2) return;

        respostasRecebidas = 0;
        if (respostasRevanche[0] && respostasRevanche[1]) {
            iniciarJogo();
        } else {
            for (GameCliente cliente : clientes) {
                if (cliente != null) cliente.exibirMensagem("Jogo encerrado. Até a próxima!");
            }
        }
    }

    private void iniciarJogo() throws RemoteException {
        resetarTabuleiro();
        jogoEncerrado = false;
        jogadorAtual = 1;
        String[] snapshot = tabuleiro.clone();
        for (GameCliente cliente : clientes) {
            if (cliente != null) cliente.atualizarTela(snapshot);
        }
        clientes[0].notificarTurno(true);
        clientes[1].notificarTurno(false);
        System.out.println("Jogo iniciado! Vez do Jogador 1.");
    }

    private void resetarTabuleiro() {
        for (int i = 0; i < 9; i++) {
            tabuleiro[i] = "";
        }
    }

    static String verificarVencedor(String[] tab) {
        int[][] combinacoes = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // linhas
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // colunas
            {0, 4, 8}, {2, 4, 6} // diagonais
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
