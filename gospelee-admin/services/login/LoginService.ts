'use client'
import {apiFetch} from "~/lib/api-client";

export const makeQrCodeAndGetCode = async (email: string, skipNotification: boolean = false) => {
  const response = await apiFetch(`/api/account/qr/enter`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({email, skipNotification}),
  });

  if (!response.ok) {
    return null;
  }

  return await response.json();
};

/**
 * QR 인증 상태 확인. 모바일이 인증을 완료하면 API가 세션 cookie를 발급함.
 * 응답이 200이고 authenticated=true면 로그인 성공.
 */
export const checkQrAuthenticated = async (email: string, code: string): Promise<boolean> => {
  const response = await apiFetch(`/api/account/qr/check`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({email, code}),
  });

  if (!response.ok) {
    return false;
  }

  const result = await response.json();
  return result?.authenticated === true;
}
