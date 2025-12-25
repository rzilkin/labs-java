let handler: ((msg: string) => void) | null = null;
let toastHandler: ((msg: string, type: 'error' | 'success' | 'info') => void) | null = null;

export function setGlobalErrorHandler(h: (msg: string) => void) {
  handler = h;
}

export function setToastHandler(h: ((msg: string, type: 'error' | 'success' | 'info') => void) | null) {
  toastHandler = h;
}

// Translate common error messages to Russian
function translateError(message: string): string {
  const translations: { [key: string]: string } = {
    'Network request failed': 'Ошибка сети. Проверьте подключение к интернету',
    'Failed to fetch': 'Ошибка сети. Проверьте подключение к интернету',
    'Unauthorized': 'Не авторизован. Пожалуйста, войдите в систему',
    'Forbidden': 'Доступ запрещен',
    'Not Found': 'Ресурс не найден',
    'Internal Server Error': 'Внутренняя ошибка сервера',
    'Bad Request': 'Неверный запрос',
    'HTTP 401': 'Не авторизован. Пожалуйста, войдите в систему',
    'HTTP 403': 'Доступ запрещен',
    'HTTP 404': 'Ресурс не найден',
    'HTTP 500': 'Внутренняя ошибка сервера',
    'HTTP 400': 'Неверный запрос',
  };

  // Check for exact matches
  if (translations[message]) {
    return translations[message];
  }

  // Check for HTTP status codes
  const httpMatch = message.match(/HTTP (\d+)/);
  if (httpMatch) {
    const status = httpMatch[1];
    const statusTranslations: { [key: string]: string } = {
      '401': 'Не авторизован. Пожалуйста, войдите в систему',
      '403': 'Доступ запрещен',
      '404': 'Ресурс не найден',
      '500': 'Внутренняя ошибка сервера',
      '400': 'Неверный запрос',
      '422': 'Ошибка валидации данных',
      '409': 'Конфликт данных',
    };
    if (statusTranslations[status]) {
      return statusTranslations[status];
    }
  }

  // If message is already in Russian or doesn't match, return as is
  return message;
}

export function showError(e: unknown, useToast: boolean = false) {
  let msg: string;
  
  if (e instanceof Error) {
    msg = e.message;
  } else if (typeof e === 'string') {
    msg = e;
  } else {
    msg = 'Произошла неизвестная ошибка';
  }

  const translatedMsg = translateError(msg);

  if (useToast && toastHandler) {
    toastHandler(translatedMsg, 'error');
  } else if (handler) {
    handler(translatedMsg);
  } else {
    // Fallback to console if no handler is set
    console.error('Unhandled error:', translatedMsg);
  }
}

export function showSuccess(message: string) {
  if (toastHandler) {
    toastHandler(message, 'success');
  }
}

export function showInfo(message: string) {
  if (toastHandler) {
    toastHandler(message, 'info');
  }
}
