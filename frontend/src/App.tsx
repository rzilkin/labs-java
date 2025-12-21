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
    // Check if user is authenticated (you might want to verify with backend)
    const savedUsername = localStorage.getItem('username');
    if (savedUsername) {
      setUsername(savedUsername);
      setIsAuthenticated(true);
    }
  }, []);

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

  const handleLoginSuccess = () => {
    const savedUsername = localStorage.getItem('username') || 'Пользователь';
    setUsername(savedUsername);
    setIsAuthenticated(true);
    setAuthModal(null);
  };
  
  // Set up Basic Auth for all requests
  useEffect(() => {
    const storedUsername = localStorage.getItem('username');
    const storedPassword = localStorage.getItem('password');
    if (storedUsername && storedPassword) {
      // Note: In a real app, you'd use a more secure method, but for Basic Auth demo:
      // The browser handles Basic Auth automatically on subsequent requests
      setUsername(storedUsername);
      setIsAuthenticated(true);
    }
  }, []);

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
