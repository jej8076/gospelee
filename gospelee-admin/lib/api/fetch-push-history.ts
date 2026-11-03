import {apiFetch} from "~/lib/api-client";
import {authHeaders} from "~/lib/api/utils/headers";
import {parseApiResponse} from "~/lib/api/utils/parse-response";

export const fetchPushHistory = async (): Promise<PushHistory[]> => {
  const response = await apiFetch("/api/push-history", {
    method: "GET",
    headers: await authHeaders(),
  });
  return parseApiResponse<PushHistory[]>(response);
};

export const fetchPushHistoryDetail = async (pushId: number): Promise<PushHistoryDetail> => {
  const response = await apiFetch(`/api/push-history/${pushId}`, {
    method: "GET",
    headers: await authHeaders(),
  });
  return parseApiResponse<PushHistoryDetail>(response);
};
