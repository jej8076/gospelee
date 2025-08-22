"use client";

import { useRouter } from "next/navigation";

type ApiFunction<T> = (...args: any[]) => Promise<T>;

export const useApiClient = () => {
  const router = useRouter();

  const callApi = async <T>(
      apiFunction: ApiFunction<T>,
      onSuccess: (data: T) => void,
      ...args: any[]
  ): Promise<void> => {
    try {
      const data = await apiFunction(...args);
      onSuccess(data);
    } catch (error: any) {
      // 401 Unauthorized - 로그인 페이지로 리다이렉트
      if (error.status === 401) {
        router.push("/login");
        return;
      }
      
      // TimeoutError 처리
      if (error.name === 'TimeoutError') {
        console.warn("API request timeout:", error.message);
        // 타임아웃은 조용히 처리하거나 필요시 사용자에게 알림
        return;
      }
      
      // AbortError 처리 (혹시 남아있는 경우)
      if (error.name === 'AbortError') {
        console.warn("API request was aborted:", error.message);
        return;
      }
      
      // 네트워크 에러
      if (error.name === 'TypeError' && error.message?.includes('fetch')) {
        console.error("Network error:", error.message);
        return;
      }
      
      // 기타 에러
      console.error("API call failed:", {
        message: error.message || error,
        status: error.status,
        name: error.name,
        stack: error.stack
      });
    }
  };

  return { callApi };
};
