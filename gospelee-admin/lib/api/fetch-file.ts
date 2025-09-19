import {apiFetch} from "~/lib/api-client";
import {getCookie} from "~/lib/cookie/cookie-utils";
import {AuthItems} from "~/constants/auth-items";
import {authHeaders} from "~/lib/api/utils/headers";

/**
 * 파일 조회 API
 * @param fileId 파일 ID (fileUid)
 * @param fileDetailId 파일 상세 ID
 * @returns 파일 blob URL
 */
export const fetchFileById = async (fileId: number, fileDetailId: number): Promise<string> => {
  try {
    const headers = await authHeaders();
    const response = await apiFetch(`/api/file/${fileId}/${fileDetailId}`, {
      method: "GET",
      headers: headers
    });

    if (!response.ok) {
      throw new Error(`파일 조회 실패: ${response.status}`);
    }

    // 응답을 blob으로 변환
    const blob = await response.blob();
    return URL.createObjectURL(blob);
  } catch (error) {
    console.error('파일 조회 실패:', error);
    throw error;
  }
};
