import {ServerEnum} from "~/enums/ServerEnum";
import {setCookie} from "~/provider/CookieProvider";

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
  const response = await fetch(`/api/account/cookie`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'id_token': token
    },
    credentials: 'include',
  });

  let cookie = response.headers.get('Set-Cookie');
  debugger;
  const defaultOptions = {
    path: '/',
    secure: false,
    sameSite: 'None',
    expires: new Date(Date.now() + 3600 * 60 * 60 * 1000),
  };

  let ss = setCookie("id_token", "sss", defaultOptions);

  return response.ok;
}
