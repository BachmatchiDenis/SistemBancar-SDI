package common;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Clasa care reprezintă o tranzacție bancară
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private TransactionType type;
    private double amount;
    private String description;
    private LocalDateTime timestamp;
    private String relatedAccount; // pentru transferuri
    
    public Transaction(TransactionType type, double amount, String description) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.timestamp = LocalDateTime.now();
        this.relatedAccount = null;
    }
    
    public Transaction(TransactionType type, double amount, String description, String relatedAccount) {
        this(type, amount, description);
        this.relatedAccount = relatedAccount;
    }
    
    public TransactionType getType() {
        return type;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return timestamp.format(formatter);
    }
    
    public String getRelatedAccount() {
        return relatedAccount;
    }
    
    @Override
    public String toString() {
        String sign = (type == TransactionType.WITHDRAWAL || type == TransactionType.TRANSFER_OUT) ? "-" : "+";
        String result = String.format("[%s] %s %s%.2f RON - %s", 
            getFormattedTimestamp(), 
            type.getDisplayName(),
            sign,
            amount,
            description);
        
        if (relatedAccount != null) {
            result += " (Cont: " + relatedAccount + ")";
        }
        
        return result;
    }
}
