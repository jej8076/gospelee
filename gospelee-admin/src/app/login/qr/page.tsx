"use client";

import QRCode from 'qrcode.react';
import {useEffect, useState} from "react";
import {ServerEnum} from "~/enums/ServerEnum";
import {useSearchParams, useRouter} from 'next/navigation';
import {
  makeQrCodeAndGetCode,
  qrCheckAndGetToken,
  setBrowserCookie
} from "~/services/login/LoginService";

const QRCodePage = () => {
  const searchParams = useSearchParams();
  const router = useRouter();
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

      if (await setBrowserCookie(token) == 200) {
        clearInterval(intervalId);
        router.push('/main');
      }

    }, 3000);

    // Clean up interval on component unmount
    return () => clearInterval(intervalId);
  };

  return (
      <div>
        <h1>QR Code</h1>
        {code ? (
            <QRCode value={`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/account/qr/${code}`}/>
        ) : (
            <p>Loading...</p>
        )}
      </div>
  );
};

export default QRCodePage;
