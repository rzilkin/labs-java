const API = import.meta.env.VITE_API_BASE_URL as string;

function getAuthHeaders(): HeadersInit {
  const headers: HeadersInit = { "Content-Type": "application/json" };
  // Get stored credentials for Basic Auth
  const username = localStorage.getItem('username');
  const password = localStorage.getItem('password');
  if (username && password) {
    const auth = btoa(`${username}:${password}`);
    headers['Authorization'] = `Basic ${auth}`;
  }
  return headers;
}

// Store credentials after successful login
export function setCredentials(username: string, password: string) {
  localStorage.setItem('username', username);
  localStorage.setItem('password', password);
}

export function clearCredentials() {
  localStorage.removeItem('username');
  localStorage.removeItem('password');
}

export async function postJson<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(`${API}${path}`, {
    method: "POST",
    headers: getAuthHeaders(),
    credentials: 'include',
    body: JSON.stringify(body),
  });

  if (!res.ok) {
    let msg = `HTTP ${res.status}`;
    try {
      const e = await res.json();
      if (e?.message) msg = e.message;
    } catch { }
    throw new Error(msg);
  }

  return (await res.json()) as T;
}

export async function getJson<T>(path: string): Promise<T> {
  const res = await fetch(`${API}${path}`, {
    headers: getAuthHeaders(),
    credentials: 'include',
  });

  if (!res.ok) {
    let msg = `HTTP ${res.status}`;
    try {
      const e = await res.json();
      if (e?.message) msg = e.message;
    } catch { }
    throw new Error(msg);
  }

  return (await res.json()) as T;
}

export async function getText(path: string): Promise<string> {
  const res = await fetch(`${API}${path}`, {
    headers: getAuthHeaders(),
    credentials: 'include',
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return await res.text();
}

export async function deleteRequest(path: string): Promise<void> {
  const res = await fetch(`${API}${path}`, {
    method: "DELETE",
    headers: getAuthHeaders(),
    credentials: 'include',
  });
  if (!res.ok) {
    let msg = `HTTP ${res.status}`;
    try {
      const e = await res.json();
      if (e?.message) msg = e.message;
    } catch { }
    throw new Error(msg);
  }
}

export async function putJson<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(`${API}${path}`, {
    method: "PUT",
    headers: getAuthHeaders(),
    credentials: 'include',
    body: JSON.stringify(body),
  });

  if (!res.ok) {
    let msg = `HTTP ${res.status}`;
    try {
      const e = await res.json();
      if (e?.message) msg = e.message;
    } catch { }
    throw new Error(msg);
  }

  return (await res.json()) as T;
}
