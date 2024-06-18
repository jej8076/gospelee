"use client";

import QRCode from 'qrcode.react';
import {useEffect, useState} from "react";
import {ServerEnum} from "~/enums/ServerEnum";

const QRCodePage = () => {
  const [code, setCode] = useState('');

  useEffect(() => {
    const callApi = async () => {
      const response = await fetch(`${ServerEnum.SERVER}/api/account/qr/enter`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({message: 'QR code page entered'}),
      });

      if (response.ok) {
        const data = await response.json();
        setCode(data.code); // Assuming the returned data has a 'code' field
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