import {apiFetch} from "~/lib/api-client";
import {authHeaders} from "~/lib/api/utils/headers";
import {parseApiResponse} from "~/lib/api/utils/parse-response";

export const fetchPushGroups = async (): Promise<PushGroup[]> => {
  const response = await apiFetch("/api/push-group", {
    method: "GET",
    headers: await authHeaders(),
  });
  return parseApiResponse<PushGroup[]>(response);
};

/** 그룹 멤버로 고를 수 있는 우리 교회 성도 */
export const fetchPushGroupCandidates = async (): Promise<PushGroupCandidate[]> => {
  const response = await apiFetch("/api/push-group/candidates", {
    method: "GET",
    headers: await authHeaders(),
  });
  return parseApiResponse<PushGroupCandidate[]>(response);
};

export const fetchCreatePushGroup = async (input: PushGroupInput): Promise<PushGroup> => {
  const response = await apiFetch("/api/push-group", {
    method: "POST",
    headers: await authHeaders(),
    body: JSON.stringify(input),
  });
  return parseApiResponse<PushGroup>(response);
};

export const fetchUpdatePushGroup = async (input: PushGroupInput): Promise<PushGroup> => {
  const response = await apiFetch("/api/push-group", {
    method: "PUT",
    headers: await authHeaders(),
    body: JSON.stringify(input),
  });
  return parseApiResponse<PushGroup>(response);
};

export const fetchDeletePushGroup = async (id: number): Promise<{ id: number }> => {
  const response = await apiFetch(`/api/push-group/${id}`, {
    method: "DELETE",
    headers: await authHeaders(),
  });
  return parseApiResponse<{ id: number }>(response);
};
