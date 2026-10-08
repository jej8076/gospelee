import {apiFetch} from "~/lib/api-client";
import {convertEcclesiaStatusType, EcclesiaStatusType} from "@/enums/ecclesia/status";
import {authHeaders} from "~/lib/api/utils/headers";

export const fetchGetEcclesia = async (accountUid: number): Promise<Ecclesia> => {

  if (accountUid === null) {
    throw {status: 500, message: 'accountUid 없음'};
  }

  const headers = await authHeaders();
  const response = await apiFetch(`/api/ecclesia/account/${accountUid}`, {
    method: "POST",
    headers: headers,
  });

  if (!response.ok) {
    const errorData = await response.json();
    console.error("Error response from server:", errorData.message);
    throw {status: response.status, message: errorData.message};
  }

  return response.json();
};

export const fetchInsertEcclesia = async (inputData: {
  [key: string]: any
}): Promise<Ecclesia> => {
  const headers = await authHeaders();
  const response = await apiFetch("/api/ecclesia", {
    method: "POST",
    headers: headers,
    body: JSON.stringify(inputData),
  });

  if (!response.ok) {
    const errorData = await response.json();
    console.error("Error response from server:", errorData.message);
    throw {status: response.status, message: errorData.message};
  }

  return response.json();
};

export const fetchUpdateEcclesia = async (inputData: {
  [key: string]: any
}): Promise<EcclesiaStatusType> => {

  const headers = await authHeaders();
  const response = await apiFetch(`/api/ecclesia/status`, {
    method: "PATCH",
    headers: headers,
    body: JSON.stringify(inputData),
  });

  if (!response.ok) {
    const errorData = await response.json();
    console.error("Error response from server:", errorData.message);
    throw {status: response.status, message: errorData.message};
  }

  const responseData = await response.json();
  return convertEcclesiaStatusType(responseData.status);
};

// 운영자(ADMIN) 전용: 전화 등으로 확인한 교회를 검증 완료/취소로 표시
export const fetchVerifyEcclesia = async (inputData: {
  ecclesiaUid: bigint;
  verified: boolean;
}): Promise<Ecclesia> => {
  const headers = await authHeaders();
  const response = await apiFetch(`/api/ecclesia/verify`, {
    method: "PATCH",
    headers: headers,
    body: JSON.stringify({
      ecclesiaUid: Number(inputData.ecclesiaUid),
      verified: inputData.verified,
    }),
  });

  if (!response.ok) {
    const errorData = await response.json();
    console.error("Error response from server:", errorData.message);
    throw {status: response.status, message: errorData.message};
  }

  return response.json();
};
