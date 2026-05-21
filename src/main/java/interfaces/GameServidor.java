package interfaces;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Contrato RMI para o Servidor (Back-end).
 * Os Clientes utilizam esta interface para enviar os seus comandos (inputs)
 * e registrarem-se no sistema centralizador.
 */
public interface GameServidor extends Remote {

    /**
     * Regista um novo cliente no servidor.
     * @param cliente Referência remota (Stub) do cliente para que o servidor possa fazer callbacks.
     * @return O identificador do jogador (ex: 1 para 'X', 2 para 'O').
     * @throws RemoteException Caso o servidor já tenha atingido o limite de 2 jogadores.
     */
    int registrarCliente(GameCliente cliente) throws RemoteException;

    /**
     * Processa a tentativa de jogada de um cliente.
     * @param indice A posição no vetor linear (0 a 8) onde o jogador deseja marcar.
     * @param idJogador O identificador do jogador que está a realizar a ação.
     */
    void realizarJogada(int indice, int idJogador) throws RemoteException;

    /**
     * Recebe a resposta do cliente quanto ao desejo de jogar novamente.
     * @param idJogador O identificador do jogador.
     * @param aceita Verdadeiro se o utilizador aceitar a desforra, falso caso contrário.
     */
    void confirmarRevanche(int idJogador, boolean aceita) throws RemoteException;
}