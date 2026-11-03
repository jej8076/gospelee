import {apiFetch} from "~/lib/api-client";
import {authHeaders} from "~/lib/api/utils/headers";
import {parseApiResponse} from "~/lib/api/utils/parse-response";

// 대상이 많으면 한 명씩 보내느라 오래 걸릴 수 있다
const SEND_TIMEOUT = 60000;

export const fetchSendPushDirect = async (input: PushDirectInput): Promise<PushDirectResult> => {
  const response = await apiFetch("/api/push-direct", {
    method: "POST",
    headers: await authHeaders(),
    body: JSON.stringify(input),
  }, SEND_TIMEOUT);
  return parseApiResponse<PushDirectResult>(response);
};
