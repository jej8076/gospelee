import {expireCookie, getCookie} from "~/lib/cookie/cookie-utils";
import {AuthItems} from "~/constants/auth-items";
import {apiFetch} from "~/lib/api-client";
import {convertEcclesiaStatusType, EcclesiaStatusType} from "@/enums/ecclesia/status";

export const fetchGetEcclesia = async (accountUid: number): Promise<Ecclesia> => {

  if (accountUid === null) {
    throw {status: 500, message: 'accountUid 없음'};
  }

  const response = await apiFetch(`/api/ecclesia/account/${accountUid}`, {
    method: "POST",
    headers: {
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

  return response.json();
};

export const fetchInsertEcclesia = async (inputData: {
  [key: string]: any
}): Promise<Ecclesia> => {

  const response = await apiFetch("/api/ecclesia", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: AuthItems.Bearer + (await getCookie(AuthItems.Authorization)),
    },
    body: JSON.stringify(inputData),
  });

  if (!response.ok) {
    await expireCookie(AuthItems.Authorization);
    const errorData = await response.json();
    console.error("Error response from server:", errorData.message);
    throw {status: response.status, message: errorData.message};
  }

  return response.json();
};

export const fetchUpdateEcclesiaStatus = async (inputData: {
  [key: string]: any
}): Promise<EcclesiaStatusType> => {

  const typedData = inputData as EcclesiaStatusSelectorProps;

  const response = await apiFetch(`/api/ecclesia/status`, {
    method: "PATCH",
    headers: {
      "Content-Type": "application/json",
      Authorization: AuthItems.Bearer + (await getCookie(AuthItems.Authorization)),
    },
    body: JSON.stringify(inputData),
  });

  if (!response.ok) {
    await expireCookie(AuthItems.Authorization);
    const errorData = await response.json();
    console.error("Error response from server:", errorData.message);
    throw {status: response.status, message: errorData.message};
  }

  const responseData = await response.json();
  return convertEcclesiaStatusType(responseData.status);
};
