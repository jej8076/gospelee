"use client";

import {Suspense, useCallback, useEffect, useRef, useState} from "react";
import {useRouter, useSearchParams} from 'next/navigation';
import {
  makeQrCodeAndGetCode,
  qrCheckAndGetToken,
  setBrowserCookies
} from "~/services/login/LoginService";
import PageTransition from '@/components/PageTransition';
import {AuthItems} from "~/constants/auth-items";

const MobileLoginContent = () => {
  const searchParams = useSearchParams();
  const router = useRouter();
  const email = searchParams.get('email');
  const [code, setCode] = useState('');
  const [isAppOpened, setIsAppOpened] = useState(false);
  const [debugLogs, setDebugLogs] = useState<string[]>([]);
  const isReqRef = useRef(false);
  const logsRef = useRef<string[]>([]);

  const addLog = useCallback((message: string) => {
    const timestamp = new Date().toLocaleTimeString();
    const log = `[${timestamp}] ${message}`;
    logsRef.current = [...logsRef.current, log];
    setDebugLogs([...logsRef.current]);
  }, []);

  useEffect(() => {
    if (!email) {
      addLog(`에러: email 파라미터 없음`);
      return;
    }

    if (isReqRef.current) return;
    isReqRef.current = true;

    addLog(`시작: email=${email}`);

    const fetchCodeAndOpenApp = async () => {
      try {
        const apiUrl = process.env.NEXT_PUBLIC_API_URL;
        addLog(`API: ${apiUrl}/api/account/qr/enter`);
        const qrCode = await makeQrCodeAndGetCode(email, true);

        if (qrCode && qrCode.code) {
          addLog(`코드 수신: ${qrCode.code}`);
          setCode(qrCode.code);

          // 딥링크 열기
          const serverUrl = `${process.env.NEXT_PUBLIC_API_URL}/api/account/qr/req/${qrCode.code}`;
          const deepLink = `oog://mobile/login/${encodeURIComponent(serverUrl)}`;
          addLog(`딥링크 실행`);
          window.location.href = deepLink;
          setIsAppOpened(true);

          // 폴링 시작
          addLog(`폴링 시작`);
          const intervalId = setInterval(async () => {
            const token = await qrCheckAndGetToken(email, qrCode.code);
            if (token == null) return;

            addLog(`토큰 수신!`);
            const cookies = [
              {name: AuthItems.Authorization, value: AuthItems.Bearer + token.idToken},
              {name: AuthItems.SocialLoginPlatform, value: token.socialLoginPlatform || "kakao"},
              {name: AuthItems.SocialAccessToken, value: token.accessToken},
              {name: AuthItems.SocialRefreshToken, value: token.refreshToken}
            ];
            if (await setBrowserCookies(cookies) == 200) {
              clearInterval(intervalId);
              addLog(`로그인 성공, 이동 중...`);
              router.push('/main');
            }
          }, 3000);
        } else {
          addLog(`에러: QR 응답 없음 - ${JSON.stringify(qrCode)}`);
        }
      } catch (error) {
        addLog(`에러: ${String(error)}`);
      }
    };

    fetchCodeAndOpenApp();
  }, [email, router, addLog]);

  return (
      <PageTransition>
        <div className="flex flex-col items-center justify-center min-h-screen bg-gray-50 px-4">
          <div className="bg-white p-8 rounded-lg shadow-sm w-full max-w-sm">
            <h1 className="text-xl font-bold mb-6 text-center text-gray-800">
              앱에서 로그인
            </h1>

            <div className="text-center mb-6">
              <p className="text-sm text-gray-500 mb-4">이메일: {email}</p>
            </div>

            <div className="flex flex-col items-center justify-center py-8">
              {isAppOpened ? (
                  <>
                    <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mb-4">
                      <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z" />
                      </svg>
                    </div>
                    <p className="text-gray-600 text-center font-medium">
                      앱에서 로그인을 완료해주세요
                    </p>
                    <p className="text-sm text-gray-400 mt-2 text-center">
                      로그인 완료 후 자동으로 이동됩니다
                    </p>
                  </>
              ) : (
                  <>
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4"></div>
                    <p className="text-gray-600 text-center">앱을 여는 중...</p>
                  </>
              )}
            </div>

            <div className="mt-6 space-y-3">
              <button
                  onClick={() => {
                    if (code) {
                      const serverUrl = `${process.env.NEXT_PUBLIC_API_URL}/api/account/qr/req/${code}`;
                      const deepLink = `oog://mobile/login/${encodeURIComponent(serverUrl)}`;
                      addLog(`수동 딥링크 실행`);
                      window.location.href = deepLink;
                    }
                  }}
                  className="w-full py-3 px-4 border border-blue-600 rounded-md text-sm font-medium text-blue-600 hover:bg-blue-50 transition duration-150 ease-in-out"
              >
                앱 다시 열기
              </button>
              <button
                  onClick={() => router.back()}
                  className="w-full py-2 text-gray-500 hover:text-gray-700 text-sm font-medium"
              >
                ← 이전으로 돌아가기
              </button>
            </div>

            {/* 디버그 로그 */}
            <div className="mt-6 p-3 bg-gray-100 rounded-md max-h-40 overflow-y-auto">
              <p className="text-xs font-bold text-gray-600 mb-2">디버그 로그:</p>
              {debugLogs.map((log, index) => (
                  <p key={index} className="text-xs text-gray-500 font-mono break-all">{log}</p>
              ))}
            </div>
          </div>
        </div>
      </PageTransition>
  );
};

const MobileLoginPage = () => {
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
        <MobileLoginContent/>
      </Suspense>
  );
};

export default MobileLoginPage;
