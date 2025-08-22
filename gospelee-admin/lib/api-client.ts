const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL;

// 타임아웃 에러 클래스
class TimeoutError extends Error {
  constructor(timeout: number) {
    super(`Request timeout after ${timeout}ms`);
    this.name = 'TimeoutError';
  }
}

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

  // 타임아웃 Promise
  const timeoutPromise = new Promise<never>((_, reject) => {
    setTimeout(() => {
      controller.abort();
      reject(new TimeoutError(timeout));
    }, timeout);
  });

  try {
    const url = `${API_BASE_URL}${endpoint}`;
    
    // fetch와 timeout을 경쟁시킴
    const response = await Promise.race([
      fetch(url, fetchOptions),
      timeoutPromise
    ]);
    
    return response;
  } catch (error) {
    // AbortError를 TimeoutError로 변환
    if (error instanceof Error && error.name === 'AbortError') {
      throw new TimeoutError(timeout);
    }
    
    // 기타 에러는 그대로 전파
    throw error;
  }
};
