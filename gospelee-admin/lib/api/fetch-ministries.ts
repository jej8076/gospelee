import {apiFetch} from "~/lib/api-client";
import {authHeaders} from "~/lib/api/utils/headers";

/** 모임 목록 조회 (ADMIN 전용). keyword 는 모임 이름이나 대표 이름 */
export const fetchMinistries = async (keyword: string): Promise<MinistryRow[]> => {
  const headers = await authHeaders();
  const response = await apiFetch("/api/admin/ministries/list", {
    method: "POST",
    headers: headers,
    body: JSON.stringify({keyword}),
  });

  if (!response.ok) {
    throw {status: response.status, message: "모임 목록을 불러오지 못했습니다."};
  }

  const result = await response.json();
  return result.data;
};

/** 모임 정지(suspend=true) / 해제 */
export const fetchChangeMinistryStatus = async (id: number, suspend: boolean): Promise<MinistryRow> => {
  const headers = await authHeaders();
  const response = await apiFetch(`/api/admin/ministries/${id}/status`, {
    method: "POST",
    headers: headers,
    body: JSON.stringify({suspend}),
  });

  if (!response.ok) {
    throw {status: response.status, message: "모임 상태를 바꾸지 못했습니다."};
  }

  const result = await response.json();
  if (result.code !== "100") {
    throw {status: response.status, message: result.message};
  }
  return result.data;
};
