import { useEffect } from 'react';

interface ToastProps {
    message: string;
    type: 'success' | 'error' | 'info';
    onClose: () => void;
    duration?: number;
}

export function Toast({ message, type, onClose, duration = 3000 }: ToastProps) {
    useEffect(() => {
        const timer = setTimeout(() => {
            onClose();
        }, duration);

        return () => clearTimeout(timer);
    }, [duration, onClose]);

    const bgColor = type === 'success' 
        ? '#10b981' 
        : type === 'error' 
        ? '#ef4444' 
        : '#3b82f6';

    return (
        <div
            style={{
                position: 'fixed',
                bottom: '20px',
                right: '20px',
                padding: '16px 20px',
                background: bgColor,
                color: 'white',
                borderRadius: '8px',
                boxShadow: '0 10px 25px rgba(0,0,0,0.2)',
                zIndex: 10000,
                minWidth: '300px',
                maxWidth: '500px',
                animation: 'slideIn 0.3s ease-out',
            }}
            onClick={onClose}
        >
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: '12px' }}>
                <span style={{ flex: 1 }}>{message}</span>
                <button
                    onClick={onClose}
                    style={{
                        background: 'transparent',
                        border: 'none',
                        color: 'white',
                        cursor: 'pointer',
                        fontSize: '20px',
                        padding: '0',
                        width: '24px',
                        height: '24px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                    }}
                >
                    ×
                </button>
            </div>
        </div>
    );
}

