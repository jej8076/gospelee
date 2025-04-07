const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL;

export const apiFetch = async (
    endpoint: string,
    options?: RequestInit,
    timeout: number = 3000
) => {
  const controller = new AbortController();
  const signal = controller.signal;

  const fetchOptions: RequestInit = {
    ...options,
    signal
  };

  // 타임아웃
  const timeoutId = setTimeout(() => controller.abort(), timeout);

  const url = `${API_BASE_URL}${endpoint}`;
  const response = await fetch(url, fetchOptions);

  clearTimeout(timeoutId);

  return response;
};
