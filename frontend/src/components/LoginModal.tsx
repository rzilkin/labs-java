import { useState } from 'react';
import { Modal } from './Modal';
import { setCredentials } from '../api';
import { showError } from '../errorManager';

interface LoginModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  onSwitchToRegister: () => void;
}

export function LoginModal({ isOpen, onClose, onSuccess, onSwitchToRegister }: LoginModalProps) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim() || !password.trim()) {
      showError(new Error('Введите имя пользователя и пароль'));
      return;
    }

    try {
      setLoading(true);
      // Use Basic Auth - browser will handle the Authorization header
      const response = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/v1/auth/login`, {
        method: 'POST',
        headers: {
          'Authorization': `Basic ${btoa(`${username.trim()}:${password}`)}`,
        },
        credentials: 'include',
      });
      
      if (response.ok) {
        // Store credentials for subsequent API calls
        setCredentials(username.trim(), password);
      }

      if (!response.ok) {
        throw new Error('Неверное имя пользователя или пароль');
      }

      onSuccess();
      onClose();
      setUsername('');
      setPassword('');
    } catch (e) {
      showError(e);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Вход">
      <form onSubmit={handleLogin}>
        <div style={{ marginBottom: '15px' }}>
          <input
            type="text"
            placeholder="Имя пользователя"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            style={{ width: '100%', padding: '8px', marginBottom: '10px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
            disabled={loading}
            autoFocus
          />
          <input
            type="password"
            placeholder="Пароль"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            style={{ width: '100%', padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
            disabled={loading}
          />
        </div>
        <div style={{ display: 'flex', gap: '10px', justifyContent: 'space-between' }}>
          <button
            type="button"
            onClick={onSwitchToRegister}
            style={{ padding: '8px 16px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
            disabled={loading}
          >
            Регистрация
          </button>
          <div style={{ display: 'flex', gap: '10px' }}>
            <button
              type="button"
              onClick={onClose}
              style={{ padding: '8px 16px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
              disabled={loading}
            >
              Отмена
            </button>
            <button
              type="submit"
              style={{ padding: '8px 16px', background: 'var(--btn-bg)', color: 'var(--btn-text)', border: '1px solid var(--border)' }}
              disabled={loading}
            >
              {loading ? 'Вход...' : 'Войти'}
            </button>
          </div>
        </div>
      </form>
    </Modal>
  );
}

