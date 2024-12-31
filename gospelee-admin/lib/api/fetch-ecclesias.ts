import {expireCookie, getCookie} from "~/lib/cookie/cookie-utils";
import {AuthItems} from "~/constants/auth-items";

export type Ecclesia = {
  name: string;
  churchIdentificationNumber: string;
};

export const fetchInsertEcclesia = async (inputData: { [key: string]: any }): Promise<Ecclesia> => {
  debugger;
  const response = await fetch("/api/ecclesia", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: AuthItems.Bearer + (await getCookie(AuthItems.Authorization)),
    },
    body: JSON.stringify(inputData)
  });

  if (!response.ok) {
    debugger;
    await expireCookie(AuthItems.Authorization);
    const errorData = await response.json();
    console.error("Error response from server:", errorData.message);
    throw {status: response.status, message: errorData.message};
  }

  return response.json();
};
