"use client";

import { QRCodeSVG } from 'qrcode.react';
import {useEffect, useState, Suspense, useRef, useCallback} from "react";
import {useRouter, useSearchParams} from 'next/navigation';
import {
  makeQrCodeAndGetCode,
  qrCheckAndGetToken,
  setBrowserCookie
} from "~/services/login/LoginService";
import PageTransition from '@/components/PageTransition';

const QRCodeContent = () => {
  const searchParams = useSearchParams();
  const router = useRouter();
  const email = searchParams.get('email');
  const [code, setCode] = useState('');
  const isReqRef = useRef(false);

  const startCheckingStatus = useCallback((email: string, code: string) => {
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
  }, [router]);

  useEffect(() => {
    if (!email || isReqRef.current) return;

    isReqRef.current = true;

    const fetchCode = async () => {
      const qrCode = await makeQrCodeAndGetCode(email);

      if (qrCode) {
        setCode(qrCode.code);
        startCheckingStatus(email, qrCode.code);
      }
    };

    fetchCode();
  }, [email, startCheckingStatus]);

  return (
    <PageTransition>
      <div className="flex flex-col items-center justify-center min-h-screen bg-gray-50">
        <div className="bg-white p-8 rounded-lg shadow-sm">
          <h1 className="text-2xl font-bold mb-6 text-center text-gray-800">QR 코드로 로그인</h1>
          <div className="text-center mb-6">
            <p className="text-gray-600 mb-2">모바일 앱으로 QR 코드를 스캔해주세요</p>
            <p className="text-sm text-gray-500">이메일: {email}</p>
          </div>
          {code ? (
            <div className="flex justify-center p-4 bg-white rounded-lg border">
              <QRCodeSVG 
                value={`${process.env.NEXT_PUBLIC_API_URL}/api/account/qr/req/${code}`}
                size={200}
                level="M"
                includeMargin={true}
              />
            </div>
          ) : (
            <div className="flex justify-center items-center h-48">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              <p className="ml-3 text-gray-600">QR 코드 생성 중...</p>
            </div>
          )}
          <div className="mt-6 text-center">
            <button
              onClick={() => router.back()}
              className="text-blue-600 hover:text-blue-800 text-sm font-medium"
            >
              ← 이전으로 돌아가기
            </button>
          </div>
        </div>
      </div>
    </PageTransition>
  );
};

const QRCodePage = () => {
  return (
    <Suspense fallback={
      <PageTransition>
        <div className="flex flex-col items-center justify-center min-h-screen bg-gray-50">
          <div className="bg-white p-8 rounded-lg shadow-sm">
            <div className="flex justify-center items-center h-48">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
              <p className="ml-3 text-gray-600">로딩 중...</p>
            </div>
          </div>
        </div>
      </PageTransition>
    }>
      <QRCodeContent />
    </Suspense>
  );
};

export default QRCodePage;
