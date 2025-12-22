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

// Global logout handler - will be set by App component
let globalLogoutHandler: (() => void) | null = null;

export function setLogoutHandler(handler: (() => void) | null) {
  globalLogoutHandler = handler;
}

export function handle401Error() {
  // Clear credentials
  clearCredentials();
  // Call logout handler if set
  if (globalLogoutHandler) {
    globalLogoutHandler();
  }
}

export async function postJson<T>(path: string, body: unknown): Promise<T> {
  try {
    const res = await fetch(`${API}${path}`, {
      method: "POST",
      headers: getAuthHeaders(),
      credentials: 'include',
      body: JSON.stringify(body),
    });

    if (!res.ok) {
      // Handle 401 Unauthorized - auto logout
      if (res.status === 401) {
        handle401Error();
        throw new Error('Сессия истекла. Войдите заново.');
      }

      let msg = `HTTP ${res.status}`;
      try {
        const e = await res.json();
        if (e?.message) {
          msg = e.message;
        } else if (e?.error) {
          msg = e.error;
        }
      } catch {
        // If JSON parsing fails, use status-based message
        const statusMessages: { [key: number]: string } = {
          400: 'Неверный запрос. Проверьте введенные данные',
          401: 'Не авторизован. Пожалуйста, войдите в систему',
          403: 'Доступ запрещен',
          404: 'Ресурс не найден',
          409: 'Конфликт данных',
          422: 'Ошибка валидации данных',
          500: 'Внутренняя ошибка сервера',
          503: 'Сервис временно недоступен',
        };
        msg = statusMessages[res.status] || `Ошибка сервера (${res.status})`;
      }
      throw new Error(msg);
    }

    return (await res.json()) as T;
  } catch (e) {
    if (e instanceof Error) {
      throw e;
    }
    if (e instanceof TypeError && e.message.includes('fetch')) {
      throw new Error('Ошибка сети. Проверьте подключение к интернету');
    }
    throw new Error('Произошла неизвестная ошибка при выполнении запроса');
  }
}

export async function getJson<T>(path: string): Promise<T> {
  try {
    const res = await fetch(`${API}${path}`, {
      headers: getAuthHeaders(),
      credentials: 'include',
    });

    if (!res.ok) {
      // Handle 401 Unauthorized - auto logout
      if (res.status === 401) {
        handle401Error();
        throw new Error('Сессия истекла. Войдите заново.');
      }

      let msg = `HTTP ${res.status}`;
      try {
        const e = await res.json();
        if (e?.message) {
          msg = e.message;
        } else if (e?.error) {
          msg = e.error;
        }
      } catch {
        const statusMessages: { [key: number]: string } = {
          400: 'Неверный запрос',
          401: 'Не авторизован. Пожалуйста, войдите в систему',
          403: 'Доступ запрещен',
          404: 'Ресурс не найден',
          500: 'Внутренняя ошибка сервера',
          503: 'Сервис временно недоступен',
        };
        msg = statusMessages[res.status] || `Ошибка сервера (${res.status})`;
      }
      throw new Error(msg);
    }

    return (await res.json()) as T;
  } catch (e) {
    if (e instanceof Error) {
      throw e;
    }
    if (e instanceof TypeError && e.message.includes('fetch')) {
      throw new Error('Ошибка сети. Проверьте подключение к интернету');
    }
    throw new Error('Произошла неизвестная ошибка при загрузке данных');
  }
}

export async function getText(path: string): Promise<string> {
  const res = await fetch(`${API}${path}`, {
    headers: getAuthHeaders(),
    credentials: 'include',
  });
  if (!res.ok) {
    // Handle 401 Unauthorized - auto logout
    if (res.status === 401) {
      handle401Error();
      throw new Error('Сессия истекла. Войдите заново.');
    }
    throw new Error(`HTTP ${res.status}`);
  }
  return await res.text();
}

export async function deleteRequest(path: string): Promise<void> {
  try {
    const res = await fetch(`${API}${path}`, {
      method: "DELETE",
      headers: getAuthHeaders(),
      credentials: 'include',
    });
    if (!res.ok) {
      // Handle 401 Unauthorized - auto logout
      if (res.status === 401) {
        handle401Error();
        throw new Error('Сессия истекла. Войдите заново.');
      }

      let msg = `HTTP ${res.status}`;
      try {
        const e = await res.json();
        if (e?.message) {
          msg = e.message;
        } else if (e?.error) {
          msg = e.error;
        }
      } catch {
        const statusMessages: { [key: number]: string } = {
          400: 'Неверный запрос',
          401: 'Не авторизован. Пожалуйста, войдите в систему',
          403: 'Доступ запрещен',
          404: 'Ресурс не найден',
          500: 'Внутренняя ошибка сервера',
        };
        msg = statusMessages[res.status] || `Ошибка сервера (${res.status})`;
      }
      throw new Error(msg);
    }
  } catch (e) {
    if (e instanceof Error) {
      throw e;
    }
    if (e instanceof TypeError && e.message.includes('fetch')) {
      throw new Error('Ошибка сети. Проверьте подключение к интернету');
    }
    throw new Error('Произошла неизвестная ошибка при удалении');
  }
}

export async function putJson<T>(path: string, body: unknown): Promise<T> {
  try {
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
        if (e?.message) {
          msg = e.message;
        } else if (e?.error) {
          msg = e.error;
        }
      } catch {
        const statusMessages: { [key: number]: string } = {
          400: 'Неверный запрос. Проверьте введенные данные',
          401: 'Не авторизован. Пожалуйста, войдите в систему',
          403: 'Доступ запрещен',
          404: 'Ресурс не найден',
          409: 'Конфликт данных',
          422: 'Ошибка валидации данных',
          500: 'Внутренняя ошибка сервера',
        };
        msg = statusMessages[res.status] || `Ошибка сервера (${res.status})`;
      }
      throw new Error(msg);
    }

    return (await res.json()) as T;
  } catch (e) {
    if (e instanceof Error) {
      throw e;
    }
    if (e instanceof TypeError && e.message.includes('fetch')) {
      throw new Error('Ошибка сети. Проверьте подключение к интернету');
    }
    throw new Error('Произошла неизвестная ошибка при обновлении данных');
  }
}
