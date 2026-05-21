package cliente;

import interfaces.GameCliente;
import interfaces.GameServidor;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class GameClienteImpl extends UnicastRemoteObject implements GameCliente {

    private GameServidor servidor;
    private int id;
    private boolean minhaVez = false;
    private boolean jogoEmAndamento = true;
    private final Scanner sc = new Scanner(System.in);

    public GameClienteImpl(GameServidor servidor) throws RemoteException {
        this.servidor = servidor;
        this.id = servidor.registrarCliente(this);
        System.out.println("Cliente registrado com sucesso! Você é o jogador " + id);
    }

    public void iniciarInputLoop() {

        while(jogoEmAndamento) {
            if(!minhaVez) {
                // Pausa para não sobrecarregar a CPU enquanto aguarda o turno
                try {Thread.sleep(200);} catch (InterruptedException e) {}
                continue;
            }

            System.out.println("Sua vez, digite a posição (0 a 8)");

            if (!sc.hasNextInt()) {
                System.out.println("Entrada inválida. Digite um numero");
                sc.next();
                continue;
            }

            int posicao = sc.nextInt();
            if (posicao < 0 || posicao > 8) {
                System.out.println("Posicao invalida. Tente novamente.");
                continue;
            }

            try {
                minhaVez = false; //bloqueia o usuario de digitar 2x, só o servidor q pode dizer se é a vez dele agr
                servidor.realizarJogada(posicao, id);
            } catch (RemoteException e) {
                System.out.println("Erro ao comunicar com o servidor: " + e.getMessage());
            }
        }
    }

    private String formatarCasa(String jogada, String numCasa){
        return (jogada == null || jogada.trim().isEmpty()) ? numCasa : jogada;
    }

    @Override
    public void atualizarTela(String[] tabuleiro) throws RemoteException {
        System.out.println("\n=== TABULEIRO ===");
        System.out.println(" " + formatarCasa(tabuleiro[0], "0") + " | " + formatarCasa(tabuleiro[1], "1") + " | " + formatarCasa(tabuleiro[2], "2"));
        System.out.println("---+---+---");
        System.out.println(" " + formatarCasa(tabuleiro[3], "3") + " | " + formatarCasa(tabuleiro[4], "4") + " | " + formatarCasa(tabuleiro[5], "5"));
        System.out.println("---+---+---");
        System.out.println(" " + formatarCasa(tabuleiro[6], "6") + " | " + formatarCasa(tabuleiro[7], "7") + " | " + formatarCasa(tabuleiro[8], "8"));
        System.out.println("=================\n");
    }

    @Override
    public void notificarTurno(boolean suaVez) throws RemoteException {
        this.minhaVez = suaVez;
        if(!suaVez) {
            System.out.println("Aguarde o turno do oponente...");
        }
    }

    @Override
    public void exibirMensagem(String mensagem) throws RemoteException {
        System.out.println(">> " + mensagem);
    }

    @Override
    public void solicitarRevanche() throws RemoteException {
        jogoEmAndamento = false;
        System.out.println("Deseja jogar novamente? (s/n)");
        String resposta = sc.next().trim().toLowerCase();
        servidor.confirmarRevanche(id, resposta.equals("s"));
    }
}
