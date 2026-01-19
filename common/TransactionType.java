package common;

import java.io.Serializable;

/**
 * Enum pentru tipurile de tranzacții
 */
public enum TransactionType implements Serializable {
    DEPOSIT("Depunere"),
    WITHDRAWAL("Retragere"),
    TRANSFER_IN("Transfer primit"),
    TRANSFER_OUT("Transfer trimis");
    
    private final String displayName;
    
    TransactionType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
