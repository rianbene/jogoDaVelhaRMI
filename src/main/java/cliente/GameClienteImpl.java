package cliente;

import interfaces.GameCliente;
import interfaces.GameServidor;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class GameClienteImpl extends UnicastRemoteObject implements GameCliente {

    private final GameServidor servidor;
    private final int id;
    private volatile boolean minhaVez = false;
    private volatile boolean pedindoRevanche = false;
    private volatile boolean sessaoAtiva = true;
    private final Scanner sc;

    public GameClienteImpl(GameServidor servidor, Scanner scanner) throws RemoteException {
        this.servidor = servidor;
        this.sc = scanner;
        this.id = servidor.registrarCliente(this);
        System.out.println("Cliente registrado com sucesso! Você é o jogador " + id);
    }

    public void iniciarInputLoop() {
        // loop dura até a sessão ser encerrada
        while(sessaoAtiva) {
            // verifica pedido de revanche antes de jogar
            if (pedindoRevanche) {
                System.out.println("\nDeseja jogar novamente? (S/N)");
                String resposta = sc.next();
                boolean aceita = resposta.trim().equalsIgnoreCase("s");
                pedindoRevanche = false; // Já lemos a resposta
                
                try {
                    servidor.confirmarRevanche(id, aceita);
                } catch (RemoteException e) {
                    System.out.println("Erro ao comunicar com o servidor.");
                    sessaoAtiva = false;
                }
                
                if (!aceita) sessaoAtiva = false; // Encerra o loop localmente
                continue;
                }
                
            }
            
            // lógica padrão de verificação de jogada
            if(!minhaVez) {
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
                //agr se a jogada falhar, o servidor enviar notificarTurno(true) em realizarJogada()
                //para jogar novamente
            } catch (RemoteException e) {
                System.out.println("Erro ao comunicar com o servidor: " + e.getMessage());
                minhaVez = true; //desbloqueia o cliente para tentar jogar novamente
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
        this.jogoEmAndamento = true;
        if (!suaVez) {
            System.out.println("Aguarde o turno do oponente...");
        }
    }

    @Override
    public void exibirMensagem(String mensagem) throws RemoteException {
        System.out.println(">> " + mensagem);
    }

    @Override
    public void solicitarRevanche() throws RemoteException {
        // Apenas aciona a flag. O InputLoop vai ver isso e perguntar ao usuário.
        this.pedindoRevanche = true;
    }

    @Override
    public void encerrarSessao() throws RemoteException {
        this.sessaoAtiva = false;
        System.exit(0); // Força a interrupção da execução do cliente
    }
}
