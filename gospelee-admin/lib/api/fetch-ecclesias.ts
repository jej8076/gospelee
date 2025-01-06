import {expireCookie, getCookie} from "~/lib/cookie/cookie-utils";
import {AuthItems} from "~/constants/auth-items";

export type Ecclesia = {
  name: string;
  churchIdentificationNumber: string;
};

export const fetchInsertEcclesia = async (inputData: {
  [key: string]: any
}, timeout = 5000): Promise<Ecclesia> => {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeout);

  const response = await fetch("/api/ecclesia", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: AuthItems.Bearer + (await getCookie(AuthItems.Authorization)),
    },
    body: JSON.stringify(inputData),
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
