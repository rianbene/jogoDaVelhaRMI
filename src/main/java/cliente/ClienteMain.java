package cliente;

import interfaces.GameServidor;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClienteMain {
    static void main() {
        try {
            // procurar o registro RMI na rede. passar IP e porta
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            // buscar do servidor no registry pelo nome
            GameServidor servidor = (GameServidor) registry.lookup("JogoDaVelhaServidor");

            // instanciar o cliente passando a referencia do servidor
            GameClienteImpl cliente = new GameClienteImpl(servidor);

            // iniciar o loop dos inputs
            cliente.iniciarInputLoop();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
