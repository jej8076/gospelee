import {expireCookie, getCookie} from "~/lib/cookie/cookie-utils";
import {AuthItems} from "~/constants/auth-items";
import {apiFetch} from "~/lib/api-client";
import {authHeaders} from "~/lib/api/utils/headers";

export const fetchEcclesiaRequests = async (): Promise<AccountEcclesiaRequest[]> => {
  const headers = await authHeaders();
  const response = await apiFetch("/api/account/ecclesia/join/request/list", {
    method: "POST",
    headers: headers,
  });

  if (!response.ok) {
    await expireCookie(AuthItems.Authorization);
    const errorData = await response.json();
    console.error("Error response from server:", errorData.message);
    throw {status: response.status, message: errorData.message};
  }

  const result = await response.json();
  return result.data || [];
};

export const decideEcclesiaRequest = async (accountEcclesiaDecide: AccountEcclesiaDecide): Promise<AccountEcclesiaRequest> => {
  const headers = await authHeaders();
  const response = await apiFetch("/api/account/ecclesia/join/request/decide", {
    method: "POST",
    headers: headers,
    body: JSON.stringify(accountEcclesiaDecide),
  });

  if (!response.ok) {
    await expireCookie(AuthItems.Authorization);
    const errorData = await response.json();
    console.error("Error response from server:", errorData.message);
    throw {status: response.status, message: errorData.message};
  }

  const result = await response.json();
  return result.data || null;
};
