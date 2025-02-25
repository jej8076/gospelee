import {expireCookie, getCookie} from "~/lib/cookie/cookie-utils";
import {AuthItems} from "~/constants/auth-items";
import {apiFetch} from "~/lib/api-client";

export type Ecclesia = {
  uid: number;
  name: string;
  churchIdentificationNumber: string;
  status: string;
  masterAccountUid: number;
};

export const fetchGetEcclesia = async (ecclesiaUid: string, timeout = 5000): Promise<Ecclesia> => {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeout);

  const response = await apiFetch(`/api/ecclesia/${ecclesiaUid}`, {
    method: "POST",
    headers: {
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

export const fetchInsertEcclesia = async (inputData: {
  [key: string]: any
}, timeout = 5000): Promise<Ecclesia> => {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeout);

  const response = await apiFetch("/api/ecclesia", {
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
