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

            // Toda a lógica foi centralizada. 
            // O código fica bloqueado aqui dentro até a sessão acabar.
            cliente.iniciarInputLoop();

            System.out.println("Até a próxima!");
            System.exit(0);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
