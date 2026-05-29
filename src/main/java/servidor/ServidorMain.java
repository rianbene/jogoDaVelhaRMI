package servidor;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ServidorMain {
    public static void main(String[] args) {
        try {
            System.setProperty("java.rmi.server.hostname", "172.20.10.9");

            GameServidorImpl servidor = new GameServidorImpl();
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("JogoDaVelhaServidor", servidor);
            System.out.println("Servidor RMI iniciado na porta 1099. Aguardando jogadores...");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
