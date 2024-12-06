'use client'
import {setCookie} from "~/lib/cookie/cookie-utils";

export const makeQrCodeAndGetCode = async (email: string) => {
  console.log('API 호출 시작'); // API 호출 확인용 로그
  const response = await fetch(`/api/account/qr/enter`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({email}),
  });

  if (!response.ok) {
    return null;
  }

  return await response.json();
};

export const qrCheckAndGetToken = async (email: string, code: string) => {
  const response = await fetch(`/api/account/qr/check`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({email, code}),
  });

  if (!response.ok) {
    return null;
  }

  const result = await response.json();
  if (result.token == null) {
    return null;
  }

  return result.token;
}

export const setBrowserCookie = async (token: string) => {
  return setCookie("Authorization", token);

}
