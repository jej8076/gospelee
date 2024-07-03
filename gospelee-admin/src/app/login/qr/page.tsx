"use client";

import QRCode from 'qrcode.react';
import {useEffect, useState} from "react";
import {ServerEnum} from "~/enums/ServerEnum";
import {useSearchParams} from 'next/navigation';

const QRCodePage = () => {
  const searchParams = useSearchParams();
  const email = searchParams.get('email');
  const [code, setCode] = useState('');
  let isReq = false;

  useEffect(() => {
    if (!email || isReq) return;

    const callApi = async () => {
      isReq = true;
      console.log('API 호출 시작'); // API 호출 확인용 로그
      const response = await fetch(`${ServerEnum.SERVER}/api/account/qr/enter`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({email}),
      });

      if (response.ok) {
        const data = await response.json();
        setCode(data.code);
        // 상태 확인 함수 호출
        startCheckingStatus(email, data.code);
      } else {
        console.error('Failed to fetch code:', response.statusText);
      }
    };

    callApi();
  }, []);

  const startCheckingStatus = (email: string, code: string) => {
    const intervalId = setInterval(async () => {
      const checkResponse = await fetch(`${ServerEnum.SERVER}/api/account/qr/check`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({email, code}),
      });
      if (checkResponse.ok) {
        const checkData = await checkResponse.json();
        if (checkData.token != null) {
          clearInterval(intervalId);
          const response = await fetch('/api/setCookie', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({id_token: checkData.token}),
          });
        }
      } else {
        console.error('Failed to check code:', checkResponse.statusText);
      }
    }, 5000);

    // Clean up interval on component unmount
    return () => clearInterval(intervalId);
  };

  return (
      <div>
        <h1>QR Code</h1>
        {code ? (
            <QRCode value={`${ServerEnum.SERVER}/api/account/qr/${code}`}/>
        ) : (
            <p>Loading...</p>
        )}
      </div>
  );
};

export default QRCodePage;
