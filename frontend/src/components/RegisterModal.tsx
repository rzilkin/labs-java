import { useState } from 'react';
import { Modal } from './Modal';
import { postJson, setCredentials } from '../api';
import { showError, showSuccess } from '../errorManager';

interface RegisterModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  onSwitchToLogin: () => void;
}

export function RegisterModal({ isOpen, onClose, onSuccess, onSwitchToLogin }: RegisterModalProps) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();

    // Validate inputs
    if (!username.trim()) {
      showError(new Error('Введите имя пользователя'), true);
      return;
    }
    if (username.trim().length > 50) {
      showError(new Error('Имя пользователя не должно превышать 50 символов'), true);
      return;
    }
    if (!password.trim()) {
      showError(new Error('Введите пароль'), true);
      return;
    }
    if (password.length < 3) {
      showError(new Error('Пароль должен содержать минимум 3 символа'), true);
      return;
    }
    if (password.length > 100) {
      showError(new Error('Пароль не должен превышать 100 символов'), true);
      return;
    }
    if (password !== confirmPassword) {
      showError(new Error('Пароли не совпадают'), true);
      return;
    }

    try {
      setLoading(true);

      // Register user
      await postJson('/api/v1/auth/register', {
        username: username.trim(),
        password: password,
      });

      // Auto-login after successful registration
      // First, save credentials to localStorage
      setCredentials(username.trim(), password);

      // Verify login by calling login endpoint
      const loginResponse = await fetch(`${import.meta.env.VITE_API_BASE_URL}/api/v1/auth/login`, {
        method: 'POST',
        headers: {
          'Authorization': `Basic ${btoa(`${username.trim()}:${password}`)}`,
          'Content-Type': 'application/json',
        },
        credentials: 'include',
      });

      if (!loginResponse.ok) {
        // Registration succeeded but login failed - show error but don't switch to login
        throw new Error('Регистрация успешна, но автоматический вход не удался. Пожалуйста, войдите вручную.');
      }

      // Success - auto-login completed
      showSuccess('Регистрация успешна! Вы автоматически вошли в систему');

      // Clear form
      setUsername('');
      setPassword('');
      setConfirmPassword('');

      // Call onSuccess to update App state (sets isAuthenticated, username, closes modal)
      onSuccess();
      onClose();
    } catch (e) {
      showError(e, true);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Регистрация">
      <form onSubmit={handleRegister}>
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
            style={{ width: '100%', padding: '8px', marginBottom: '10px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
            disabled={loading}
          />
          <input
            type="password"
            placeholder="Подтвердите пароль"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            style={{ width: '100%', padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
            disabled={loading}
          />
        </div>
        <div style={{ display: 'flex', gap: '10px', justifyContent: 'space-between' }}>
          <button
            type="button"
            onClick={onSwitchToLogin}
            style={{ padding: '8px 16px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
            disabled={loading}
          >
            Вход
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
              {loading ? 'Регистрация...' : 'Зарегистрироваться'}
            </button>
          </div>
        </div>
      </form>
    </Modal>
  );
}

