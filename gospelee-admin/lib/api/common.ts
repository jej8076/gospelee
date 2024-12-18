import {expireCookie, getCookie} from "~/lib/cookie/cookie-utils";
import Router from "next/router"; // Next.js 라우팅 기능 추가

/**
 * 제네릭 형태의 API 호출 함수
 * @param url API 엔드포인트
 * @param method HTTP 요청 메서드 (기본값: 'GET')
 * @param setData 데이터 상태 업데이트 함수
 * @param headers 추가 헤더 옵션 (선택적)
 */
export const fetchData = async <T>(
    url: string,
    method: string = "GET",
    setData: (data: T) => void,
    headers: Record<string, string> = {}
): Promise<void> => {

  const defaultHeaders = {
    "Content-Type": "application/json",
    Authorization: "Bearer " + (await getCookie("Authorization")),
    ...headers, // 추가 헤더 병합
  };

  const response = await fetch(url, {
    method,
    headers: defaultHeaders,
  });

  if (!response.ok) {
    await expireCookie("Authorization");
    const errorData = await response.json();
    console.error("Error response from server:", errorData.message);

    if (typeof window !== "undefined") {
      const Router = (await import("next/router")).default; // Lazy import Router
      Router.push("/login");
    }
    return;
  }

  const data: T = await response.json();
  setData(data);
};
