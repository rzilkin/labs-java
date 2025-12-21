import { useEffect } from 'react';

interface ModalProps {
    isOpen: boolean;
    onClose: () => void;
    title: string;
    children: React.ReactNode;
}

export function Modal({ isOpen, onClose, title, children }: ModalProps) {
    useEffect(() => {
        if (isOpen) {
            const handleEscape = (e: KeyboardEvent) => {
                if (e.key === 'Escape') {
                    onClose();
                }
            };
            document.addEventListener('keydown', handleEscape);
            return () => document.removeEventListener('keydown', handleEscape);
        }
    }, [isOpen, onClose]);

    if (!isOpen) return null;

    return (
        <div
            style={{
                position: 'fixed',
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                backgroundColor: 'rgba(0, 0, 0, 0.5)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                zIndex: 1000,
            }}
            onClick={onClose}
        >
            <div
                style={{
                    backgroundColor: 'var(--card)',
                    color: 'var(--text)',
                    padding: '20px',
                    borderRadius: '4px',
                    maxWidth: '90%',
                    maxHeight: '90%',
                    overflow: 'auto',
                    minWidth: '400px',
                    border: '1px solid var(--border)',
                    boxShadow: 'var(--shadow)',
                }}
                onClick={(e) => e.stopPropagation()}
            >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                    <h2 style={{ margin: 0, color: 'var(--text)' }}>{title}</h2>
                    <button onClick={onClose} style={{ padding: '5px 10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                        ✕
                    </button>
                </div>
                {children}
            </div>
        </div>
    );
}

