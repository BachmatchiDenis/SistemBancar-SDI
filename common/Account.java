package common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Clasa care reprezintă un cont bancar
 * Implementează Serializable pentru a putea fi transmisă prin RMI
 */
public class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String accountNumber;
    private String ownerName;
    private String pin;
    private double balance;
    private List<Transaction> transactionHistory;
    private LocalDateTime createdAt;
    
    public Account(String accountNumber, String ownerName, String pin, double initialBalance) {
        this.accountNumber = accountNumber;
        this.ownerName = ownerName;
        this.pin = pin;
        this.balance = initialBalance;
        this.transactionHistory = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        
        if (initialBalance > 0) {
            addTransaction(new Transaction(TransactionType.DEPOSIT, initialBalance, 
                "Depozit inițial la crearea contului"));
        }
    }
    
    public String getAccountNumber() {
        return accountNumber;
    }
    
    public String getOwnerName() {
        return ownerName;
    }
    
    public boolean validatePin(String pin) {
        return this.pin.equals(pin);
    }
    
    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    public List<Transaction> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }
    
    public void addTransaction(Transaction transaction) {
        this.transactionHistory.add(transaction);
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public String getFormattedCreatedAt() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return createdAt.format(formatter);
    }
    
    @Override
    public String toString() {
        return "Cont: " + accountNumber + " | Titular: " + ownerName + " | Sold: " + String.format("%.2f", balance) + " RON";
    }
}
