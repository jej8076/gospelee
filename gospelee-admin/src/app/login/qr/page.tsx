"use client";

import QRCode from 'qrcode.react';
import {useEffect, useState} from "react";
import {ServerEnum} from "~/enums/ServerEnum";
import {useSearchParams} from 'next/navigation';
import {
  makeQrCodeAndGetCode,
  qrCheckAndGetToken,
  setBrowserCookie
} from "~/services/login/LoginService";

const QRCodePage = () => {
  const searchParams = useSearchParams();
  const email = searchParams.get('email');
  const [code, setCode] = useState('');
  let isReq = false;

  useEffect(() => {
    if (!email || isReq) return;

    isReq = true;

    const fetchCode = async () => {
      const qrCode = await makeQrCodeAndGetCode(email);

      if (qrCode) {
        setCode(qrCode.code);
        startCheckingStatus(email, qrCode.code);
      }
    };

    fetchCode();
  }, []);

  const startCheckingStatus = (email: string, code: string) => {
    const intervalId = setInterval(async () => {
      const token = await qrCheckAndGetToken(email, code);

      if (token == null) return;

      if (await setBrowserCookie(token)) {
        clearInterval(intervalId);

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
