import {expireCookie, getCookie} from "~/lib/cookie/cookie-utils";
import {AuthItems} from "~/constants/auth-items";

export type Users = {
  name: string;
  title: string;
  department: string;
  email: string;
  role: string;
  image: string;
};

export const fetchUsers = async (timeout = 5000): Promise<Users[]> => {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeout);

  const response = await fetch("/api/account/getAccount/list", {
    method: "POST",
    headers: {
      "X-App-Identifier": "OOG_WEB",
      "Content-Type": "application/json",
      Authorization: AuthItems.Bearer + (await getCookie(AuthItems.Authorization)),
    },
    signal: controller.signal
  });

  clearTimeout(timeoutId);

  if (!response.ok) {
    await expireCookie(AuthItems.Authorization);
    const errorData = await response.json();
    console.error("Error response from server:", errorData.message);
    throw {status: response.status, message: errorData.message};
  }

  return response.json();
};
