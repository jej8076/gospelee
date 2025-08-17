const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL;

export const apiFetch = async (
    endpoint: string,
    options?: RequestInit,
    timeout: number = 3000
) => {
  const controller = new AbortController();
  const signal = controller.signal;

  // 절대 변경 불가능한 기본 헤더
  const defaultHeaders: HeadersInit = {
    "X-App-Identifier": "OOG_WEB",
  };

  // 사용자 헤더에서 기본 헤더 키는 제거
  const userHeaders = {...(options?.headers || {})};
  delete (userHeaders as any)["X-App-Identifier"];

  const fetchOptions: RequestInit = {
    ...options,
    headers: {
      ...userHeaders,
      ...defaultHeaders, // 마지막에 기본 헤더를 덮어써서 보장
    },
    signal
  };

  // 타임아웃
  const timeoutId = setTimeout(() => controller.abort(), timeout);

  const url = `${API_BASE_URL}${endpoint}`;
  const response = await fetch(url, fetchOptions);

  clearTimeout(timeoutId);

  return response;
};
