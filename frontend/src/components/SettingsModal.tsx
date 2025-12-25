import { useState, useEffect } from 'react';
import { Modal } from './Modal';

interface SettingsModalProps {
    isOpen: boolean;
    onClose: () => void;
    factoryType: 'array' | 'linked-list';
    onFactoryChange: (factory: 'array' | 'linked-list') => void;
}

export function SettingsModal({ isOpen, onClose, factoryType, onFactoryChange }: SettingsModalProps) {
    const [currentFactory, setCurrentFactory] = useState<'array' | 'linked-list'>(factoryType);

    useEffect(() => {
        if (isOpen) {
            setCurrentFactory(factoryType);
        }
    }, [isOpen, factoryType]);

    const handleSave = () => {
        onFactoryChange(currentFactory);
        onClose();
    };

    return (
        <Modal isOpen={isOpen} onClose={onClose} title="Настройки">
            <div>
                <div style={{ marginBottom: '15px' }}>
                    <label style={{ color: 'var(--text)' }}>
                        <input
                            type="radio"
                            checked={currentFactory === 'array'}
                            onChange={() => setCurrentFactory('array')}
                            style={{ marginRight: '8px' }}
                        />
                        На основе массива
                    </label>
                </div>
                <div style={{ marginBottom: '15px' }}>
                    <label style={{ color: 'var(--text)' }}>
                        <input
                            type="radio"
                            checked={currentFactory === 'linked-list'}
                            onChange={() => setCurrentFactory('linked-list')}
                            style={{ marginRight: '8px' }}
                        />
                        На основе связного списка
                    </label>
                </div>
                <div style={{ marginTop: '20px' }}>
                    <button onClick={handleSave} style={{ padding: '8px 16px', marginRight: '10px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                        Сохранить
                    </button>
                    <button onClick={onClose} style={{ padding: '8px 16px', background: 'var(--btn2-bg)', color: 'var(--btn2-text)', border: '1px solid var(--border)' }}>
                        Отмена
                    </button>
                </div>
            </div>
        </Modal>
    );
}

