import {apiFetch} from "~/lib/api-client";
import {authHeaders, authHeadersWithoutContentsType} from "~/lib/api/utils/headers";

// PDF 등 큰 파일을 올릴 수 있어 업로드는 시간을 넉넉히 둔다
const UPLOAD_TIMEOUT = 60000;

/** 서버 오류는 HTTP 200 본문의 {code, message} 로 오는 경우가 있어 함께 확인한다 */
const parseResponse = async <T>(response: Response): Promise<T> => {
  const body = await response.json().catch(() => null);
  const isErrorBody = body !== null && typeof body === 'object' && !Array.isArray(body)
      && 'code' in body && 'message' in body;
  if (!response.ok || isErrorBody) {
    throw {status: response.status, message: body?.message ?? '요청에 실패했습니다.'};
  }
  return body as T;
};

/** 주보 첨부 파일을 조회하는 주소 (토큰 기반이라 로그인 쿠키 없이도 열린다) */
export const bulletinFileUrl = (accessToken: string, fileDetailId: number): string =>
    `${process.env.NEXT_PUBLIC_API_URL}/api/file/${accessToken}/${fileDetailId}`;

export const fetchBulletins = async (limit?: number): Promise<Bulletin[]> => {
  const query = limit ? `?limit=${limit}` : '';
  const response = await apiFetch(`/api/bulletin${query}`, {
    method: "GET",
    headers: await authHeaders(),
  });
  return parseResponse<Bulletin[]>(response);
};

export const fetchBulletinById = async (id: string | number): Promise<Bulletin> => {
  const response = await apiFetch(`/api/bulletin/${id}`, {
    method: "GET",
    headers: await authHeaders(),
  });
  return parseResponse<Bulletin>(response);
};

const toFormData = (input: BulletinInput): FormData => {
  const {files, ...body} = input;
  const formData = new FormData();
  files.forEach((file) => formData.append('files', file));
  formData.append('body', new Blob([JSON.stringify(body)], {type: 'application/json'}));
  return formData;
};

export const fetchInsertBulletin = async (input: BulletinInput): Promise<Bulletin> => {
  const response = await apiFetch("/api/bulletin", {
    method: "POST",
    headers: await authHeadersWithoutContentsType(),
    body: toFormData(input),
  }, UPLOAD_TIMEOUT);
  return parseResponse<Bulletin>(response);
};

export const fetchUpdateBulletin = async (input: BulletinInput): Promise<Bulletin> => {
  const response = await apiFetch("/api/bulletin", {
    method: "PUT",
    headers: await authHeadersWithoutContentsType(),
    body: toFormData(input),
  }, UPLOAD_TIMEOUT);
  return parseResponse<Bulletin>(response);
};

export const fetchDeleteBulletin = async (id: number): Promise<{ id: number }> => {
  const response = await apiFetch(`/api/bulletin/${id}`, {
    method: "DELETE",
    headers: await authHeaders(),
  });
  return parseResponse<{ id: number }>(response);
};
