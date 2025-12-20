const API = import.meta.env.VITE_API_BASE_URL as string;

export async function postJson<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(`${API}${path}`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });

  if (!res.ok) {
    let msg = `HTTP ${res.status}`;
    try {
      const e = await res.json();
      if (e?.message) msg = e.message;
    } catch {}
    throw new Error(msg);
  }

  return (await res.json()) as T;
}

export async function getText(path: string): Promise<string> {
  const res = await fetch(`${API}${path}`);
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return await res.text();
}
