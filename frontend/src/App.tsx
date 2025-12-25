import { useState, useEffect } from 'react';
import { ThemeProvider } from './ThemeContext';
import { MainPage } from './components/MainPage';
import { SettingsModal } from './components/SettingsModal';
import { OperationsModal } from './components/OperationsModal';
import { DifferentiationModal } from './components/DifferentiationModal';
import { FunctionEditModal } from './components/FunctionEditModal';
import { FunctionGraphModal } from './components/FunctionGraphModal';
import { CompositeFunctionModal } from './components/CompositeFunctionModal';
import { IntegrationModal } from './components/IntegrationModal';
import { LoginModal } from './components/LoginModal';
import { RegisterModal } from './components/RegisterModal';
import { ErrorModal } from './ErrorModal';
import { setGlobalErrorHandler } from './errorManager';
import { clearCredentials } from './api';

type ModalType = 'settings' | 'operations' | 'differentiation' | 'graph' | 'composite' | 'integration' | null;
type AuthModalType = 'login' | 'register' | null;
type FactoryType = 'array' | 'linked-list';

export default function App() {
  const [activeModal, setActiveModal] = useState<ModalType>(null);
  const [authModal, setAuthModal] = useState<AuthModalType>(null);
  const [editingFunctionId, setEditingFunctionId] = useState<number | null>(null);
  const [error, setError] = useState<string>('');
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [username, setUsername] = useState<string>('');

  const [factoryKey, setFactoryKey] = useState<FactoryType>(() => {
    const saved = localStorage.getItem('factoryKey');
    return saved === 'linked-list' ? 'linked-list' : 'array';
  });

  useEffect(() => {
    setGlobalErrorHandler(setError);
    verifyAuth();

    // Listen for auth-required events (401 errors)
    const handleAuthRequired = () => {
      setIsAuthenticated(false);
      setUsername('');
      setAuthModal('login');
    };

    window.addEventListener('auth-required', handleAuthRequired);
    return () => {
      window.removeEventListener('auth-required', handleAuthRequired);
    };
  }, []);

  const verifyAuth = async () => {
    const savedUsername = localStorage.getItem('username');
    const savedPassword = localStorage.getItem('password');
    
    if (!savedUsername || !savedPassword) {
      setIsAuthenticated(false);
      setUsername('');
      return;
    }

    // Verify credentials by making a test API call
    try {
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/v1/functions?size=1`, {
        method: 'GET',
        headers: {
          'Authorization': `Basic ${btoa(`${savedUsername}:${savedPassword}`)}`,
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      });

      if (response.ok) {
        setIsAuthenticated(true);
        setUsername(savedUsername);
      } else if (response.status === 401) {
        // Credentials are invalid, clear them
        localStorage.removeItem('username');
        localStorage.removeItem('password');
        setIsAuthenticated(false);
        setUsername('');
      } else {
        // Other error, still try to use credentials
        setIsAuthenticated(true);
        setUsername(savedUsername);
      }
    } catch (e) {
      // Network error or other issue - assume not authenticated
      console.error('Auth verification failed:', e);
      setIsAuthenticated(false);
      setUsername('');
    }
  };

  const openModal = (modal: ModalType) => {
    if (!isAuthenticated) {
      setAuthModal('login');
      return;
    }
    setActiveModal(modal);
  };

  const closeModal = () => {
    setActiveModal(null);
  };

  const handleFactoryChange = (newFactory: FactoryType) => {
    setFactoryKey(newFactory);
    localStorage.setItem('factoryKey', newFactory);
  };

  const handleLoginSuccess = async () => {
    const savedUsername = localStorage.getItem('username');
    if (savedUsername) {
      setUsername(savedUsername);
      setIsAuthenticated(true);
      setAuthModal(null);
    } else {
      // If no credentials found, show login again
      setAuthModal('login');
    }
  };

  const handleEditFunction = (id: number) => {
    if (!isAuthenticated) {
      setAuthModal('login');
      return;
    }
    setEditingFunctionId(id);
  };

  const handleFunctionUpdated = () => {
    // Refresh function list will be handled by MainPage
  };

  const handleFunctionDeleted = () => {
    // Refresh function list will be handled by MainPage
  };

  const handleLogout = () => {
    clearCredentials();
    setIsAuthenticated(false);
    setUsername('');
    setAuthModal('login');
  };

  return (
    <ThemeProvider>
      <MainPage
        onOpenSettings={() => openModal('settings')}
        onOpenOperations={() => openModal('operations')}
        onOpenDifferentiation={() => openModal('differentiation')}
        onOpenGraph={() => openModal('graph')}
        onOpenComposite={() => openModal('composite')}
        onOpenIntegration={() => openModal('integration')}
        onEditFunction={handleEditFunction}
        onLogin={() => setAuthModal('login')}
        onLogout={handleLogout}
        isAuthenticated={isAuthenticated}
        username={username}
      />
      <SettingsModal
        isOpen={activeModal === 'settings'}
        onClose={closeModal}
        factoryType={factoryKey}
        onFactoryChange={handleFactoryChange}
      />
      <OperationsModal
        isOpen={activeModal === 'operations'}
        onClose={closeModal}
        factoryKey={factoryKey}
      />
      <DifferentiationModal 
        isOpen={activeModal === 'differentiation'} 
        onClose={closeModal}
        factoryKey={factoryKey}
      />
      <FunctionGraphModal
        isOpen={activeModal === 'graph'}
        onClose={closeModal}
      />
      <CompositeFunctionModal
        isOpen={activeModal === 'composite'}
        onClose={closeModal}
        onCreated={handleFunctionUpdated}
      />
      <IntegrationModal
        isOpen={activeModal === 'integration'}
        onClose={closeModal}
      />
      <FunctionEditModal
        isOpen={editingFunctionId !== null}
        onClose={() => setEditingFunctionId(null)}
        functionId={editingFunctionId || 0}
        onFunctionUpdated={handleFunctionUpdated}
        onFunctionDeleted={handleFunctionDeleted}
      />
      <LoginModal
        isOpen={authModal === 'login'}
        onClose={() => setAuthModal(null)}
        onSuccess={handleLoginSuccess}
        onSwitchToRegister={() => setAuthModal('register')}
      />
      <RegisterModal
        isOpen={authModal === 'register'}
        onClose={() => setAuthModal(null)}
        onSuccess={handleLoginSuccess}
        onSwitchToLogin={() => setAuthModal('login')}
      />
      {error && <ErrorModal message={error} onClose={() => setError('')} />}
    </ThemeProvider>
  );
}
