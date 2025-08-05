import {expireCookie, getCookie} from "~/lib/cookie/cookie-utils";
import {AuthItems} from "~/constants/auth-items";
import {apiFetch} from "~/lib/api-client";

export const fetchEcclesiaRequests = async (): Promise<AccountEcclesiaRequest[]> => {
  const response = await apiFetch("/api/account/ecclesia/request/list", {
    method: "POST",
    headers: {
      "X-App-Identifier": "OOG_WEB",
      "Content-Type": "application/json",
      Authorization: AuthItems.Bearer + (await getCookie(AuthItems.Authorization)),
    }
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
