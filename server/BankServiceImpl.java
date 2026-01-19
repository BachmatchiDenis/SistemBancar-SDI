package server;

import common.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.io.*;
import java.nio.file.*;

/**
 * Implementarea serviciului bancar RMI
 * Această clasă conține toată logica de business pentru operațiunile bancare
 * Datele sunt salvate persistent în format JSON
 */
public class BankServiceImpl extends UnicastRemoteObject implements BankService {
    private static final long serialVersionUID = 1L;
    
    // Stocare thread-safe pentru conturi
    private final Map<String, Account> accounts;
    // Generator de numere de cont
    private final AtomicLong accountNumberGenerator;
    // Prefix pentru numerele de cont
    private static final String ACCOUNT_PREFIX = "RO";
    // Fișier pentru persistență date - folosim calea absolută
    private static final String DATA_DIR;
    private static final String ACCOUNTS_FILE;
    private static final String COUNTER_FILE;
    
    static {
        // Determinăm directorul proiectului bazat pe locația clasei
        String userDir = System.getProperty("user.dir");
        DATA_DIR = userDir + File.separator + "data";
        ACCOUNTS_FILE = DATA_DIR + File.separator + "accounts.dat";
        COUNTER_FILE = DATA_DIR + File.separator + "counter.dat";
    }
    
    public BankServiceImpl() throws RemoteException {
        super();
        this.accounts = new ConcurrentHashMap<>();
        this.accountNumberGenerator = new AtomicLong(1000000000L);
        
        // Creăm directorul pentru date dacă nu există
        createDataDirectory();
        
        // Încărcăm datele salvate sau creăm conturi demo
        if (!loadData()) {
            createDemoAccounts();
            saveData();
        }
    }
    
    /**
     * Creează directorul pentru date dacă nu există
     */
    private void createDataDirectory() {
        try {
            Files.createDirectories(Paths.get(DATA_DIR));
        } catch (IOException e) {
            System.err.println("Eroare la crearea directorului data: " + e.getMessage());
        }
    }
    
    /**
     * Salvează toate datele pe disc (conturi și counter)
     */
    private synchronized void saveData() {
        try {
            // Salvăm conturile
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(ACCOUNTS_FILE))) {
                oos.writeObject(new HashMap<>(accounts));
            }
            
            // Salvăm counter-ul
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(COUNTER_FILE))) {
                oos.writeLong(accountNumberGenerator.get());
            }
            
            System.out.println("[PERSISTENȚĂ] Date salvate cu succes (" + accounts.size() + " conturi)");
        } catch (IOException e) {
            System.err.println("[PERSISTENȚĂ] Eroare la salvare: " + e.getMessage());
        }
    }
    
    /**
     * Încarcă datele de pe disc
     * @return true dacă datele au fost încărcate cu succes
     */
    @SuppressWarnings("unchecked")
    private boolean loadData() {
        File accountsFile = new File(ACCOUNTS_FILE);
        File counterFile = new File(COUNTER_FILE);
        
        if (!accountsFile.exists() || !counterFile.exists()) {
            System.out.println("[PERSISTENȚĂ] Nu există date salvate, se vor crea conturi demo");
            return false;
        }
        
        try {
            // Încărcăm conturile
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(ACCOUNTS_FILE))) {
                Map<String, Account> loadedAccounts = (Map<String, Account>) ois.readObject();
                accounts.putAll(loadedAccounts);
            }
            
            // Încărcăm counter-ul
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(COUNTER_FILE))) {
                long counter = ois.readLong();
                accountNumberGenerator.set(counter);
            }
            
            System.out.println("[PERSISTENȚĂ] Date încărcate cu succes (" + accounts.size() + " conturi)");
            System.out.println("[PERSISTENȚĂ] Următorul număr cont: " + ACCOUNT_PREFIX + accountNumberGenerator.get());
            
            // Afișăm conturile existente
            for (Account acc : accounts.values()) {
                System.out.println("  -> Cont: " + acc.getAccountNumber() + " | " + acc.getOwnerName() + 
                    " | Sold: " + String.format("%.2f", acc.getBalance()) + " RON");
            }
            
            return true;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("[PERSISTENȚĂ] Eroare la încărcare: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Creează conturi demo pentru testare
     */
    private void createDemoAccounts() {
        try {
            // Cont demo 1
            Account demo1 = new Account(generateAccountNumber(), "Ion Popescu", "1234", 5000.0);
            accounts.put(demo1.getAccountNumber(), demo1);
            System.out.println("Cont demo creat: " + demo1.getAccountNumber() + " (PIN: 1234)");
            
            // Cont demo 2
            Account demo2 = new Account(generateAccountNumber(), "Maria Ionescu", "5678", 10000.0);
            accounts.put(demo2.getAccountNumber(), demo2);
            System.out.println("Cont demo creat: " + demo2.getAccountNumber() + " (PIN: 5678)");
            
            // Cont demo 3
            Account demo3 = new Account(generateAccountNumber(), "Alexandru Gheorghe", "0000", 2500.0);
            accounts.put(demo3.getAccountNumber(), demo3);
            System.out.println("Cont demo creat: " + demo3.getAccountNumber() + " (PIN: 0000)");
            
        } catch (Exception e) {
            System.err.println("Eroare la crearea conturilor demo: " + e.getMessage());
        }
    }
    
    /**
     * Generează un număr de cont unic
     */
    private String generateAccountNumber() {
        return ACCOUNT_PREFIX + accountNumberGenerator.getAndIncrement();
    }
    
    @Override
    public String createAccount(String ownerName, String pin, double initialBalance) 
            throws RemoteException, BankException {
        
        // Validări
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new BankException("Numele titularului este obligatoriu!");
        }
        if (pin == null || pin.length() != 4 || !pin.matches("\\d{4}")) {
            throw new BankException("PIN-ul trebuie să conțină exact 4 cifre!");
        }
        if (initialBalance < 0) {
            throw new BankException("Soldul inițial nu poate fi negativ!");
        }
        
        String accountNumber = generateAccountNumber();
        Account newAccount = new Account(accountNumber, ownerName.trim(), pin, initialBalance);
        accounts.put(accountNumber, newAccount);
        
        logOperation("CREARE CONT", accountNumber, "Titular: " + ownerName + ", Sold inițial: " + initialBalance);
        
        // Salvăm datele pe disc
        saveData();
        
        return accountNumber;
    }
    
    @Override
    public Account login(String accountNumber, String pin) throws RemoteException, BankException {
        Account account = validateAndGetAccount(accountNumber, pin);
        logOperation("AUTENTIFICARE", accountNumber, "Autentificare reușită");
        return account;
    }
    
    @Override
    public double getBalance(String accountNumber, String pin) throws RemoteException, BankException {
        Account account = validateAndGetAccount(accountNumber, pin);
        return account.getBalance();
    }
    
    @Override
    public synchronized double deposit(String accountNumber, String pin, double amount) 
            throws RemoteException, BankException {
        
        if (amount <= 0) {
            throw new BankException("Suma de depus trebuie să fie pozitivă!");
        }
        if (amount > 1000000) {
            throw new BankException("Suma maximă pentru o depunere este 1.000.000 RON!");
        }
        
        Account account = validateAndGetAccount(accountNumber, pin);
        
        synchronized (account) {
            double newBalance = account.getBalance() + amount;
            account.setBalance(newBalance);
            account.addTransaction(new Transaction(TransactionType.DEPOSIT, amount, 
                "Depunere numerar"));
            
            logOperation("DEPUNERE", accountNumber, "Sumă: " + amount + " RON, Sold nou: " + newBalance + " RON");
            
            // Salvăm datele pe disc
            saveData();
            
            return newBalance;
        }
    }
    
    @Override
    public synchronized double withdraw(String accountNumber, String pin, double amount) 
            throws RemoteException, BankException {
        
        if (amount <= 0) {
            throw new BankException("Suma de retras trebuie să fie pozitivă!");
        }
        if (amount > 10000) {
            throw new BankException("Suma maximă pentru o retragere este 10.000 RON!");
        }
        
        Account account = validateAndGetAccount(accountNumber, pin);
        
        synchronized (account) {
            if (account.getBalance() < amount) {
                throw new BankException("Fonduri insuficiente! Sold disponibil: " + 
                    String.format("%.2f", account.getBalance()) + " RON");
            }
            
            double newBalance = account.getBalance() - amount;
            account.setBalance(newBalance);
            account.addTransaction(new Transaction(TransactionType.WITHDRAWAL, amount, 
                "Retragere numerar"));
            
            logOperation("RETRAGERE", accountNumber, "Sumă: " + amount + " RON, Sold nou: " + newBalance + " RON");
            
            // Salvăm datele pe disc
            saveData();
            
            return newBalance;
        }
    }
    
    @Override
    public synchronized double transfer(String fromAccount, String pin, String toAccount, double amount) 
            throws RemoteException, BankException {
        
        if (amount <= 0) {
            throw new BankException("Suma de transferat trebuie să fie pozitivă!");
        }
        if (amount > 50000) {
            throw new BankException("Suma maximă pentru un transfer este 50.000 RON!");
        }
        if (fromAccount.equals(toAccount)) {
            throw new BankException("Nu puteți transfera bani către același cont!");
        }
        
        Account source = validateAndGetAccount(fromAccount, pin);
        Account destination = accounts.get(toAccount);
        
        if (destination == null) {
            throw new BankException("Contul destinație nu există: " + toAccount);
        }
        
        // Blocăm ambele conturi într-o ordine consistentă pentru a evita deadlock
        Object firstLock = fromAccount.compareTo(toAccount) < 0 ? source : destination;
        Object secondLock = fromAccount.compareTo(toAccount) < 0 ? destination : source;
        
        synchronized (firstLock) {
            synchronized (secondLock) {
                if (source.getBalance() < amount) {
                    throw new BankException("Fonduri insuficiente! Sold disponibil: " + 
                        String.format("%.2f", source.getBalance()) + " RON");
                }
                
                // Efectuăm transferul
                source.setBalance(source.getBalance() - amount);
                destination.setBalance(destination.getBalance() + amount);
                
                // Adăugăm tranzacțiile
                source.addTransaction(new Transaction(TransactionType.TRANSFER_OUT, amount, 
                    "Transfer către " + destination.getOwnerName(), toAccount));
                destination.addTransaction(new Transaction(TransactionType.TRANSFER_IN, amount, 
                    "Transfer de la " + source.getOwnerName(), fromAccount));
                
                logOperation("TRANSFER", fromAccount, 
                    "Către: " + toAccount + ", Sumă: " + amount + " RON");
                
                // Salvăm datele pe disc
                saveData();
                
                return source.getBalance();
            }
        }
    }
    
    @Override
    public List<Transaction> getTransactionHistory(String accountNumber, String pin) 
            throws RemoteException, BankException {
        Account account = validateAndGetAccount(accountNumber, pin);
        return account.getTransactionHistory();
    }
    
    @Override
    public Account getAccountInfo(String accountNumber, String pin) throws RemoteException, BankException {
        return validateAndGetAccount(accountNumber, pin);
    }
    
    @Override
    public boolean accountExists(String accountNumber) throws RemoteException {
        return accounts.containsKey(accountNumber);
    }
    
    @Override
    public List<String> getAllAccountNumbers() throws RemoteException {
        return new ArrayList<>(accounts.keySet());
    }
    
    @Override
    public boolean ping() throws RemoteException {
        return true;
    }
    
    /**
     * Validează credențialele și returnează contul
     */
    private Account validateAndGetAccount(String accountNumber, String pin) throws BankException {
        if (accountNumber == null || accountNumber.isEmpty()) {
            throw new BankException("Numărul de cont este obligatoriu!");
        }
        if (pin == null || pin.isEmpty()) {
            throw new BankException("PIN-ul este obligatoriu!");
        }
        
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new BankException("Contul nu există: " + accountNumber);
        }
        if (!account.validatePin(pin)) {
            throw new BankException("PIN incorect!");
        }
        
        return account;
    }
    
    /**
     * Logare operațiuni pentru monitorizare
     */
    private void logOperation(String operation, String accountNumber, String details) {
        String timestamp = java.time.LocalDateTime.now()
            .format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        System.out.println("[" + timestamp + "] " + operation + " | Cont: " + accountNumber + " | " + details);
    }
    
    /**
     * Returnează numărul total de conturi
     */
    public int getTotalAccounts() {
        return accounts.size();
    }
}
