"use client";

import {FormEvent, useState} from 'react';
import QRCode from 'qrcode.react';

const QRCodePage = () => {
  const [token, setToken] = useState('');

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    const response = await fetch('/api/setToken', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({token}),
    });
    // 처리 결과에 따른 추가 로직 작성
  };

  return (
      <div>
        <h1>QR Code</h1>
        <QRCode value={`https://your-nextjs-app.com/token?token=${token}`}/>
        <form onSubmit={handleSubmit}>
          <input
              type="text"
              value={token}
              onChange={(e) => setToken(e.target.value)}
          />
          <button type="submit">Generate QR Code</button>
        </form>
      </div>
  );
};

export default QRCodePage;