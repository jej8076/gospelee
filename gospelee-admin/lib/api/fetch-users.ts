import {expireCookie, getCookie} from "~/lib/cookie/cookie-utils";
import {AuthItems} from "~/constants/auth-items";
import {apiFetch} from "~/lib/api-client";
import {authHeaders} from "~/lib/api/utils/headers";

export type Users = {
  name: string;
  title: string;
  department: string;
  email: string;
  role: string;
  image: string;
};

export const fetchUsers = async (): Promise<Users[]> => {
  const headers = await authHeaders();
  const response = await apiFetch("/api/account/getAccount/list", {
    method: "POST",
    headers: headers,
  });

  if (!response.ok) {
    await expireCookie(AuthItems.Authorization);
    await expireCookie(AuthItems.SocialAccessToken);
    await expireCookie(AuthItems.SocialRefreshToken);
    const errorData = await response.json();
    console.error("Error response from server:", errorData.message);
    throw {status: response.status, message: errorData.message};
  }

  return response.json();
};
