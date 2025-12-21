import { useState } from 'react';
import { Modal } from './Modal';
import { postJson } from '../api';
import { showError } from '../errorManager';

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
    if (!username.trim() || !password.trim()) {
      showError(new Error('Введите имя пользователя и пароль'));
      return;
    }

    if (password !== confirmPassword) {
      showError(new Error('Пароли не совпадают'));
      return;
    }

    if (password.length < 3) {
      showError(new Error('Пароль должен содержать минимум 3 символа'));
      return;
    }

    try {
      setLoading(true);
      await postJson('/api/v1/auth/register', {
        username: username.trim(),
        password: password,
      });

      onSuccess();
      onClose();
      setUsername('');
      setPassword('');
      setConfirmPassword('');
      // Switch to login after successful registration
      setTimeout(() => {
        onSwitchToLogin();
      }, 100);
    } catch (e) {
      showError(e);
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

