package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Contrato RMI para o Cliente (Front-end).
 * O Servidor utiliza esta interface para invocar métodos (callbacks) nos clientes ligados,
 * evitando que o cliente tenha de questionar constantemente o servidor (polling).
 */
public interface GameCliente extends Remote {
    /**
     * Atualiza a representação visual do jogo no terminal do cliente.
     * @param tabuleiro Vetor linear de 9 posições contendo o estado atual do jogo.
     */
    void atualizarTela(String[] tabuleiro) throws RemoteException;

    /**
     * Notifica o cliente se é o seu turno de jogar ou se deve aguardar.
     * @param suaVez Verdadeiro se o cliente puder enviar comandos, falso caso contrário.
     */
    void notificarTurno(boolean suaVez) throws RemoteException;

    /**
     * Exibe notificações gerais no terminal (ex: "Vitória!", "Oponente desconectou-se").
     * @param mensagem A mensagem a ser impressa.
     */
    void exibirMensagem(String mensagem) throws RemoteException;

    /**
     * Invoca o estado de final de jogo no cliente, solicitando que este pergunte
     * ao utilizador se deseja iniciar uma revanche
     */
    void solicitarRevanche() throws RemoteException;
}
