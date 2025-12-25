import { Toast } from './Toast';

interface ToastItem {
    id: string;
    message: string;
    type: 'success' | 'error' | 'info';
}

interface ToastContainerProps {
    toasts: ToastItem[];
    onRemove: (id: string) => void;
}

export function ToastContainer({ toasts, onRemove }: ToastContainerProps) {
    return (
        <>
            {toasts.map((toast, index) => (
                <div
                    key={toast.id}
                    style={{
                        position: 'fixed',
                        bottom: `${20 + index * 80}px`,
                        right: '20px',
                        zIndex: 10000,
                    }}
                >
                    <Toast
                        message={toast.message}
                        type={toast.type}
                        onClose={() => onRemove(toast.id)}
                    />
                </div>
            ))}
        </>
    );
}

