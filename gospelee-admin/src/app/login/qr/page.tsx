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
      } else {
        console.error('Failed to fetch code:', response.statusText);
      }
    };

    callApi();
  }, []);

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
