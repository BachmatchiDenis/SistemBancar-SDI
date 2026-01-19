package common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interfața RMI pentru serviciul bancar
 * Aceasta este interfața la distanță pe care clienții o folosesc pentru a comunica cu serverul
 */
public interface BankService extends Remote {
    
    /**
     * Înregistrează un cont nou în sistem
     * @param ownerName Numele titularului
     * @param pin Codul PIN pentru autentificare
     * @param initialBalance Soldul inițial
     * @return Numărul de cont generat
     */
    String createAccount(String ownerName, String pin, double initialBalance) throws RemoteException, BankException;
    
    /**
     * Autentifică un client și returnează detaliile contului
     * @param accountNumber Numărul de cont
     * @param pin Codul PIN
     * @return Obiectul Account dacă autentificarea reușește
     */
    Account login(String accountNumber, String pin) throws RemoteException, BankException;
    
    /**
     * Obține soldul curent al unui cont
     * @param accountNumber Numărul de cont
     * @param pin Codul PIN
     * @return Soldul curent
     */
    double getBalance(String accountNumber, String pin) throws RemoteException, BankException;
    
    /**
     * Efectuează o depunere în cont
     * @param accountNumber Numărul de cont
     * @param pin Codul PIN
     * @param amount Suma de depus
     * @return Noul sold după depunere
     */
    double deposit(String accountNumber, String pin, double amount) throws RemoteException, BankException;
    
    /**
     * Efectuează o retragere din cont
     * @param accountNumber Numărul de cont
     * @param pin Codul PIN
     * @param amount Suma de retras
     * @return Noul sold după retragere
     */
    double withdraw(String accountNumber, String pin, double amount) throws RemoteException, BankException;
    
    /**
     * Efectuează un transfer între două conturi
     * @param fromAccount Contul sursă
     * @param pin Codul PIN al contului sursă
     * @param toAccount Contul destinație
     * @param amount Suma de transferat
     * @return Noul sold al contului sursă
     */
    double transfer(String fromAccount, String pin, String toAccount, double amount) throws RemoteException, BankException;
    
    /**
     * Obține istoricul tranzacțiilor pentru un cont
     * @param accountNumber Numărul de cont
     * @param pin Codul PIN
     * @return Lista de tranzacții
     */
    List<Transaction> getTransactionHistory(String accountNumber, String pin) throws RemoteException, BankException;
    
    /**
     * Obține informații despre cont
     * @param accountNumber Numărul de cont
     * @param pin Codul PIN
     * @return Obiectul Account cu detaliile contului
     */
    Account getAccountInfo(String accountNumber, String pin) throws RemoteException, BankException;
    
    /**
     * Verifică dacă un cont există
     * @param accountNumber Numărul de cont
     * @return true dacă contul există
     */
    boolean accountExists(String accountNumber) throws RemoteException;
    
    /**
     * Obține lista tuturor numerelor de cont (pentru administrare)
     * @return Lista numerelor de cont
     */
    List<String> getAllAccountNumbers() throws RemoteException;
    
    /**
     * Ping pentru verificarea conectivității
     * @return true dacă serverul răspunde
     */
    boolean ping() throws RemoteException;
}
