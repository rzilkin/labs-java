import { useEffect, useState, useRef } from 'react';
import { useTheme } from '../ThemeContext';
import { getJson, postJson, deleteRequest } from '../api';
import { showError, setToastHandler, showSuccess } from '../errorManager';
import { ToastContainer } from './ToastContainer';
import { Modal } from './Modal';
import { validateFunctionName, validatePoints, validatePointCount, validateRange, validateExpression, validateComponentIds, validateNumber } from '../utils/validation';

interface FunctionSummary {
    id: number;
    name: string;
    type: string;
    createdAt?: string;
}

interface Point {
    x: number;
    y: number;
}

interface FunctionFull {
    summary: {
        id: number;
        name: string;
        type: string;
    };
    points?: Point[];
    analyticExpression?: string;
    components?: number[];
}

// Cache for function expressions (for composition preview)
const functionExpressionCache = new Map<number, string>();

interface MainPageProps {
    onOpenSettings: () => void;
    onOpenOperations: () => void;
    onOpenDifferentiation: () => void;
    onOpenGraph: () => void;
    onOpenIntegration: () => void;
    onEditFunction: (id: number) => void;
    onLogin: () => void;
    onLogout: () => void;
    isAuthenticated: boolean;
    username?: string;
}

export function MainPage({
    onOpenSettings,
    onOpenOperations,
    onOpenDifferentiation,
    onOpenGraph,
    onOpenIntegration,
    onEditFunction,
    onLogin,
    onLogout,
    isAuthenticated,
    username,
}: MainPageProps) {
    const { theme, toggleTheme } = useTheme();
    const [functions, setFunctions] = useState<FunctionSummary[]>([]);
    const [loading, setLoading] = useState(false);
    const [savingId, setSavingId] = useState<number | null>(null);
    const [loadingFile, setLoadingFile] = useState(false);
    const fileInputRef = useRef<HTMLInputElement>(null);
    const [toasts, setToasts] = useState<Array<{ id: string; message: string; type: 'success' | 'error' | 'info' }>>([]);

    // Function creation state
    const [creationType, setCreationType] = useState<'ANALYTIC' | 'TABULATED_MANUAL' | 'TABULATED_FROM_FUNCTION' | 'COMPOSITE'>('ANALYTIC');
    const [creationName, setCreationName] = useState('');
    const [creating, setCreating] = useState(false);

    // ANALYTIC
    const [analyticExpression, setAnalyticExpression] = useState('');

    // TABULATED_MANUAL
    const [manualPointCount, setManualPointCount] = useState('');
    const [manualPoints, setManualPoints] = useState<Array<{ x: string; y: string }>>([]);

    // TABULATED_FROM_FUNCTION
    const [fromFunctionSourceId, setFromFunctionSourceId] = useState<number | null>(null);
    const [fromFunctionCount, setFromFunctionCount] = useState('10');
    const [fromFunctionFrom, setFromFunctionFrom] = useState('0');
    const [fromFunctionTo, setFromFunctionTo] = useState('10');

    // COMPOSITE
    const [compositeSelectedIds, setCompositeSelectedIds] = useState<number[]>([]);

    // Delete all functions
    const [showDeleteAllConfirm, setShowDeleteAllConfirm] = useState(false);
    const [deletingAll, setDeletingAll] = useState(false);

    const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
        const id = Math.random().toString(36).substr(2, 9);
        setToasts(prev => [...prev, { id, message, type }]);
    };

    const removeToast = (id: string) => {
        setToasts(prev => prev.filter(t => t.id !== id));
    };

    // Register toast handler for global error manager
    useEffect(() => {
        setToastHandler(showToast);
        return () => setToastHandler(null);
    }, []);

    useEffect(() => {
        if (isAuthenticated) {
            loadFunctions();
        }
    }, [isAuthenticated]);

    // Refresh when function is created (via key change from parent)
    // The parent will change the key to force a remount, which will trigger useEffect

    const loadFunctions = async () => {
        try {
            setLoading(true);
            const data = await getJson<FunctionSummary[]>('/api/v1/functions');
            setFunctions(data || []);
        } catch (e) {
            showError(e, true); // Use toast for API errors
        } finally {
            setLoading(false);
        }
    };

    const handleFunctionClick = (id: number) => {
        onEditFunction(id);
    };

    // Function creation handlers
    const handleBuildManualTable = () => {
        try {
            const countError = validatePointCount(manualPointCount);
            if (countError) {
                showError(new Error(countError), true);
                return;
            }
            const count = parseInt(manualPointCount, 10);
            if (count > 500) {
                showError(new Error('Максимум 500 точек для ручного ввода'), true);
                return;
            }
            setManualPoints(Array.from({ length: count }, () => ({ x: '', y: '' })));
        } catch (e) {
            showError(e, true);
        }
    };

    const handleManualPointChange = (index: number, key: 'x' | 'y', value: string) => {
        const newPoints = [...manualPoints];
        newPoints[index] = { ...newPoints[index], [key]: value };
        setManualPoints(newPoints);
    };

    const handleCreateFunction = async () => {
        // Validate name
        const nameError = validateFunctionName(creationName);
        if (nameError) {
            showError(new Error(nameError), true);
            return;
        }

        try {
            setCreating(true);

            if (creationType === 'ANALYTIC') {
                const exprError = validateExpression(analyticExpression);
                if (exprError) {
                    showError(new Error(exprError), true);
                    return;
                }

                const created = await postJson<FunctionFull>('/api/v1/functions/analytic', {
                    name: creationName.trim(),
                    expression: analyticExpression.trim(),
                });

                if (!created || !created.summary) {
                    throw new Error('Не удалось создать аналитическую функцию');
                }

                showSuccess(`Аналитическая функция "${created.summary.name}" успешно создана (ID: ${created.summary.id})`);
            } else if (creationType === 'TABULATED_MANUAL') {
                if (manualPoints.length === 0) {
                    showError(new Error('Сначала создайте таблицу точек'), true);
                    return;
                }

                // Parse and validate points
                const numericPoints: Point[] = manualPoints.map((p, i) => {
                    const xError = validateNumber(p.x, `X[${i}]`);
                    if (xError) throw new Error(xError);
                    const yError = validateNumber(p.y, `Y[${i}]`);
                    if (yError) throw new Error(yError);
                    return { x: parseFloat(p.x), y: parseFloat(p.y) };
                });

                const pointsError = validatePoints(numericPoints);
                if (pointsError) {
                    showError(new Error(pointsError), true);
                    return;
                }

                const created = await postJson<FunctionFull>('/api/v1/functions/tabulated/manual', {
                    name: creationName.trim(),
                    points: numericPoints,
                });

                if (!created || !created.summary) {
                    throw new Error('Не удалось создать табулированную функцию');
                }

                showSuccess(`Табулированная функция "${created.summary.name}" успешно создана (ID: ${created.summary.id})`);
            } else if (creationType === 'TABULATED_FROM_FUNCTION') {
                if (!fromFunctionSourceId) {
                    showError(new Error('Выберите функцию-источник'), true);
                    return;
                }

                const countError = validatePointCount(fromFunctionCount);
                if (countError) {
                    showError(new Error(countError), true);
                    return;
                }

                const rangeError = validateRange(fromFunctionFrom, fromFunctionTo);
                if (rangeError) {
                    showError(new Error(rangeError), true);
                    return;
                }

                const count = parseInt(fromFunctionCount, 10);
                if (count > 5000) {
                    showError(new Error('Максимум 5000 точек'), true);
                    return;
                }

                const created = await postJson<FunctionFull>('/api/v1/functions/tabulated/from-function', {
                    name: creationName.trim(),
                    sourceFunctionId: fromFunctionSourceId,
                    count: count,
                    from: parseFloat(fromFunctionFrom),
                    to: parseFloat(fromFunctionTo),
                });

                if (!created || !created.summary) {
                    throw new Error('Не удалось создать табулированную функцию из другой функции');
                }

                showSuccess(`Табулированная функция "${created.summary.name}" успешно создана (ID: ${created.summary.id})`);
            } else if (creationType === 'COMPOSITE') {
                const componentsError = validateComponentIds(compositeSelectedIds);
                if (componentsError) {
                    showError(new Error(componentsError), true);
                    return;
                }

                const created = await postJson<FunctionFull>('/api/v1/functions/composite', {
                    name: creationName.trim(),
                    componentIds: compositeSelectedIds,
                });

                if (!created || !created.summary) {
                    throw new Error('Не удалось создать составную функцию');
                }

                showSuccess(`Составная функция "${created.summary.name}" успешно создана (ID: ${created.summary.id})`);
            }

            // Reset form
            setCreationName('');
            setAnalyticExpression('');
            setManualPointCount('');
            setManualPoints([]);
            setFromFunctionSourceId(null);
            setFromFunctionCount('10');
            setFromFunctionFrom('0');
            setFromFunctionTo('10');
            setCompositeSelectedIds([]);

            // Refresh function list
            await loadFunctions();
        } catch (e) {
            showError(e, true);
        } finally {
            setCreating(false);
        }
    };

    const handleToggleCompositeComponent = (id: number) => {
        setCompositeSelectedIds(prev => {
            if (prev.includes(id)) {
                return prev.filter(x => x !== id);
            } else {
                return [...prev, id];
            }
        });
    };

    const handleCompositeMoveUp = (index: number) => {
        if (index === 0) return;
        setCompositeSelectedIds(prev => {
            const newIds = [...prev];
            [newIds[index - 1], newIds[index]] = [newIds[index], newIds[index - 1]];
            return newIds;
        });
    };

    const handleCompositeMoveDown = (index: number) => {
        setCompositeSelectedIds(prev => {
            if (index >= prev.length - 1) return prev;
            const newIds = [...prev];
            [newIds[index], newIds[index + 1]] = [newIds[index + 1], newIds[index]];
            return newIds;
        });
    };

    const handleCompositeRemove = (index: number) => {
        setCompositeSelectedIds(prev => prev.filter((_, i) => i !== index));
    };

    // Get function display name for composition preview
    const getFunctionDisplayName = (funcId: number): string => {
        const func = functions.find(f => f.id === funcId);
        if (!func) return `f${funcId}`;
        
        // Check cache for expression
        const cachedExpr = functionExpressionCache.get(funcId);
        if (cachedExpr) {
            return cachedExpr;
        }
        
        // For display, use name or try to fetch expression
        return func.name;
    };

    // Load function expression for preview (async)
    const loadFunctionExpression = async (funcId: number) => {
        if (functionExpressionCache.has(funcId)) return;
        
        try {
            const funcData = await getJson<FunctionFull>(`/api/v1/functions/${funcId}`);
            if (funcData.analyticExpression) {
                functionExpressionCache.set(funcId, funcData.analyticExpression);
            } else {
                functionExpressionCache.set(funcId, funcData.summary.name);
            }
        } catch {
            // Ignore errors, use name as fallback
        }
    };

    // Load expressions for all selected components
    useEffect(() => {
        compositeSelectedIds.forEach(id => {
            loadFunctionExpression(id);
        });
    }, [compositeSelectedIds]);

    // Generate composition preview string
    const getCompositionPreview = (): string => {
        if (compositeSelectedIds.length === 0) return '';
        if (compositeSelectedIds.length === 1) {
            const name = getFunctionDisplayName(compositeSelectedIds[0]);
            return `f(x) = ${name}(x)`;
        }

        // Build nested composition: f_n(...f_2(f_1(x))...)
        // Components are applied in order: first component gets x, its result goes to second, etc.
        let result = 'x';
        for (let i = 0; i < compositeSelectedIds.length; i++) {
            const func = functions.find(f => f.id === compositeSelectedIds[i]);
            const expr = functionExpressionCache.get(compositeSelectedIds[i]);
            
            if (expr && func?.type === 'ANALYTIC') {
                // Replace 'x' in expression with current result
                // Simple replacement for preview
                result = expr.replace(/\bx\b/g, `(${result})`);
            } else {
                const name = func?.name || `f${compositeSelectedIds[i]}`;
                result = `${name}(${result})`;
            }
        }

        return `f(x) = ${result}`;
    };

    const handleDeleteAllFunctions = async () => {
        if (functions.length === 0) {
            showError(new Error('Нет функций для удаления'), true);
            return;
        }

        setShowDeleteAllConfirm(true);
    };

    const confirmDeleteAll = async () => {
        try {
            setDeletingAll(true);
            setShowDeleteAllConfirm(false);

            // Delete all functions sequentially
            let deletedCount = 0;
            let failedCount = 0;

            for (const func of functions) {
                try {
                    await deleteRequest(`/api/v1/functions/${func.id}`);
                    deletedCount++;
                } catch (e) {
                    console.error(`Failed to delete function ${func.id}:`, e);
                    failedCount++;
                    // Continue deleting other functions even if one fails
                }
            }

            // Refresh the function list
            await loadFunctions();

            if (failedCount === 0) {
                showSuccess(`Все функции (${deletedCount} шт.) успешно удалены`);
            } else {
                showError(new Error(`Удалено ${deletedCount} функций, не удалось удалить ${failedCount}`), true);
            }
        } catch (e) {
            showError(e, true);
        } finally {
            setDeletingAll(false);
        }
    };

    const handleSaveFunction = async (id: number) => {
        try {
            setSavingId(id);
            // Use export endpoint to get full function data
            const functionData = await getJson<FunctionFull>(`/api/v1/functions/${id}/export`);

            if (!functionData || !functionData.summary) {
                throw new Error('Не удалось получить данные функции');
            }

            // Prepare export data (remove id from summary as it will be assigned on import)
            const exportData = {
                summary: {
                    name: functionData.summary.name,
                    type: functionData.summary.type,
                },
                points: functionData.points || [],
                analyticExpression: functionData.analyticExpression,
                components: functionData.components || [],
            };

            // Create JSON blob
            const json = JSON.stringify(exportData, null, 2);
            const blob = new Blob([json], { type: 'application/json' });
            const url = URL.createObjectURL(blob);

            // Create download link
            const a = document.createElement('a');
            a.href = url;
            a.download = `${functionData.summary.name.replace(/[^a-zA-Z0-9]/g, '_')}_${id}.json`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(url);
            showSuccess('Функция успешно сохранена');
        } catch (e) {
            showError(e, true);
        } finally {
            setSavingId(null);
        }
    };

    const handleLoadFunction = () => {
        fileInputRef.current?.click();
    };

    const handleFileChange = async (event: React.ChangeEvent<HTMLInputElement>) => {
        const file = event.target.files?.[0];
        if (!file) return;

        // Reset input
        if (fileInputRef.current) {
            fileInputRef.current.value = '';
        }

        try {
            setLoadingFile(true);
            const text = await file.text();
            let functionData: FunctionFull;

            try {
                functionData = JSON.parse(text);
            } catch (e) {
                throw new Error('Неверный формат JSON файла');
            }

            // Validate structure
            if (!functionData.summary || !functionData.summary.name || !functionData.summary.type) {
                throw new Error('Файл должен содержать объект summary с полями name и type');
            }

            // Validate function name
            const nameError = validateFunctionName(functionData.summary.name);
            if (nameError) {
                throw new Error(nameError);
            }

            // Validate points if present
            if (functionData.points && functionData.points.length > 0) {
                const pointsError = validatePoints(functionData.points);
                if (pointsError) {
                    throw new Error(pointsError);
                }
            }

            // Import function
            const imported = await postJson<FunctionFull>('/api/v1/functions/import', functionData);

            if (!imported || !imported.summary) {
                throw new Error('Не удалось импортировать функцию');
            }

            // Show success toast
            showToast(`Функция "${imported.summary.name}" успешно импортирована (ID: ${imported.summary.id})`, 'success');

            // Refresh function list
            await loadFunctions();
        } catch (e) {
            showError(e, true);
        } finally {
            setLoadingFile(false);
        }
    };

    return (
        <div style={{ padding: '20px', minHeight: '100vh' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <h1 style={{ margin: 0 }}>Главная страничка</h1>
                <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                    {isAuthenticated ? (
                        <>
                            <span style={{ color: 'var(--text)' }}>Пользователь: {username}</span>
                            <button
                                onClick={(e) => toggleTheme(e)}
                                style={{
                                    padding: '8px 16px',
                                    background: 'var(--btn2-bg)',
                                    color: 'var(--btn2-text)',
                                    border: '1px solid var(--border)',
                                    position: 'relative',
                                    overflow: 'hidden',
                                    transition: 'transform 0.2s',
                                }}
                                onMouseDown={(e) => {
                                    e.currentTarget.style.transform = 'scale(0.95)';
                                }}
                                onMouseUp={(e) => {
                                    e.currentTarget.style.transform = 'scale(1)';
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.transform = 'scale(1)';
                                }}
                            >
                                {theme === 'dark' ? '☀️ Светлая тема' : '🌙 Тёмная тема'}
                            </button>
                            <button
                                onClick={onOpenSettings}
                                style={{ padding: '10px 20px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                            >
                                Настройки
                            </button>
                            <button
                                onClick={onLogout}
                                style={{ padding: '10px 20px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                            >
                                Выйти
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
                    {/* Function Creation Section */}
                    <div style={{ marginBottom: '30px', padding: '20px', background: 'var(--card)', border: '1px solid var(--border)', borderRadius: '8px' }}>
                        <h2 style={{ color: 'var(--text)', marginTop: 0, marginBottom: '20px' }}>Создать новую функцию</h2>

                        {/* Type Selection */}
                        <div style={{ marginBottom: '20px' }}>
                            <label style={{ color: 'var(--text)', display: 'block', marginBottom: '8px' }}>Тип функции:</label>
                            <select
                                value={creationType}
                                onChange={(e) => {
                                    setCreationType(e.target.value as any);
                                    // Reset form when type changes
                                    setCreationName('');
                                    setAnalyticExpression('');
                                    setManualPointCount('');
                                    setManualPoints([]);
                                    setFromFunctionSourceId(null);
                                    setCompositeSelectedIds([]);
                                }}
                                style={{ width: '100%', padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                            >
                                <option value="ANALYTIC">Аналитическая (ANALYTIC)</option>
                                <option value="TABULATED_MANUAL">Табулированная вручную (TABULATED_MANUAL)</option>
                                <option value="TABULATED_FROM_FUNCTION">Табулированная из функции (TABULATED_FROM_FUNCTION)</option>
                                <option value="COMPOSITE">Составная (COMPOSITE)</option>
                            </select>
                        </div>

                        {/* Name Input */}
                        <div style={{ marginBottom: '20px' }}>
                            <label style={{ color: 'var(--text)', display: 'block', marginBottom: '8px' }}>Имя функции:</label>
                            <input
                                type="text"
                                value={creationName}
                                onChange={(e) => setCreationName(e.target.value)}
                                placeholder="Введите имя функции"
                                style={{ width: '100%', padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                            />
                        </div>

                        {/* ANALYTIC Form */}
                        {creationType === 'ANALYTIC' && (
                            <div style={{ marginBottom: '20px' }}>
                                <label style={{ color: 'var(--text)', display: 'block', marginBottom: '8px' }}>Выражение:</label>
                                <input
                                    type="text"
                                    value={analyticExpression}
                                    onChange={(e) => setAnalyticExpression(e.target.value)}
                                    placeholder="Например: x^2 + sin(x)"
                                    style={{ width: '100%', padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                                />
                                <div style={{ color: 'var(--muted)', fontSize: '12px', marginTop: '5px' }}>
                                    Используйте x как переменную. Поддерживаются функции: sin, cos, tan, exp, log, sqrt и др.
                                </div>
                            </div>
                        )}

                        {/* TABULATED_MANUAL Form */}
                        {creationType === 'TABULATED_MANUAL' && (
                            <div style={{ marginBottom: '20px' }}>
                                <div style={{ marginBottom: '15px' }}>
                                    <div style={{ display: 'flex', gap: '5px', marginBottom: '10px' }}>
                                        <input
                                            type="text"
                                            value={manualPointCount}
                                            onChange={(e) => setManualPointCount(e.target.value)}
                                            placeholder="Количество точек"
                                            style={{ flex: 1, padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                                        />
                                        <button
                                            onClick={handleBuildManualTable}
                                            style={{ padding: '8px 16px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                                        >
                                            Показать таблицу
                                        </button>
                                    </div>
                                </div>

                                {manualPoints.length > 0 && (
                                    <div style={{ maxHeight: '300px', overflowY: 'auto', marginBottom: '15px', border: '1px solid var(--border)', borderRadius: '4px' }}>
                                        <table style={{ width: '100%', borderCollapse: 'collapse', color: 'var(--text)' }}>
                                            <thead>
                                                <tr>
                                                    <th style={{ border: '1px solid var(--border)', padding: '8px', backgroundColor: 'var(--card)', color: 'var(--muted)' }}>X</th>
                                                    <th style={{ border: '1px solid var(--border)', padding: '8px', backgroundColor: 'var(--card)', color: 'var(--muted)' }}>Y</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                {manualPoints.map((p, i) => (
                                                    <tr key={i}>
                                                        <td style={{ border: '1px solid var(--border)', padding: '8px' }}>
                                                            <input
                                                                value={p.x}
                                                                onChange={(e) => handleManualPointChange(i, 'x', e.target.value)}
                                                                placeholder="x"
                                                                style={{ width: '100%', padding: '4px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                                                            />
                                                        </td>
                                                        <td style={{ border: '1px solid var(--border)', padding: '8px' }}>
                                                            <input
                                                                value={p.y}
                                                                onChange={(e) => handleManualPointChange(i, 'y', e.target.value)}
                                                                placeholder="y"
                                                                style={{ width: '100%', padding: '4px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                                                            />
                                                        </td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </table>
                                    </div>
                                )}
                            </div>
                        )}

                        {/* TABULATED_FROM_FUNCTION Form */}
                        {creationType === 'TABULATED_FROM_FUNCTION' && (
                            <div style={{ marginBottom: '20px' }}>
                                <div style={{ marginBottom: '10px' }}>
                                    <label style={{ color: 'var(--text)', display: 'block', marginBottom: '8px' }}>Функция-источник:</label>
                                    <select
                                        value={fromFunctionSourceId || ''}
                                        onChange={(e) => setFromFunctionSourceId(e.target.value ? Number(e.target.value) : null)}
                                        style={{ width: '100%', padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                                    >
                                        <option value="">Выберите функцию</option>
                                        {functions.filter(f => f.type === 'TABULATED').map(f => (
                                            <option key={f.id} value={f.id}>
                                                {f.name} (ID: {f.id})
                                            </option>
                                        ))}
                                    </select>
                                </div>
                                <div style={{ marginBottom: '10px' }}>
                                    <label style={{ color: 'var(--text)', display: 'block', marginBottom: '8px' }}>Количество точек:</label>
                                    <input
                                        type="number"
                                        value={fromFunctionCount}
                                        onChange={(e) => setFromFunctionCount(e.target.value)}
                                        min="2"
                                        max="5000"
                                        style={{ width: '100%', padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                                    />
                                </div>
                                <div style={{ display: 'flex', gap: '10px', marginBottom: '10px' }}>
                                    <div style={{ flex: 1 }}>
                                        <label style={{ color: 'var(--text)', display: 'block', marginBottom: '8px' }}>От (xFrom):</label>
                                        <input
                                            type="number"
                                            value={fromFunctionFrom}
                                            onChange={(e) => setFromFunctionFrom(e.target.value)}
                                            style={{ width: '100%', padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                                        />
                                    </div>
                                    <div style={{ flex: 1 }}>
                                        <label style={{ color: 'var(--text)', display: 'block', marginBottom: '8px' }}>До (xTo):</label>
                                        <input
                                            type="number"
                                            value={fromFunctionTo}
                                            onChange={(e) => setFromFunctionTo(e.target.value)}
                                            style={{ width: '100%', padding: '8px', background: 'var(--input-bg)', color: 'var(--input-text)', border: '1px solid var(--border)' }}
                                        />
                                    </div>
                                </div>
                            </div>
                        )}

                        {/* COMPOSITE Form */}
                        {creationType === 'COMPOSITE' && (
                            <div style={{ marginBottom: '20px' }}>
                                {/* Available Functions to Select */}
                                <label style={{ color: 'var(--text)', display: 'block', marginBottom: '8px' }}>
                                    Выберите компоненты (минимум 2):
                                </label>
                                <div style={{ maxHeight: '200px', overflowY: 'auto', border: '1px solid var(--border)', borderRadius: '4px', padding: '10px', marginBottom: '15px' }}>
                                    {functions.length === 0 ? (
                                        <div style={{ color: 'var(--text)' }}>Нет доступных функций</div>
                                    ) : (
                                        functions.map(func => (
                                            <div
                                                key={func.id}
                                                style={{
                                                    padding: '8px',
                                                    marginBottom: '5px',
                                                    border: '1px solid var(--border)',
                                                    borderRadius: '4px',
                                                    cursor: 'pointer',
                                                    background: compositeSelectedIds.includes(func.id) ? 'var(--btn2-bg)' : 'var(--card)',
                                                    color: 'var(--text)',
                                                }}
                                                onClick={() => handleToggleCompositeComponent(func.id)}
                                            >
                                                <input
                                                    type="checkbox"
                                                    checked={compositeSelectedIds.includes(func.id)}
                                                    onChange={() => handleToggleCompositeComponent(func.id)}
                                                    style={{ marginRight: '8px' }}
                                                />
                                                {func.name} (ID: {func.id}, Тип: {func.type})
                                            </div>
                                        ))
                                    )}
                                </div>

                                {/* Ordered Component List */}
                                {compositeSelectedIds.length > 0 && (
                                    <div style={{ marginBottom: '15px' }}>
                                        <label style={{ color: 'var(--text)', display: 'block', marginBottom: '8px', fontWeight: 'bold' }}>
                                            Порядок применения функций:
                                        </label>
                                        <div style={{ 
                                            padding: '10px', 
                                            background: 'rgba(59, 130, 246, 0.1)', 
                                            border: '1px solid rgba(59, 130, 246, 0.3)', 
                                            borderRadius: '4px',
                                            marginBottom: '10px',
                                            fontSize: '13px',
                                            color: 'var(--text)'
                                        }}>
                                            ℹ️ Функции применяются последовательно: результат первой функции подаётся на вход второй, и т.д.
                                        </div>
                                        
                                        <div style={{ border: '1px solid var(--border)', borderRadius: '4px', padding: '10px' }}>
                                            {compositeSelectedIds.map((id, index) => {
                                                const func = functions.find(f => f.id === id);
                                                const expr = functionExpressionCache.get(id);
                                                return (
                                                    <div
                                                        key={`${id}-${index}`}
                                                        style={{
                                                            display: 'flex',
                                                            alignItems: 'center',
                                                            gap: '10px',
                                                            padding: '10px',
                                                            marginBottom: index < compositeSelectedIds.length - 1 ? '8px' : '0',
                                                            background: 'var(--card)',
                                                            border: '1px solid var(--border)',
                                                            borderRadius: '4px',
                                                        }}
                                                    >
                                                        <span style={{ 
                                                            color: 'var(--btn-bg)', 
                                                            fontWeight: 'bold',
                                                            minWidth: '24px',
                                                            textAlign: 'center'
                                                        }}>
                                                            {index + 1}
                                                        </span>
                                                        <div style={{ flex: 1, color: 'var(--text)' }}>
                                                            <div style={{ fontWeight: 500 }}>
                                                                {func?.name || `ID: ${id}`}
                                                            </div>
                                                            {expr && func?.type === 'ANALYTIC' && (
                                                                <div style={{ fontSize: '12px', color: 'var(--muted)', fontFamily: 'monospace' }}>
                                                                    {expr}
                                                                </div>
                                                            )}
                                                            {func?.type === 'TABULATED' && (
                                                                <div style={{ fontSize: '12px', color: 'var(--muted)' }}>
                                                                    (табулированная)
                                                                </div>
                                                            )}
                                                        </div>
                                                        <div style={{ display: 'flex', gap: '4px' }}>
                                                            <button
                                                                onClick={() => handleCompositeMoveUp(index)}
                                                                disabled={index === 0}
                                                                title="Переместить вверх"
                                                                style={{
                                                                    padding: '6px 10px',
                                                                    background: 'var(--btn2-bg)',
                                                                    color: 'var(--btn2-text)',
                                                                    border: '1px solid var(--border)',
                                                                    cursor: index === 0 ? 'not-allowed' : 'pointer',
                                                                    opacity: index === 0 ? 0.4 : 1,
                                                                    borderRadius: '4px',
                                                                    fontSize: '14px'
                                                                }}
                                                            >
                                                                ↑
                                                            </button>
                                                            <button
                                                                onClick={() => handleCompositeMoveDown(index)}
                                                                disabled={index === compositeSelectedIds.length - 1}
                                                                title="Переместить вниз"
                                                                style={{
                                                                    padding: '6px 10px',
                                                                    background: 'var(--btn2-bg)',
                                                                    color: 'var(--btn2-text)',
                                                                    border: '1px solid var(--border)',
                                                                    cursor: index === compositeSelectedIds.length - 1 ? 'not-allowed' : 'pointer',
                                                                    opacity: index === compositeSelectedIds.length - 1 ? 0.4 : 1,
                                                                    borderRadius: '4px',
                                                                    fontSize: '14px'
                                                                }}
                                                            >
                                                                ↓
                                                            </button>
                                                            <button
                                                                onClick={() => handleCompositeRemove(index)}
                                                                title="Удалить"
                                                                style={{
                                                                    padding: '6px 10px',
                                                                    background: '#dc2626',
                                                                    color: 'white',
                                                                    border: '1px solid #dc2626',
                                                                    cursor: 'pointer',
                                                                    borderRadius: '4px',
                                                                    fontSize: '14px'
                                                                }}
                                                            >
                                                                ✕
                                                            </button>
                                                        </div>
                                                    </div>
                                                );
                                            })}
                                        </div>

                                        {/* Composition Preview */}
                                        {compositeSelectedIds.length >= 2 && (
                                            <div style={{ 
                                                marginTop: '15px',
                                                padding: '15px',
                                                background: 'var(--card)',
                                                border: '2px solid var(--btn-bg)',
                                                borderRadius: '8px'
                                            }}>
                                                <div style={{ 
                                                    color: 'var(--muted)', 
                                                    fontSize: '12px', 
                                                    marginBottom: '8px',
                                                    textTransform: 'uppercase',
                                                    letterSpacing: '0.5px'
                                                }}>
                                                    Результат композиции:
                                                </div>
                                                <div style={{ 
                                                    color: 'var(--text)', 
                                                    fontSize: '16px',
                                                    fontFamily: 'monospace',
                                                    wordBreak: 'break-word',
                                                    lineHeight: '1.5'
                                                }}>
                                                    {getCompositionPreview()}
                                                </div>
                                            </div>
                                        )}

                                        <div style={{ marginTop: '10px', color: 'var(--muted)', fontSize: '13px' }}>
                                            Выбрано компонентов: {compositeSelectedIds.length}
                                            {compositeSelectedIds.length < 2 && (
                                                <span style={{ color: '#dc2626', marginLeft: '10px' }}>
                                                    (необходимо минимум 2)
                                                </span>
                                            )}
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}

                        {/* Create Button */}
                        <div>
                            <button
                                onClick={handleCreateFunction}
                                disabled={creating || !creationName.trim()}
                                style={{
                                    padding: '10px 20px',
                                    background: 'var(--btn-bg)',
                                    color: 'var(--btn-text)',
                                    border: '1px solid var(--border)',
                                    cursor: (creating || !creationName.trim()) ? 'not-allowed' : 'pointer',
                                    opacity: (creating || !creationName.trim()) ? 0.5 : 1,
                                }}
                            >
                                {creating ? 'Создание...' : 'Создать'}
                            </button>
                        </div>
                    </div>

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
                        <button
                            onClick={handleDeleteAllFunctions}
                            disabled={loading || deletingAll || functions.length === 0}
                            style={{
                                padding: '10px 20px',
                                background: '#dc2626',
                                color: 'white',
                                border: '1px solid #dc2626',
                                cursor: (loading || deletingAll || functions.length === 0) ? 'not-allowed' : 'pointer',
                                opacity: (loading || deletingAll || functions.length === 0) ? 0.5 : 1,
                            }}
                        >
                            {deletingAll ? 'Удаление...' : 'Удалить все функции'}
                        </button>
                    </div>

                    {/* List of existing functions */}
                    <div>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px' }}>
                            <h2 style={{ color: 'var(--text)', margin: 0 }}>Существующие функции</h2>
                            <button
                                onClick={handleLoadFunction}
                                disabled={loadingFile}
                                style={{
                                    padding: '8px 16px',
                                    background: 'var(--btn-bg)',
                                    color: 'var(--btn-text)',
                                    border: '1px solid var(--border)',
                                    opacity: loadingFile ? 0.6 : 1,
                                    cursor: loadingFile ? 'not-allowed' : 'pointer'
                                }}
                            >
                                {loadingFile ? 'Загрузка...' : 'Загрузить'}
                            </button>
                            <input
                                ref={fileInputRef}
                                type="file"
                                accept=".json"
                                onChange={handleFileChange}
                                style={{ display: 'none' }}
                            />
                        </div>
                        {loading ? (
                            <p style={{ color: 'var(--text)' }}>Загрузка функций...</p>
                        ) : functions.length === 0 ? (
                            <p style={{ color: 'var(--text)' }}>Нет функций. Список пуст.</p>
                        ) : (
                            <div style={{ overflowX: 'auto' }}>
                                <table style={{ width: '100%', borderCollapse: 'collapse', color: 'var(--text)', minWidth: '600px' }}>
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
                                                    <div style={{ display: 'flex', gap: '5px', flexWrap: 'wrap' }}>
                                                        <button
                                                            onClick={() => handleFunctionClick(func.id)}
                                                            style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                                                        >
                                                            Редактировать
                                                        </button>
                                                        <button
                                                            onClick={() => handleSaveFunction(func.id)}
                                                            disabled={savingId === func.id}
                                                            style={{
                                                                padding: '5px 10px',
                                                                background: 'var(--btn2-bg)',
                                                                color: 'var(--btn2-text)',
                                                                border: '1px solid var(--border)',
                                                                opacity: savingId === func.id ? 0.6 : 1,
                                                                cursor: savingId === func.id ? 'not-allowed' : 'pointer'
                                                            }}
                                                        >
                                                            {savingId === func.id ? 'Сохранение...' : 'Сохранить'}
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
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
            <ToastContainer toasts={toasts} onRemove={removeToast} />

            {/* Delete All Confirmation Modal */}
            <Modal isOpen={showDeleteAllConfirm} onClose={() => setShowDeleteAllConfirm(false)} title="Подтверждение удаления">
                <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
                    <div style={{ color: 'var(--text)', fontSize: '16px' }}>
                        Вы уверены, что хотите удалить все функции?
                    </div>
                    <div style={{ color: 'var(--muted)', fontSize: '14px', padding: '15px', background: 'var(--card)', border: '1px solid var(--border)', borderRadius: '4px' }}>
                        <strong>Внимание!</strong> Это действие удалит <strong>ВСЕ {functions.length} функций</strong> без возможности восстановления.
                    </div>
                    <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                        <button
                            onClick={() => setShowDeleteAllConfirm(false)}
                            style={{ padding: '8px 16px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}
                        >
                            Отмена
                        </button>
                        <button
                            onClick={confirmDeleteAll}
                            disabled={deletingAll}
                            style={{
                                padding: '8px 16px',
                                background: '#dc2626',
                                color: 'white',
                                border: '1px solid #dc2626',
                                cursor: deletingAll ? 'not-allowed' : 'pointer',
                                opacity: deletingAll ? 0.5 : 1,
                            }}
                        >
                            {deletingAll ? 'Удаление...' : 'Да, удалить все'}
                        </button>
                    </div>
                </div>
            </Modal>
        </div>
    );
}
