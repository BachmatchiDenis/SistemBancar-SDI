package web;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import common.*;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.Naming;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Server Web pentru Sistemul Bancar
 * Oferă o interfață REST API și servește paginile web
 */
public class WebServer {
    
    private static final int DEFAULT_WEB_PORT = 8080;
    private static final int DEFAULT_RMI_PORT = 1099;
    private static BankService bankService;
    private static String basePath;
    
    public static void main(String[] args) {
        int webPort = DEFAULT_WEB_PORT;
        String rmiHost = "localhost";
        int rmiPort = DEFAULT_RMI_PORT;
        
        // Parsare argumente
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-webport") && i + 1 < args.length) {
                webPort = Integer.parseInt(args[++i]);
            } else if (args[i].equals("-rmihost") && i + 1 < args.length) {
                rmiHost = args[++i];
            } else if (args[i].equals("-rmiport") && i + 1 < args.length) {
                rmiPort = Integer.parseInt(args[++i]);
            }
        }
        
        try {
            // Determinăm path-ul de bază
            basePath = System.getProperty("user.dir");
            
            // Conectare la serviciul RMI
            String rmiUrl = "rmi://" + rmiHost + ":" + rmiPort + "/BankService";
            System.out.println("╔════════════════════════════════════════════════════════════╗");
            System.out.println("║           SISTEM BANCAR - SERVER WEB                       ║");
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("  Conectare la serviciul RMI: " + rmiUrl);
            
            bankService = (BankService) Naming.lookup(rmiUrl);
            
            if (bankService.ping()) {
                System.out.println("  [OK] Conectat la serviciul bancar RMI");
            }
            
            // Creare server HTTP
            String localIP = InetAddress.getLocalHost().getHostAddress();
            HttpServer server = HttpServer.create(new InetSocketAddress(webPort), 0);
            
            // Configurare handlere
            server.createContext("/", new StaticFileHandler());
            server.createContext("/api/login", new LoginHandler());
            server.createContext("/api/account", new AccountHandler());
            server.createContext("/api/deposit", new DepositHandler());
            server.createContext("/api/withdraw", new WithdrawHandler());
            server.createContext("/api/transfer", new TransferHandler());
            server.createContext("/api/transactions", new TransactionsHandler());
            server.createContext("/api/create-account", new CreateAccountHandler());
            server.createContext("/api/accounts", new AccountsListHandler());
            
            server.setExecutor(Executors.newFixedThreadPool(10));
            server.start();
            
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("║                  SERVER WEB PORNIT                         ║");
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("  Adresa locală:  http://localhost:" + webPort);
            System.out.println("  Adresa rețea:   http://" + localIP + ":" + webPort);
            System.out.println("╠════════════════════════════════════════════════════════════╣");
            System.out.println("  Deschideți browserul la adresa de mai sus");
            System.out.println("╚════════════════════════════════════════════════════════════╝");
            
        } catch (Exception e) {
            System.err.println("[EROARE] " + e.getMessage());
            System.err.println("Asigurați-vă că serverul RMI este pornit!");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Handler pentru fișiere statice (HTML, CSS, JS)
     */
    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }
            
            try {
                Path filePath = Paths.get(basePath, "web", "static", path);
                
                if (Files.exists(filePath) && !Files.isDirectory(filePath)) {
                    byte[] content = Files.readAllBytes(filePath);
                    String contentType = getContentType(path);
                    
                    exchange.getResponseHeaders().set("Content-Type", contentType);
                    exchange.sendResponseHeaders(200, content.length);
                    exchange.getResponseBody().write(content);
                } else {
                    // Returnăm index.html pentru SPA routing
                    Path indexPath = Paths.get(basePath, "web", "static", "index.html");
                    byte[] content = Files.readAllBytes(indexPath);
                    exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, content.length);
                    exchange.getResponseBody().write(content);
                }
            } catch (Exception e) {
                String error = "File not found";
                exchange.sendResponseHeaders(404, error.length());
                exchange.getResponseBody().write(error.getBytes());
            }
            exchange.getResponseBody().close();
        }
        
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html; charset=UTF-8";
            if (path.endsWith(".css")) return "text/css; charset=UTF-8";
            if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (path.endsWith(".json")) return "application/json; charset=UTF-8";
            if (path.endsWith(".png")) return "image/png";
            if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
            if (path.endsWith(".svg")) return "image/svg+xml";
            if (path.endsWith(".ico")) return "image/x-icon";
            return "text/plain; charset=UTF-8";
        }
    }
    
    /**
     * Handler pentru autentificare
     */
    static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            if (!exchange.getRequestMethod().equals("POST")) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            
            try {
                Map<String, String> params = parseJsonBody(exchange);
                String accountNumber = params.get("accountNumber");
                String pin = params.get("pin");
                
                Account account = bankService.login(accountNumber, pin);
                
                String json = String.format(
                    "{\"success\":true,\"account\":{\"accountNumber\":\"%s\",\"ownerName\":\"%s\",\"balance\":%.2f,\"createdAt\":\"%s\"}}",
                    account.getAccountNumber(),
                    account.getOwnerName(),
                    account.getBalance(),
                    account.getFormattedCreatedAt()
                );
                
                sendJson(exchange, 200, json);
                
            } catch (BankException e) {
                sendError(exchange, 401, e.getMessage());
            } catch (Exception e) {
                sendError(exchange, 500, "Eroare server: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handler pentru informații cont
     */
    static class AccountHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            try {
                Map<String, String> params = parseJsonBody(exchange);
                String accountNumber = params.get("accountNumber");
                String pin = params.get("pin");
                
                Account account = bankService.getAccountInfo(accountNumber, pin);
                
                String json = String.format(
                    "{\"success\":true,\"account\":{\"accountNumber\":\"%s\",\"ownerName\":\"%s\",\"balance\":%.2f}}",
                    account.getAccountNumber(),
                    account.getOwnerName(),
                    account.getBalance()
                );
                
                sendJson(exchange, 200, json);
                
            } catch (BankException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                sendError(exchange, 500, "Eroare server: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handler pentru depuneri
     */
    static class DepositHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            try {
                Map<String, String> params = parseJsonBody(exchange);
                String accountNumber = params.get("accountNumber");
                String pin = params.get("pin");
                double amount = Double.parseDouble(params.get("amount"));
                
                double newBalance = bankService.deposit(accountNumber, pin, amount);
                
                String json = String.format(
                    "{\"success\":true,\"message\":\"Depunere efectuată cu succes\",\"newBalance\":%.2f}",
                    newBalance
                );
                
                sendJson(exchange, 200, json);
                
            } catch (BankException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                sendError(exchange, 500, "Eroare server: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handler pentru retrageri
     */
    static class WithdrawHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            try {
                Map<String, String> params = parseJsonBody(exchange);
                String accountNumber = params.get("accountNumber");
                String pin = params.get("pin");
                double amount = Double.parseDouble(params.get("amount"));
                
                double newBalance = bankService.withdraw(accountNumber, pin, amount);
                
                String json = String.format(
                    "{\"success\":true,\"message\":\"Retragere efectuată cu succes\",\"newBalance\":%.2f}",
                    newBalance
                );
                
                sendJson(exchange, 200, json);
                
            } catch (BankException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                sendError(exchange, 500, "Eroare server: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handler pentru transferuri
     */
    static class TransferHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            try {
                Map<String, String> params = parseJsonBody(exchange);
                String accountNumber = params.get("accountNumber");
                String pin = params.get("pin");
                String toAccount = params.get("toAccount");
                double amount = Double.parseDouble(params.get("amount"));
                
                double newBalance = bankService.transfer(accountNumber, pin, toAccount, amount);
                
                String json = String.format(
                    "{\"success\":true,\"message\":\"Transfer efectuat cu succes\",\"newBalance\":%.2f}",
                    newBalance
                );
                
                sendJson(exchange, 200, json);
                
            } catch (BankException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                sendError(exchange, 500, "Eroare server: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handler pentru istoric tranzacții
     */
    static class TransactionsHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            try {
                Map<String, String> params = parseJsonBody(exchange);
                String accountNumber = params.get("accountNumber");
                String pin = params.get("pin");
                
                List<Transaction> transactions = bankService.getTransactionHistory(accountNumber, pin);
                
                StringBuilder json = new StringBuilder("{\"success\":true,\"transactions\":[");
                for (int i = 0; i < transactions.size(); i++) {
                    Transaction t = transactions.get(i);
                    if (i > 0) json.append(",");
                    json.append(String.format(
                        "{\"type\":\"%s\",\"typeName\":\"%s\",\"amount\":%.2f,\"description\":\"%s\",\"timestamp\":\"%s\",\"relatedAccount\":\"%s\"}",
                        t.getType().name(),
                        t.getType().getDisplayName(),
                        t.getAmount(),
                        escapeJson(t.getDescription()),
                        t.getFormattedTimestamp(),
                        t.getRelatedAccount() != null ? t.getRelatedAccount() : ""
                    ));
                }
                json.append("]}");
                
                sendJson(exchange, 200, json.toString());
                
            } catch (BankException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                sendError(exchange, 500, "Eroare server: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handler pentru creare cont
     */
    static class CreateAccountHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            try {
                Map<String, String> params = parseJsonBody(exchange);
                String ownerName = params.get("ownerName");
                String pin = params.get("pin");
                double initialBalance = Double.parseDouble(params.getOrDefault("initialBalance", "0"));
                
                String accountNumber = bankService.createAccount(ownerName, pin, initialBalance);
                
                String json = String.format(
                    "{\"success\":true,\"message\":\"Cont creat cu succes\",\"accountNumber\":\"%s\"}",
                    accountNumber
                );
                
                sendJson(exchange, 200, json);
                
            } catch (BankException e) {
                sendError(exchange, 400, e.getMessage());
            } catch (Exception e) {
                sendError(exchange, 500, "Eroare server: " + e.getMessage());
            }
        }
    }
    
    /**
     * Handler pentru lista conturilor (pentru transfer)
     */
    static class AccountsListHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            setCorsHeaders(exchange);
            
            if (exchange.getRequestMethod().equals("OPTIONS")) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            try {
                List<String> accounts = bankService.getAllAccountNumbers();
                
                StringBuilder json = new StringBuilder("{\"success\":true,\"accounts\":[");
                for (int i = 0; i < accounts.size(); i++) {
                    if (i > 0) json.append(",");
                    json.append("\"").append(accounts.get(i)).append("\"");
                }
                json.append("]}");
                
                sendJson(exchange, 200, json.toString());
                
            } catch (Exception e) {
                sendError(exchange, 500, "Eroare server: " + e.getMessage());
            }
        }
    }
    
    // Metode utilitare
    
    private static void setCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }
    
    private static void sendJson(HttpExchange exchange, int code, String json) throws IOException {
        byte[] response = json.getBytes("UTF-8");
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(code, response.length);
        exchange.getResponseBody().write(response);
        exchange.getResponseBody().close();
    }
    
    private static void sendError(HttpExchange exchange, int code, String message) throws IOException {
        String json = String.format("{\"success\":false,\"error\":\"%s\"}", escapeJson(message));
        sendJson(exchange, code, json);
    }
    
    private static String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    private static Map<String, String> parseJsonBody(HttpExchange exchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "UTF-8");
        BufferedReader br = new BufferedReader(isr);
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            body.append(line);
        }
        
        return parseSimpleJson(body.toString());
    }
    
    private static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> result = new HashMap<>();
        
        // Parser JSON simplu pentru obiecte plate
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
        
        String[] pairs = json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                String key = keyValue[0].trim().replace("\"", "");
                String value = keyValue[1].trim();
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                result.put(key, value);
            }
        }
        
        return result;
    }
}
