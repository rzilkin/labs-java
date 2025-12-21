type Props = { message: string; onClose: () => void };

export function ErrorModal({ message, onClose }: Props) {
  return (
    <div
      style={{
        position: "fixed",
        inset: 0,
        background: "rgba(0,0,0,0.6)",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        zIndex: 9999,
      }}
      onClick={onClose}
    >
      <div
        style={{
          background: "var(--card)",
          color: "var(--text)",
          padding: 20,
          borderRadius: 12,
          width: 420,
          border: "1px solid var(--border)",
          boxShadow: "var(--shadow)",
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <h3 style={{ marginTop: 0, color: "var(--text)" }}>Ошибка</h3>
        <div style={{ whiteSpace: "pre-wrap", color: "var(--text)", marginBottom: 16 }}>{message}</div>
        <button
          onClick={onClose}
          style={{
            padding: "8px 16px",
            background: "var(--btn-bg)",
            color: "var(--btn-text)",
            border: "1px solid var(--border)",
            cursor: "pointer",
          }}
        >
          Закрыть
        </button>
      </div>
    </div>
  );
}
