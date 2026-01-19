package server;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.net.InetAddress;

/**
 * Serverul RMI pentru sistemul bancar
 * Pornește registry-ul RMI și înregistrează serviciul bancar
 */
public class BankServer {
    
    private static final int DEFAULT_PORT = 1099;
    private static final String SERVICE_NAME = "BankService";
    
    public static void main(String[] args) {
        int port = DEFAULT_PORT;
        
        // Verificăm dacă s-a specificat un port diferit
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Port invalid. Se folosește portul implicit: " + DEFAULT_PORT);
            }
        }
        
        try {
            // Obținem adresa IP locală
            String localIP = InetAddress.getLocalHost().getHostAddress();
            
            // Setăm proprietățile pentru RMI
            System.setProperty("java.rmi.server.hostname", localIP);
            
            System.out.println("╔════════════════════════════════════════════════════════════╗");
            System.out.println("║           SISTEM BANCAR - SERVER RMI                       ║");
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("║  Inițializare server...                                    ║");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            System.out.println();
            
            // Creăm registry-ul RMI
            Registry registry = null;
            try {
                registry = LocateRegistry.createRegistry(port);
                System.out.println("[OK] Registry RMI creat pe portul " + port);
            } catch (Exception e) {
                // Registry-ul poate fi deja creat
                registry = LocateRegistry.getRegistry(port);
                System.out.println("[OK] Registry RMI existent utilizat pe portul " + port);
            }
            
            // Creăm și înregistrăm serviciul bancar
            BankServiceImpl bankService = new BankServiceImpl();
            
            // Înregistrăm serviciul în registry
            String bindUrl = "rmi://" + localIP + ":" + port + "/" + SERVICE_NAME;
            Naming.rebind(bindUrl, bankService);
            
            System.out.println("[OK] Serviciul bancar înregistrat cu succes");
            System.out.println();
            System.out.println("╔════════════════════════════════════════════════════════════╗");
            System.out.println("║                    SERVER PORNIT                           ║");
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("  Adresa IP: " + localIP);
            System.out.println("  Port: " + port);
            System.out.println("  Serviciu: " + SERVICE_NAME);
            System.out.println("  URL Conexiune: " + bindUrl);
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("  Conturi active: " + bankService.getTotalAccounts());
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("  Pentru conectare de pe alt dispozitiv din rețea,");
            System.out.println("  folosiți adresa IP: " + localIP);
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("  Apăsați Ctrl+C pentru a opri serverul");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            System.out.println();
            System.out.println("=== LOG OPERAȚIUNI ===");
            
            // Hook pentru închidere curată
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\n[INFO] Server oprit.");
            }));
            
            // Menținem serverul activ
            synchronized (BankServer.class) {
                BankServer.class.wait();
            }
            
        } catch (Exception e) {
            System.err.println("[EROARE] Nu s-a putut porni serverul: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
