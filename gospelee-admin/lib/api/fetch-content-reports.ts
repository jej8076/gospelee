import {apiFetch} from "~/lib/api-client";
import {authHeaders} from "~/lib/api/utils/headers";

/** 신고 목록 조회 (ADMIN 전용) */
export const fetchContentReports = async (status: string): Promise<ContentReport[]> => {
  const headers = await authHeaders();
  const response = await apiFetch("/api/admin/content-reports/list", {
    method: "POST",
    headers: headers,
    body: JSON.stringify({status}),
  });

  if (!response.ok) {
    throw {status: response.status, message: "신고 목록을 불러오지 못했습니다."};
  }

  const result = await response.json();
  return result.data;
};

/** 신고 처리: TAKEDOWN(콘텐츠 내리기) / DISMISS(기각) */
export const fetchResolveContentReport = async (uid: number, action: 'TAKEDOWN' | 'DISMISS'): Promise<void> => {
  const headers = await authHeaders();
  const response = await apiFetch(`/api/admin/content-reports/${uid}/resolve`, {
    method: "POST",
    headers: headers,
    body: JSON.stringify({action}),
  });

  if (!response.ok) {
    throw {status: response.status, message: "신고 처리에 실패했습니다."};
  }

  const result = await response.json();
  if (result.code !== "100") {
    throw {status: response.status, message: result.message};
  }
};
