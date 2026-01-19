/**
 * SISTEM BANCAR - CLIENT JAVASCRIPT
 * Aplicație web pentru sistemul bancar distribuit
 */

// =====================================================
// STATE & CONFIG
// =====================================================

const state = {
    serverUrl: '',
    accountNumber: '',
    pin: '',
    account: null,
    transactions: []
};

// =====================================================
// DOM ELEMENTS
// =====================================================

const screens = {
    connection: document.getElementById('connection-screen'),
    login: document.getElementById('login-screen'),
    register: document.getElementById('register-screen'),
    dashboard: document.getElementById('dashboard-screen')
};

const forms = {
    connection: document.getElementById('connection-form'),
    login: document.getElementById('login-form'),
    register: document.getElementById('register-form'),
    deposit: document.getElementById('deposit-form'),
    withdraw: document.getElementById('withdraw-form'),
    transfer: document.getElementById('transfer-form')
};

const elements = {
    serverUrl: document.getElementById('server-url'),
    accountNumber: document.getElementById('account-number'),
    pin: document.getElementById('pin'),
    userName: document.getElementById('user-name'),
    balanceAmount: document.getElementById('balance-amount'),
    accountDisplay: document.getElementById('account-display'),
    transactionsBody: document.getElementById('transactions-body'),
    connectionError: document.getElementById('connection-error'),
    loginError: document.getElementById('login-error'),
    registerError: document.getElementById('register-error')
};

// =====================================================
// SCREEN NAVIGATION
// =====================================================

function showScreen(screenName) {
    Object.values(screens).forEach(screen => {
        screen.classList.remove('active');
    });
    screens[screenName].classList.add('active');
}

// =====================================================
// API CALLS
// =====================================================

async function apiCall(endpoint, data = {}) {
    const url = state.serverUrl + endpoint;
    
    try {
        const response = await fetch(url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        });
        
        const result = await response.json();
        
        if (!result.success) {
            throw new Error(result.error || 'Eroare necunoscută');
        }
        
        return result;
    } catch (error) {
        if (error.message.includes('Failed to fetch')) {
            throw new Error('Nu se poate conecta la server. Verificați conexiunea.');
        }
        throw error;
    }
}

// =====================================================
// CONNECTION
// =====================================================

async function connect() {
    const btn = forms.connection.querySelector('button');
    btn.classList.add('loading');
    hideError('connection');
    
    // Folosim URL-ul curent al paginii
    state.serverUrl = window.location.origin;
    
    try {
        // Testăm conexiunea prin API
        const response = await fetch(state.serverUrl + '/api/accounts');
        const data = await response.json();
        
        if (data.success) {
            showToast('Conectat la server!', 'success');
            showScreen('login');
        } else {
            throw new Error('Serverul nu răspunde corect');
        }
    } catch (error) {
        showError('connection', error.message);
    } finally {
        btn.classList.remove('loading');
    }
}

// =====================================================
// AUTHENTICATION
// =====================================================

async function login() {
    const btn = forms.login.querySelector('button[type="submit"]');
    btn.classList.add('loading');
    hideError('login');
    
    const accountNumber = elements.accountNumber.value.trim();
    const pin = elements.pin.value.trim();
    
    if (!accountNumber || !pin) {
        showError('login', 'Completați toate câmpurile!');
        btn.classList.remove('loading');
        return;
    }
    
    try {
        const result = await apiCall('/api/login', { accountNumber, pin });
        
        state.accountNumber = accountNumber;
        state.pin = pin;
        state.account = result.account;
        
        showToast('Autentificare reușită!', 'success');
        updateDashboard();
        loadTransactions();
        showScreen('dashboard');
        
        // Curățăm câmpurile
        elements.accountNumber.value = '';
        elements.pin.value = '';
        
    } catch (error) {
        showError('login', error.message);
    } finally {
        btn.classList.remove('loading');
    }
}

async function register() {
    const btn = forms.register.querySelector('button[type="submit"]');
    btn.classList.add('loading');
    hideError('register');
    
    const ownerName = document.getElementById('reg-name').value.trim();
    const pin = document.getElementById('reg-pin').value.trim();
    const pinConfirm = document.getElementById('reg-pin-confirm').value.trim();
    const initialBalance = parseFloat(document.getElementById('reg-balance').value) || 0;
    
    if (!ownerName || !pin || !pinConfirm) {
        showError('register', 'Completați toate câmpurile obligatorii!');
        btn.classList.remove('loading');
        return;
    }
    
    if (pin !== pinConfirm) {
        showError('register', 'PIN-urile nu coincid!');
        btn.classList.remove('loading');
        return;
    }
    
    if (!/^\d{4}$/.test(pin)) {
        showError('register', 'PIN-ul trebuie să conțină exact 4 cifre!');
        btn.classList.remove('loading');
        return;
    }
    
    try {
        const result = await apiCall('/api/create-account', { 
            ownerName, 
            pin, 
            initialBalance 
        });
        
        showModal('success', 'Cont Creat!', 
            `Contul a fost creat cu succes!\n\nNumăr cont: ${result.accountNumber}\nPIN: ${pin}\n\nNotați aceste informații!`);
        
        // Pre-completăm formularul de login
        elements.accountNumber.value = result.accountNumber;
        
        // Curățăm formularul de înregistrare
        forms.register.reset();
        
        showScreen('login');
        
    } catch (error) {
        showError('register', error.message);
    } finally {
        btn.classList.remove('loading');
    }
}

function logout() {
    state.accountNumber = '';
    state.pin = '';
    state.account = null;
    state.transactions = [];
    
    showToast('Deconectat cu succes!', 'info');
    showScreen('login');
}

// =====================================================
// BANKING OPERATIONS
// =====================================================

async function deposit() {
    const amountInput = document.getElementById('deposit-amount');
    const amount = parseFloat(amountInput.value);
    
    if (!amount || amount <= 0) {
        showToast('Introduceți o sumă validă!', 'error');
        return;
    }
    
    try {
        const result = await apiCall('/api/deposit', {
            accountNumber: state.accountNumber,
            pin: state.pin,
            amount
        });
        
        showToast(`Depunere de ${amount.toFixed(2)} RON efectuată!`, 'success');
        amountInput.value = '';
        
        state.account.balance = result.newBalance;
        updateDashboard();
        loadTransactions();
        
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function withdraw() {
    const amountInput = document.getElementById('withdraw-amount');
    const amount = parseFloat(amountInput.value);
    
    if (!amount || amount <= 0) {
        showToast('Introduceți o sumă validă!', 'error');
        return;
    }
    
    try {
        const result = await apiCall('/api/withdraw', {
            accountNumber: state.accountNumber,
            pin: state.pin,
            amount
        });
        
        showToast(`Retragere de ${amount.toFixed(2)} RON efectuată!`, 'success');
        amountInput.value = '';
        
        state.account.balance = result.newBalance;
        updateDashboard();
        loadTransactions();
        
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function transfer() {
    const toAccountInput = document.getElementById('transfer-to');
    const amountInput = document.getElementById('transfer-amount');
    
    const toAccount = toAccountInput.value.trim();
    const amount = parseFloat(amountInput.value);
    
    if (!toAccount) {
        showToast('Introduceți contul destinație!', 'error');
        return;
    }
    
    if (!amount || amount <= 0) {
        showToast('Introduceți o sumă validă!', 'error');
        return;
    }
    
    if (toAccount === state.accountNumber) {
        showToast('Nu puteți transfera către același cont!', 'error');
        return;
    }
    
    // Confirmare transfer
    const confirmed = confirm(`Confirmați transferul de ${amount.toFixed(2)} RON către contul ${toAccount}?`);
    if (!confirmed) return;
    
    try {
        const result = await apiCall('/api/transfer', {
            accountNumber: state.accountNumber,
            pin: state.pin,
            toAccount,
            amount
        });
        
        showToast(`Transfer de ${amount.toFixed(2)} RON efectuat!`, 'success');
        toAccountInput.value = '';
        amountInput.value = '';
        
        state.account.balance = result.newBalance;
        updateDashboard();
        loadTransactions();
        
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// =====================================================
// DATA LOADING
// =====================================================

async function loadTransactions() {
    try {
        const result = await apiCall('/api/transactions', {
            accountNumber: state.accountNumber,
            pin: state.pin
        });
        
        state.transactions = result.transactions;
        renderTransactions();
        
    } catch (error) {
        console.error('Eroare la încărcarea tranzacțiilor:', error);
    }
}

async function refreshData() {
    try {
        const result = await apiCall('/api/account', {
            accountNumber: state.accountNumber,
            pin: state.pin
        });
        
        state.account = result.account;
        updateDashboard();
        loadTransactions();
        
        showToast('Date actualizate!', 'success');
        
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// =====================================================
// UI UPDATES
// =====================================================

function updateDashboard() {
    if (!state.account) return;
    
    elements.userName.textContent = `Bine ați venit, ${state.account.ownerName}!`;
    elements.balanceAmount.textContent = `${state.account.balance.toFixed(2)} RON`;
    elements.accountDisplay.textContent = state.accountNumber;
}

function renderTransactions() {
    const tbody = elements.transactionsBody;
    
    if (state.transactions.length === 0) {
        tbody.innerHTML = '<tr><td colspan="4" class="empty-message">Nu există tranzacții</td></tr>';
        return;
    }
    
    // Sortăm invers (cele mai recente primele)
    const sorted = [...state.transactions].reverse();
    
    tbody.innerHTML = sorted.map(t => {
        const isNegative = t.type === 'WITHDRAWAL' || t.type === 'TRANSFER_OUT';
        const sign = isNegative ? '-' : '+';
        const amountClass = isNegative ? 'amount-negative' : 'amount-positive';
        
        return `
            <tr>
                <td>${t.timestamp}</td>
                <td>${t.typeName}</td>
                <td class="${amountClass}">${sign}${t.amount.toFixed(2)} RON</td>
                <td>${t.description}${t.relatedAccount ? ` (${t.relatedAccount})` : ''}</td>
            </tr>
        `;
    }).join('');
}

// =====================================================
// NOTIFICATIONS
// =====================================================

function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <span>${type === 'success' ? '✓' : type === 'error' ? '✗' : 'ℹ'}</span>
        <span>${message}</span>
    `;
    
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateX(100px)';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

function showModal(type, title, message) {
    const modal = document.getElementById('modal');
    const icon = document.getElementById('modal-icon');
    const titleEl = document.getElementById('modal-title');
    const messageEl = document.getElementById('modal-message');
    
    icon.textContent = type === 'success' ? '✓' : '✗';
    icon.className = `modal-icon ${type}`;
    titleEl.textContent = title;
    messageEl.textContent = message;
    
    modal.classList.remove('hidden');
}

function hideModal() {
    document.getElementById('modal').classList.add('hidden');
}

function showError(type, message) {
    const errorEl = elements[type + 'Error'];
    if (errorEl) {
        errorEl.textContent = message;
        errorEl.classList.remove('hidden');
    }
}

function hideError(type) {
    const errorEl = elements[type + 'Error'];
    if (errorEl) {
        errorEl.classList.add('hidden');
    }
}

// =====================================================
// HELPER FUNCTIONS
// =====================================================

function fillDemo(accountNumber, pin) {
    elements.accountNumber.value = accountNumber;
    elements.pin.value = pin;
}

// Expunem funcția global pentru onclick în HTML
window.fillDemo = fillDemo;

// =====================================================
// EVENT LISTENERS
// =====================================================

document.addEventListener('DOMContentLoaded', () => {
    // Setăm URL-ul serverului la adresa curentă
    elements.serverUrl.value = window.location.origin;
    
    // Connection form
    forms.connection.addEventListener('submit', (e) => {
        e.preventDefault();
        connect();
    });
    
    // Login form
    forms.login.addEventListener('submit', (e) => {
        e.preventDefault();
        login();
    });
    
    // Register form
    forms.register.addEventListener('submit', (e) => {
        e.preventDefault();
        register();
    });
    
    // Banking operations forms
    forms.deposit.addEventListener('submit', (e) => {
        e.preventDefault();
        deposit();
    });
    
    forms.withdraw.addEventListener('submit', (e) => {
        e.preventDefault();
        withdraw();
    });
    
    forms.transfer.addEventListener('submit', (e) => {
        e.preventDefault();
        transfer();
    });
    
    // Navigation buttons
    document.getElementById('show-register').addEventListener('click', () => {
        showScreen('register');
    });
    
    document.getElementById('back-to-login').addEventListener('click', () => {
        showScreen('login');
    });
    
    document.getElementById('back-to-connection').addEventListener('click', () => {
        showScreen('connection');
    });
    
    document.getElementById('logout-btn').addEventListener('click', logout);
    
    document.getElementById('refresh-btn').addEventListener('click', refreshData);
    
    document.getElementById('refresh-transactions').addEventListener('click', loadTransactions);
    
    // Modal close
    document.getElementById('modal-close').addEventListener('click', hideModal);
    document.getElementById('modal').addEventListener('click', (e) => {
        if (e.target.id === 'modal') hideModal();
    });
    
    // Enter key on PIN field to submit login
    elements.pin.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            login();
        }
    });
});
