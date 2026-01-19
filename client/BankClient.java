package client;

import common.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.rmi.Naming;
import java.util.List;

/**
 * Clientul cu interfață grafică pentru sistemul bancar
 */
public class BankClient extends JFrame {
    
    private static final long serialVersionUID = 1L;
    
    // Serviciul RMI
    private BankService bankService;
    
    // Informații sesiune
    private String currentAccountNumber;
    private String currentPin;
    private Account currentAccount;
    
    // Componente pentru conexiune
    private JTextField serverIpField;
    private JTextField serverPortField;
    private JButton connectButton;
    private JLabel connectionStatusLabel;
    
    // Componente pentru autentificare
    private JTextField accountNumberField;
    private JPasswordField pinField;
    private JButton loginButton;
    private JButton createAccountButton;
    
    // Panouri principale
    private JPanel connectionPanel;
    private JPanel loginPanel;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    private JPanel containerPanel;
    
    // Componente pentru operațiuni bancare
    private JLabel welcomeLabel;
    private JLabel balanceLabel;
    private JTextField amountField;
    private JTextField transferAccountField;
    private JTextArea historyArea;
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    
    // Culori temă
    private static final Color PRIMARY_COLOR = new Color(25, 118, 210);
    private static final Color PRIMARY_DARK = new Color(21, 101, 192);
    private static final Color SUCCESS_COLOR = new Color(76, 175, 80);
    private static final Color ERROR_COLOR = new Color(244, 67, 54);
    private static final Color BACKGROUND_COLOR = new Color(245, 245, 245);
    private static final Color CARD_COLOR = Color.WHITE;
    
    public BankClient() {
        setTitle("Sistem Bancar - Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));
        
        // Setăm look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        initializeComponents();
        layoutComponents();
        
        setVisible(true);
    }
    
    private void initializeComponents() {
        // Container cu CardLayout pentru a schimba între ecrane
        cardLayout = new CardLayout();
        containerPanel = new JPanel(cardLayout);
        
        // Inițializăm panourile
        createConnectionPanel();
        createLoginPanel();
        createMainPanel();
        
        containerPanel.add(connectionPanel, "connection");
        containerPanel.add(loginPanel, "login");
        containerPanel.add(mainPanel, "main");
    }
    
    private void createConnectionPanel() {
        connectionPanel = new JPanel(new GridBagLayout());
        connectionPanel.setBackground(BACKGROUND_COLOR);
        
        JPanel card = createCard();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(450, 350));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Titlu
        JLabel titleLabel = new JLabel("🏦 Sistem Bancar", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(titleLabel, gbc);
        
        // Subtitlu
        JLabel subtitleLabel = new JLabel("Conectare la Server RMI", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        gbc.gridy = 1;
        card.add(subtitleLabel, gbc);
        
        gbc.gridwidth = 1;
        
        // Server IP
        JLabel ipLabel = new JLabel("Adresa IP Server:");
        ipLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        card.add(ipLabel, gbc);
        
        serverIpField = createStyledTextField("localhost");
        gbc.gridx = 1;
        card.add(serverIpField, gbc);
        
        // Port
        JLabel portLabel = new JLabel("Port:");
        portLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 3;
        card.add(portLabel, gbc);
        
        serverPortField = createStyledTextField("1099");
        gbc.gridx = 1;
        card.add(serverPortField, gbc);
        
        // Buton conectare
        connectButton = createStyledButton("Conectare", PRIMARY_COLOR);
        connectButton.addActionListener(e -> connectToServer());
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        card.add(connectButton, gbc);
        
        // Status conexiune
        connectionStatusLabel = new JLabel(" ", SwingConstants.CENTER);
        connectionStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridy = 5;
        gbc.insets = new Insets(10, 10, 10, 10);
        card.add(connectionStatusLabel, gbc);
        
        connectionPanel.add(card);
    }
    
    private void createLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        loginPanel.setBackground(BACKGROUND_COLOR);
        
        JPanel card = createCard();
        card.setLayout(new GridBagLayout());
        card.setPreferredSize(new Dimension(450, 400));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Titlu
        JLabel titleLabel = new JLabel("🔐 Autentificare", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        card.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        
        // Număr cont
        JLabel accountLabel = new JLabel("Număr Cont:");
        accountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 1;
        card.add(accountLabel, gbc);
        
        accountNumberField = createStyledTextField("");
        gbc.gridx = 1;
        card.add(accountNumberField, gbc);
        
        // PIN
        JLabel pinLabel = new JLabel("Cod PIN:");
        pinLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridx = 0;
        gbc.gridy = 2;
        card.add(pinLabel, gbc);
        
        pinField = new JPasswordField();
        pinField.setPreferredSize(new Dimension(200, 35));
        pinField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pinField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        gbc.gridx = 1;
        card.add(pinField, gbc);
        
        // Buton autentificare
        loginButton = createStyledButton("Autentificare", PRIMARY_COLOR);
        loginButton.addActionListener(e -> login());
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        card.add(loginButton, gbc);
        
        // Buton creare cont
        createAccountButton = createStyledButton("Creare Cont Nou", SUCCESS_COLOR);
        createAccountButton.addActionListener(e -> showCreateAccountDialog());
        gbc.gridy = 4;
        gbc.insets = new Insets(5, 10, 10, 10);
        card.add(createAccountButton, gbc);
        
        // Buton înapoi
        JButton backButton = createStyledButton("← Înapoi", Color.GRAY);
        backButton.addActionListener(e -> {
            bankService = null;
            cardLayout.show(containerPanel, "connection");
        });
        gbc.gridy = 5;
        card.add(backButton, gbc);
        
        loginPanel.add(card);
    }
    
    private void createMainPanel() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(BACKGROUND_COLOR);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        welcomeLabel = new JLabel("Bine ați venit!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);
        
        balanceLabel = new JLabel("Sold: 0.00 RON");
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        balanceLabel.setForeground(Color.WHITE);
        headerPanel.add(balanceLabel, BorderLayout.EAST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Panou operațiuni
        JPanel operationsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        operationsPanel.setBackground(BACKGROUND_COLOR);
        
        // Card Depunere
        operationsPanel.add(createOperationCard("💰 Depunere", "Depune fonduri în cont", 
            "Sumă:", e -> performDeposit()));
        
        // Card Retragere
        operationsPanel.add(createOperationCard("💸 Retragere", "Retrage fonduri din cont", 
            "Sumă:", e -> performWithdraw()));
        
        // Card Transfer
        JPanel transferCard = createCard();
        transferCard.setLayout(new BoxLayout(transferCard, BoxLayout.Y_AXIS));
        transferCard.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel transferTitle = new JLabel("💳 Transfer");
        transferTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        transferTitle.setForeground(PRIMARY_COLOR);
        transferTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        transferCard.add(transferTitle);
        transferCard.add(Box.createVerticalStrut(5));
        
        JLabel transferDesc = new JLabel("Transferă fonduri către alt cont");
        transferDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        transferDesc.setForeground(Color.GRAY);
        transferDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        transferCard.add(transferDesc);
        transferCard.add(Box.createVerticalStrut(15));
        
        JLabel toAccountLabel = new JLabel("Cont destinație:");
        toAccountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        toAccountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        transferCard.add(toAccountLabel);
        transferCard.add(Box.createVerticalStrut(5));
        
        transferAccountField = createStyledTextField("");
        transferAccountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        transferAccountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        transferCard.add(transferAccountField);
        transferCard.add(Box.createVerticalStrut(10));
        
        JLabel amountLabel = new JLabel("Sumă:");
        amountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        amountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        transferCard.add(amountLabel);
        transferCard.add(Box.createVerticalStrut(5));
        
        JTextField transferAmountField = createStyledTextField("");
        transferAmountField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        transferAmountField.setAlignmentX(Component.LEFT_ALIGNMENT);
        transferCard.add(transferAmountField);
        transferCard.add(Box.createVerticalStrut(15));
        
        JButton transferButton = createStyledButton("Transferă", SUCCESS_COLOR);
        transferButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        transferButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        transferButton.addActionListener(e -> performTransfer(transferAmountField.getText()));
        transferCard.add(transferButton);
        
        operationsPanel.add(transferCard);
        
        mainPanel.add(operationsPanel, BorderLayout.CENTER);
        
        // Panou istoric
        JPanel historyPanel = createCard();
        historyPanel.setLayout(new BorderLayout(10, 10));
        historyPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        historyPanel.setPreferredSize(new Dimension(0, 200));
        
        JLabel historyTitle = new JLabel("📋 Istoric Tranzacții");
        historyTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        historyTitle.setForeground(PRIMARY_COLOR);
        historyPanel.add(historyTitle, BorderLayout.NORTH);
        
        // Tabel pentru istoric
        String[] columns = {"Data/Ora", "Tip", "Sumă", "Descriere"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionTable = new JTable(tableModel);
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        transactionTable.setRowHeight(25);
        transactionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        transactionTable.getTableHeader().setBackground(PRIMARY_COLOR);
        transactionTable.getTableHeader().setForeground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        historyPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Butoane
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(CARD_COLOR);
        
        JButton refreshButton = createStyledButton("🔄 Actualizare", PRIMARY_COLOR);
        refreshButton.addActionListener(e -> refreshData());
        buttonPanel.add(refreshButton);
        
        JButton logoutButton = createStyledButton("🚪 Deconectare", ERROR_COLOR);
        logoutButton.addActionListener(e -> logout());
        buttonPanel.add(logoutButton);
        
        historyPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(historyPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createOperationCard(String title, String description, String fieldLabel, 
            ActionListener action) {
        JPanel card = createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLbl.setForeground(PRIMARY_COLOR);
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(5));
        
        JLabel descLbl = new JLabel(description);
        descLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLbl.setForeground(Color.GRAY);
        descLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(descLbl);
        card.add(Box.createVerticalStrut(15));
        
        JLabel fieldLbl = new JLabel(fieldLabel);
        fieldLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        fieldLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(fieldLbl);
        card.add(Box.createVerticalStrut(5));
        
        JTextField field = createStyledTextField("");
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(field);
        card.add(Box.createVerticalStrut(15));
        
        JButton button = createStyledButton("Execută", SUCCESS_COLOR);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        // Stocăm field-ul pentru a-l accesa în action
        button.putClientProperty("amountField", field);
        button.addActionListener(e -> {
            JTextField amountFld = (JTextField) ((JButton)e.getSource()).getClientProperty("amountField");
            amountField = amountFld;
            action.actionPerformed(e);
        });
        
        card.add(button);
        
        return card;
    }
    
    private JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(220, 220, 220), 1, true),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return card;
    }
    
    private JTextField createStyledTextField(String text) {
        JTextField field = new JTextField(text);
        field.setPreferredSize(new Dimension(200, 35));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return field;
    }
    
    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(200, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });
        
        return button;
    }
    
    private void layoutComponents() {
        setLayout(new BorderLayout());
        add(containerPanel, BorderLayout.CENTER);
    }
    
    private void connectToServer() {
        String ip = serverIpField.getText().trim();
        String portStr = serverPortField.getText().trim();
        
        if (ip.isEmpty()) {
            showError("Introduceți adresa IP a serverului!");
            return;
        }
        
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            showError("Port invalid!");
            return;
        }
        
        connectionStatusLabel.setText("Conectare în curs...");
        connectionStatusLabel.setForeground(Color.ORANGE);
        connectButton.setEnabled(false);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                String url = "rmi://" + ip + ":" + port + "/BankService";
                bankService = (BankService) Naming.lookup(url);
                return bankService.ping();
            }
            
            @Override
            protected void done() {
                connectButton.setEnabled(true);
                try {
                    if (get()) {
                        connectionStatusLabel.setText("✓ Conectat cu succes!");
                        connectionStatusLabel.setForeground(SUCCESS_COLOR);
                        
                        Timer timer = new Timer(1000, e -> {
                            cardLayout.show(containerPanel, "login");
                        });
                        timer.setRepeats(false);
                        timer.start();
                    }
                } catch (Exception e) {
                    connectionStatusLabel.setText("✗ Eroare: " + e.getCause().getMessage());
                    connectionStatusLabel.setForeground(ERROR_COLOR);
                    bankService = null;
                }
            }
        };
        worker.execute();
    }
    
    private void login() {
        String accountNum = accountNumberField.getText().trim();
        String pin = new String(pinField.getPassword());
        
        if (accountNum.isEmpty() || pin.isEmpty()) {
            showError("Completați toate câmpurile!");
            return;
        }
        
        try {
            currentAccount = bankService.login(accountNum, pin);
            currentAccountNumber = accountNum;
            currentPin = pin;
            
            welcomeLabel.setText("Bine ați venit, " + currentAccount.getOwnerName() + "!");
            refreshData();
            
            cardLayout.show(containerPanel, "main");
            
            // Curățăm câmpurile
            accountNumberField.setText("");
            pinField.setText("");
            
        } catch (BankException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Eroare de conexiune: " + e.getMessage());
        }
    }
    
    private void showCreateAccountDialog() {
        JDialog dialog = new JDialog(this, "Creare Cont Nou", true);
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        dialog.getContentPane().setBackground(BACKGROUND_COLOR);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel titleLabel = new JLabel("Cont Nou", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        dialog.add(titleLabel, gbc);
        
        gbc.gridwidth = 1;
        
        dialog.add(new JLabel("Nume titular:"), setGbc(gbc, 0, 1));
        JTextField nameField = createStyledTextField("");
        dialog.add(nameField, setGbc(gbc, 1, 1));
        
        dialog.add(new JLabel("PIN (4 cifre):"), setGbc(gbc, 0, 2));
        JPasswordField newPinField = new JPasswordField();
        newPinField.setPreferredSize(new Dimension(200, 35));
        dialog.add(newPinField, setGbc(gbc, 1, 2));
        
        dialog.add(new JLabel("Confirmare PIN:"), setGbc(gbc, 0, 3));
        JPasswordField confirmPinField = new JPasswordField();
        confirmPinField.setPreferredSize(new Dimension(200, 35));
        dialog.add(confirmPinField, setGbc(gbc, 1, 3));
        
        dialog.add(new JLabel("Depozit inițial (RON):"), setGbc(gbc, 0, 4));
        JTextField initialBalanceField = createStyledTextField("0");
        dialog.add(initialBalanceField, setGbc(gbc, 1, 4));
        
        JButton createBtn = createStyledButton("Creează Cont", SUCCESS_COLOR);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        dialog.add(createBtn, gbc);
        
        createBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String pin1 = new String(newPinField.getPassword());
            String pin2 = new String(confirmPinField.getPassword());
            String balanceStr = initialBalanceField.getText().trim();
            
            if (name.isEmpty()) {
                showError("Introduceți numele titularului!");
                return;
            }
            if (!pin1.equals(pin2)) {
                showError("PIN-urile nu coincid!");
                return;
            }
            if (!pin1.matches("\\d{4}")) {
                showError("PIN-ul trebuie să conțină exact 4 cifre!");
                return;
            }
            
            double balance;
            try {
                balance = Double.parseDouble(balanceStr);
                if (balance < 0) {
                    showError("Soldul inițial nu poate fi negativ!");
                    return;
                }
            } catch (NumberFormatException ex) {
                showError("Sumă invalidă!");
                return;
            }
            
            try {
                String newAccountNumber = bankService.createAccount(name, pin1, balance);
                dialog.dispose();
                
                JOptionPane.showMessageDialog(this,
                    "Cont creat cu succes!\n\n" +
                    "Număr cont: " + newAccountNumber + "\n" +
                    "PIN: " + pin1 + "\n\n" +
                    "Vă rugăm să notați aceste informații!",
                    "Cont Creat",
                    JOptionPane.INFORMATION_MESSAGE);
                
                accountNumberField.setText(newAccountNumber);
                
            } catch (BankException ex) {
                showError(ex.getMessage());
            } catch (Exception ex) {
                showError("Eroare: " + ex.getMessage());
            }
        });
        
        dialog.setVisible(true);
    }
    
    private GridBagConstraints setGbc(GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        return gbc;
    }
    
    private void performDeposit() {
        String amountStr = amountField.getText().trim();
        
        if (amountStr.isEmpty()) {
            showError("Introduceți suma!");
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            showError("Sumă invalidă!");
            return;
        }
        
        try {
            double newBalance = bankService.deposit(currentAccountNumber, currentPin, amount);
            showSuccess("Depunere efectuată cu succes!\nNou sold: " + String.format("%.2f", newBalance) + " RON");
            refreshData();
            amountField.setText("");
        } catch (BankException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Eroare: " + e.getMessage());
        }
    }
    
    private void performWithdraw() {
        String amountStr = amountField.getText().trim();
        
        if (amountStr.isEmpty()) {
            showError("Introduceți suma!");
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            showError("Sumă invalidă!");
            return;
        }
        
        try {
            double newBalance = bankService.withdraw(currentAccountNumber, currentPin, amount);
            showSuccess("Retragere efectuată cu succes!\nNou sold: " + String.format("%.2f", newBalance) + " RON");
            refreshData();
            amountField.setText("");
        } catch (BankException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Eroare: " + e.getMessage());
        }
    }
    
    private void performTransfer(String amountStr) {
        String toAccount = transferAccountField.getText().trim();
        
        if (toAccount.isEmpty() || amountStr.isEmpty()) {
            showError("Completați toate câmpurile!");
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            showError("Sumă invalidă!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirmați transferul de " + String.format("%.2f", amount) + " RON\n" +
            "către contul " + toAccount + "?",
            "Confirmare Transfer",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            double newBalance = bankService.transfer(currentAccountNumber, currentPin, toAccount, amount);
            showSuccess("Transfer efectuat cu succes!\nNou sold: " + String.format("%.2f", newBalance) + " RON");
            refreshData();
            transferAccountField.setText("");
        } catch (BankException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("Eroare: " + e.getMessage());
        }
    }
    
    private void refreshData() {
        try {
            currentAccount = bankService.getAccountInfo(currentAccountNumber, currentPin);
            balanceLabel.setText("Sold: " + String.format("%.2f", currentAccount.getBalance()) + " RON");
            
            // Actualizăm tabelul de tranzacții
            tableModel.setRowCount(0);
            List<Transaction> transactions = bankService.getTransactionHistory(currentAccountNumber, currentPin);
            
            // Afișăm în ordine inversă (cele mai recente primele)
            for (int i = transactions.size() - 1; i >= 0; i--) {
                Transaction t = transactions.get(i);
                String sign = (t.getType() == TransactionType.WITHDRAWAL || 
                              t.getType() == TransactionType.TRANSFER_OUT) ? "-" : "+";
                tableModel.addRow(new Object[]{
                    t.getFormattedTimestamp(),
                    t.getType().getDisplayName(),
                    sign + String.format("%.2f", t.getAmount()) + " RON",
                    t.getDescription()
                });
            }
            
        } catch (Exception e) {
            showError("Eroare la actualizare: " + e.getMessage());
        }
    }
    
    private void logout() {
        currentAccountNumber = null;
        currentPin = null;
        currentAccount = null;
        tableModel.setRowCount(0);
        cardLayout.show(containerPanel, "login");
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Eroare", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Succes", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new BankClient());
    }
}
