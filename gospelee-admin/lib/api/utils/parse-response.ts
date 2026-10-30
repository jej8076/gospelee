/**
 * 서버 오류는 HTTP 200 본문의 {code, message} 로 오는 경우가 있어 함께 확인한다.
 * 실패하면 {status, message} 를 던진다.
 */
export const parseApiResponse = async <T>(response: Response): Promise<T> => {
  const body = await response.json().catch(() => null);
  const isErrorBody = body !== null && typeof body === 'object' && !Array.isArray(body)
      && 'code' in body && 'message' in body;
  if (!response.ok || isErrorBody) {
    throw {status: response.status, message: body?.message ?? '요청에 실패했습니다.'};
  }
  return body as T;
};
