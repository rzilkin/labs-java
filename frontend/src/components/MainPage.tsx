import { useEffect, useState } from 'react';
import { useTheme } from '../ThemeContext';
import { getJson } from '../api';
import { showError } from '../errorManager';

interface FunctionSummary {
    id: number;
    name: string;
    type: string;
    createdAt?: string;
}

interface MainPageProps {
    onOpenSettings: () => void;
    onOpenOperations: () => void;
    onOpenDifferentiation: () => void;
    onOpenGraph: () => void;
    onOpenComposite: () => void;
    onOpenIntegration: () => void;
    onEditFunction: (id: number) => void;
    onLogin: () => void;
    isAuthenticated: boolean;
    username?: string;
}

export function MainPage({
    onOpenSettings,
    onOpenOperations,
    onOpenDifferentiation,
    onOpenGraph,
    onOpenComposite,
    onOpenIntegration,
    onEditFunction,
    onLogin,
    isAuthenticated,
    username,
}: MainPageProps) {
    const { theme, toggleTheme } = useTheme();
    const [functions, setFunctions] = useState<FunctionSummary[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        if (isAuthenticated) {
            loadFunctions();
        }
    }, [isAuthenticated]);

    const loadFunctions = async () => {
        try {
            setLoading(true);
            const data = await getJson<FunctionSummary[]>('/api/v1/functions');
            setFunctions(data || []);
        } catch (e) {
            showError(e);
        } finally {
            setLoading(false);
        }
    };

    const handleFunctionClick = (id: number) => {
        onEditFunction(id);
    };

    return (
        <div style={{ padding: '20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <h1 style={{ margin: 0 }}>Главная страничка</h1>
                <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                    {isAuthenticated ? (
                        <>
                            <span style={{ color: 'var(--text)' }}>Пользователь: {username}</span>
                            <button
                                onClick={toggleTheme}
                                style={{ padding: '8px 16px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                            >
                                {theme === 'dark' ? '☀️ Светлая тема' : '🌙 Тёмная тема'}
                            </button>
                            <button
                                onClick={onOpenSettings}
                                style={{ padding: '10px 20px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                            >
                                Настройки
                            </button>
                        </>
                    ) : (
                        <button
                            onClick={onLogin}
                            style={{ padding: '10px 20px', background: 'var(--btn-bg)', color: 'var(--btn-text)', border: '1px solid var(--border)' }}
                        >
                            Войти
                        </button>
                    )}
                </div>
            </div>

            {isAuthenticated && (
                <>
                    {/* Buttons to open windows */}
                    <div style={{ marginBottom: '30px', display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                        <button
                            onClick={onOpenOperations}
                            style={{ padding: '10px 20px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                        >
                            Поэлементные операции
                        </button>
                        <button
                            onClick={onOpenDifferentiation}
                            style={{ padding: '10px 20px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                        >
                            Дифференцирование
                        </button>
                        <button
                            onClick={onOpenGraph}
                            style={{ padding: '10px 20px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                        >
                            График функции
                        </button>
                        <button
                            onClick={onOpenComposite}
                            style={{ padding: '10px 20px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                        >
                            Составная функция
                        </button>
                        <button
                            onClick={onOpenIntegration}
                            style={{ padding: '10px 20px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                        >
                            Интегрирование
                        </button>
                        <button
                            onClick={loadFunctions}
                            style={{ padding: '10px 20px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                            disabled={loading}
                        >
                            {loading ? 'Загрузка...' : 'Обновить список'}
                        </button>
                    </div>

                    {/* List of existing functions */}
                    <div>
                        <h2 style={{ color: 'var(--text)' }}>Существующие функции</h2>
                        {loading ? (
                            <p style={{ color: 'var(--text)' }}>Загрузка функций...</p>
                        ) : functions.length === 0 ? (
                            <p style={{ color: 'var(--text)' }}>Нет функций. Список пуст.</p>
                        ) : (
                            <table style={{ width: '100%', borderCollapse: 'collapse', color: 'var(--text)' }}>
                                <thead>
                                    <tr>
                                        <th style={{ border: '1px solid var(--border)', padding: '8px', backgroundColor: 'var(--card)', color: 'var(--muted)' }}>ID</th>
                                        <th style={{ border: '1px solid var(--border)', padding: '8px', backgroundColor: 'var(--card)', color: 'var(--muted)' }}>Название</th>
                                        <th style={{ border: '1px solid var(--border)', padding: '8px', backgroundColor: 'var(--card)', color: 'var(--muted)' }}>Тип</th>
                                        <th style={{ border: '1px solid var(--border)', padding: '8px', backgroundColor: 'var(--card)', color: 'var(--muted)' }}>Действия</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {functions.map((func) => (
                                        <tr key={func.id}>
                                            <td style={{ border: '1px solid var(--border)', padding: '8px' }}>{func.id}</td>
                                            <td style={{ border: '1px solid var(--border)', padding: '8px' }}>{func.name}</td>
                                            <td style={{ border: '1px solid var(--border)', padding: '8px' }}>{func.type}</td>
                                            <td style={{ border: '1px solid var(--border)', padding: '8px' }}>
                                                <button
                                                    onClick={() => handleFunctionClick(func.id)}
                                                    style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)', marginRight: '5px' }}
                                                >
                                                    Редактировать
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        )}
                    </div>
                </>
            )}

            {!isAuthenticated && (
                <div style={{ textAlign: 'center', padding: '40px', color: 'var(--text)' }}>
                    <p>Для работы с функциями необходимо войти в систему.</p>
                    <button
                        onClick={onLogin}
                        style={{ padding: '12px 24px', background: 'var(--btn-bg)', color: 'var(--btn-text)', border: '1px solid var(--border)', marginTop: '20px' }}
                    >
                        Войти или зарегистрироваться
                    </button>
                </div>
            )}
        </div>
    );
}
