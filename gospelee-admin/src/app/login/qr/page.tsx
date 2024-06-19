"use client";

import QRCode from 'qrcode.react';
import {useEffect, useState} from "react";
import {ServerEnum} from "~/enums/ServerEnum";
import {useSearchParams} from 'next/navigation';

const QRCodePage = () => {
  const searchParams = useSearchParams();
  const email = searchParams.get('email');
  const [code, setCode] = useState('');

  useEffect(() => {
    if (!email) return;

    const callApi = async () => {
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
