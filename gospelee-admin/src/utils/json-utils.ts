export const tryParseJson = <T = unknown>(value: unknown): { success: true; data: T } | {
  success: false
} => {
  if (typeof value !== 'string') return {success: false};

  try {
    const parsed = JSON.parse(value);
    return {success: true, data: parsed};
  } catch {
    return {success: false};
  }
};
