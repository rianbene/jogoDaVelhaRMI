package cliente;

import interfaces.GameServidor;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class ClienteMain {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("172.20.10.9", 1099);
            GameServidor servidor = (GameServidor) registry.lookup("JogoDaVelhaServidor");

            Scanner scanner = new Scanner(System.in);
            GameClienteImpl cliente = new GameClienteImpl(servidor, scanner);

            while (true) {
                cliente.iniciarInputLoop();

                System.out.println("Deseja jogar novamente? (s/n)");
                boolean aceita = scanner.next().trim().equalsIgnoreCase("s");
                servidor.confirmarRevanche(cliente.getId(), aceita);

                if (!aceita) break;

                while (!cliente.isJogoEmAndamento()) {
                    Thread.sleep(100);
                }

                if (!cliente.isSessaoAtiva()) break;
            }

            System.out.println("Até a próxima!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
