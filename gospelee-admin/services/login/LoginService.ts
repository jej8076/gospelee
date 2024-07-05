import {ServerEnum} from "~/enums/ServerEnum";

export const makeQrCodeAndGetCode = async (email: string) => {
  console.log('API 호출 시작'); // API 호출 확인용 로그
  const response = await fetch(`${ServerEnum.SERVER}/api/account/qr/enter`, {
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
  const response = await fetch(`${ServerEnum.SERVER}/api/account/qr/check`, {
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

export const setCookie = async (token: string) => {
  const response = await fetch(`${ServerEnum.SERVER}/api/account/cookie`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'id_token': token
    },
  })

  return response.ok;
}
