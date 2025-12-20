let handler: ((msg: string) => void) | null = null;

export function setGlobalErrorHandler(h: (msg: string) => void) {
  handler = h;
}

export function showError(e: unknown) {
  const msg = e instanceof Error ? e.message : String(e);
  handler?.(msg);
}
