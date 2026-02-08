'use client'
import {setCookie, setCookies} from "~/lib/cookie/cookie-utils";
import {AuthItems} from "~/constants/auth-items";
import {apiFetch} from "~/lib/api-client";

export const makeQrCodeAndGetCode = async (email: string, skipNotification: boolean = false) => {
  console.log('API 호출 시작'); // API 호출 확인용 로그
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

export const qrCheckAndGetToken = async (email: string, code: string) => {
  const response = await apiFetch(`/api/account/qr/check`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({email, code}),
  });

  if (!response.ok) {
    alert(response.ok);
    return null;
  }

  const result = await response.json();
  if (result.idToken == null && result.accessToken == null) {
    return null;
  }

  return result;
}

export const setBrowserCookie = async (token: string) => {
  return setCookie(AuthItems.Authorization, token);
}

export const setBrowserCookies = async (cookies: { name: string; value: string }[]) => {
  return await setCookies(cookies);
}

