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
          background: "#111",
          color: "#fff",
          padding: 20,
          borderRadius: 12,
          width: 420,
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <h3 style={{ marginTop: 0 }}>Ошибка</h3>
        <div style={{ whiteSpace: "pre-wrap" }}>{message}</div>
        <button style={{ marginTop: 12 }} onClick={onClose}>
          Закрыть
        </button>
      </div>
    </div>
  );
}
