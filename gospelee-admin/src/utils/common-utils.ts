export function isBlank(value: string | null | undefined): boolean {
  return value === null || value === undefined || value.trim().length === 0;
}

export function isNotBlank(value: string | null | undefined): boolean {
  return !isBlank(value);
}
