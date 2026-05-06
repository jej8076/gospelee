/**
 * 인증은 httpOnly 세션 쿠키로 자동 처리됨 (apiFetch의 credentials: 'include').
 * 기본 JSON 요청 헤더만 반환.
 */
export const authHeaders = async () => {
  return {
    "Content-Type": "application/json",
  };
};

export const authHeadersWithoutContentsType = async () => {
  return {};
};
