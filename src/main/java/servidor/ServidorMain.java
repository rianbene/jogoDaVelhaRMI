package servidor;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServidorMain {
    public static void main(String[] args) {
        try {
            GameServidorImpl servidor = new GameServidorImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("JogoDaVelhaServidor", servidor);
            System.out.println("Servidor RMI iniciado na porta 1099. Aguardando jogadores...");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
