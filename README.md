# Sistem Bancar Distribuit cu Java RMI + Web

## Descriere
Acest proiect implementează un sistem bancar distribuit folosind Java RMI (Remote Method Invocation) cu interfață web modernă. Sistemul permite mai multor clienți să se conecteze simultan din orice browser pentru a efectua operațiuni bancare.

## Caracteristici

### Funcționalități
- **Creare cont** - Clienții pot crea conturi noi cu sold inițial
- **Autentificare** - Login securizat cu număr de cont și PIN
- **Depuneri** - Depunere de fonduri în cont
- **Retrageri** - Retragere de fonduri din cont
- **Transferuri** - Transfer de fonduri între conturi
- **Istoric tranzacții** - Vizualizarea tuturor operațiunilor efectuate

### Arhitectură Distribuită
- **Server RMI** - Backend pentru logica de business și date
- **Server Web HTTP** - API REST și servire pagini web
- **Client Web** - Interfață HTML/CSS/JavaScript accesibilă din browser
- Comunicare distribuită prin RMI + HTTP

## Structura Proiectului

```
SistemBancar/
├── common/                    # Clase și interfețe partajate
│   ├── Account.java          # Model cont bancar
│   ├── Transaction.java      # Model tranzacție
│   ├── TransactionType.java  # Enum tipuri tranzacții
│   ├── BankService.java      # Interfața RMI
│   └── BankException.java    # Excepție personalizată
├── server/                    # Componenta server RMI
│   ├── BankServer.java       # Serverul RMI principal
│   └── BankServiceImpl.java  # Implementarea serviciului
├── web/                       # Componenta server Web
│   ├── WebServer.java        # Server HTTP + API REST
│   └── static/               # Fișiere web statice
│       ├── index.html        # Pagina principală
│       ├── style.css         # Stiluri CSS
│       └── app.js            # Logica JavaScript
├── client/                    # Client desktop (opțional)
│   └── BankClient.java       # Client GUI Swing
├── compile.bat               # Script compilare
├── run_server.bat            # Script pornire server RMI
├── run_web.bat               # Script pornire server Web
├── start_all.bat             # Pornire automată completă
└── README.md                 # Documentație
```

## Cerințe Sistem
- Java JDK 8 sau mai nou
- Browser web modern (Chrome, Firefox, Edge, Safari)
- Conexiune de rețea (pentru mod distribuit)

## Instrucțiuni de Utilizare

### Metoda Rapidă (Recomandată)
```batch
compile.bat
start_all.bat
```
Aceasta va compila și porni automat ambele servere, apoi va deschide browserul.

### Metoda Manuală

#### 1. Compilare
```batch
compile.bat
```

#### 2. Pornire Server RMI (Terminal 1)
```batch
run_server.bat
```

#### 3. Pornire Server Web (Terminal 2)
```batch
run_web.bat
```

#### 4. Accesare Aplicație
Deschideți browserul la: **http://localhost:8080**

### Opțiuni Avansate
```batch
run_server.bat [port_rmi]
run_web.bat -webport 8080 -rmihost localhost -rmiport 1099
```

## Conturi Demo

La pornirea serverului se creează automat 3 conturi demo:

| Număr Cont | Titular | PIN | Sold Inițial |
|------------|---------|-----|--------------|
| RO1000000000 | Ion Popescu | 1234 | 5,000.00 RON |
| RO1000000001 | Maria Ionescu | 5678 | 10,000.00 RON |
| RO1000000002 | Alexandru Gheorghe | 0000 | 2,500.00 RON |

## Conectare din Rețea

### Pe Server (Mașina Gazdă)
1. Rulați `run_server.bat` - pornește backend-ul RMI
2. Rulați `run_web.bat` - pornește serverul web
3. Notați adresa IP afișată (ex: 192.168.1.100)

### Pe Client (Alte Dispozitive)
1. Deschideți browserul
2. Navigați la `http://[IP_SERVER]:8080`
3. Conectați-vă și autentificați-vă

### Configurare Firewall Windows
Dacă aveți probleme de conectare:
```batch
netsh advfirewall firewall add rule name="Bank RMI" dir=in action=allow protocol=TCP localport=1099
netsh advfirewall firewall add rule name="Bank Web" dir=in action=allow protocol=TCP localport=8080
```

## Limitări de Securitate
- Suma maximă depunere: 1,000,000 RON
- Suma maximă retragere: 10,000 RON
- Suma maximă transfer: 50,000 RON
- PIN: exact 4 cifre

## Tehnologii Utilizate
- **Java RMI** - Remote Method Invocation pentru comunicare distribuită
- **Java HTTP Server** - Server web integrat în JDK
- **HTML5/CSS3/JavaScript** - Interfață web modernă și responsivă
- **REST API** - Comunicare client-server prin JSON
- **ConcurrentHashMap** - Stocare thread-safe pentru conturi
- **Sincronizare** - Prevenirea race conditions la tranzacții

## Diagrama Arhitecturii

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│    Browser 1    │     │    Browser 2    │     │   Browser N     │
│  (Chrome/Edge)  │     │    (Firefox)    │     │    (Safari)     │
└────────┬────────┘     └────────┬────────┘     └────────┬────────┘
         │                       │                       │
         │         HTTP          │         HTTP          │
         │      (Port 8080)      │      (Port 8080)      │
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                        ┌────────▼────────┐
                        │   Web Server    │
                        │   (REST API)    │
                        │   Port 8080     │
                        └────────┬────────┘
                                 │
                                RMI
                                 │
                        ┌────────▼────────┐
                        │   RMI Server    │
                        │   Port 1099     │
                        └────────┬────────┘
                                 │
                        ┌────────▼────────┐
                        │ BankServiceImpl │
                        │   (Business)    │
                        └────────┬────────┘
                                 │
                        ┌────────▼────────┐
                        │   Accounts DB   │
                        │ (ConcurrentMap) │
                        └─────────────────┘
```

## API Endpoints

| Endpoint | Metodă | Descriere |
|----------|--------|-----------|
| `/api/login` | POST | Autentificare utilizator |
| `/api/account` | POST | Informații cont |
| `/api/deposit` | POST | Depunere fonduri |
| `/api/withdraw` | POST | Retragere fonduri |
| `/api/transfer` | POST | Transfer între conturi |
| `/api/transactions` | POST | Istoric tranzacții |
| `/api/create-account` | POST | Creare cont nou |
| `/api/accounts` | GET | Lista conturilor |

## Autor
Proiect educațional - Sistem Bancar Distribuit cu RMI + Web

## Licență
Acest proiect este disponibil în scop educațional.
