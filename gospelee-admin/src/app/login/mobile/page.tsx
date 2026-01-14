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
import {getCookies} from "~/lib/cookie/cookie-utils";

const MobileLoginContent = () => {
  const searchParams = useSearchParams();
  const router = useRouter();
  const email = searchParams.get('email');
  const [code, setCode] = useState('');
  const [isAppOpened, setIsAppOpened] = useState(false);
  const isReqRef = useRef(false);

  const startCheckingStatus = useCallback((email: string, code: string) => {
    const intervalId = setInterval(async () => {
      const token = await qrCheckAndGetToken(email, code);
      if (token == null) return;

      const cookies = [
        {name: AuthItems.Authorization, value: AuthItems.Bearer + token.idToken},
        // TODO 하드코딩 되어있음, 디바이스에서 로그인 플랫폼을 가져와야함
        {name: AuthItems.SocialLoginPlatform, value: "kakao"},
        {name: AuthItems.SocialAccessToken, value: token.accessToken},
        {name: AuthItems.SocialRefreshToken, value: token.refreshToken}
      ];
      if (await setBrowserCookies(cookies) == 200) {
        await getCookies([AuthItems.Authorization, AuthItems.SocialAccessToken, AuthItems.SocialRefreshToken]);
        clearInterval(intervalId);
        router.push('/main');
      }
    }, 3000);

    return () => clearInterval(intervalId);
  }, [router]);

  const openDeepLink = useCallback((code: string) => {
    const serverUrl = `${process.env.NEXT_PUBLIC_API_URL}/api/account/qr/req/${code}`;
    window.location.href = `oog://mobile/login/${encodeURIComponent(serverUrl)}`;
    setIsAppOpened(true);
  }, []);

  useEffect(() => {
    if (!email || isReqRef.current) return;

    isReqRef.current = true;

    const fetchCodeAndOpenApp = async () => {
      const qrCode = await makeQrCodeAndGetCode(email);

      if (qrCode) {
        setCode(qrCode.code);
        openDeepLink(qrCode.code);
        startCheckingStatus(email, qrCode.code);
      }
    };

    fetchCodeAndOpenApp();
  }, [email, startCheckingStatus, openDeepLink]);

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
              <div
                  className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mb-4"></div>
              <p className="text-gray-600 text-center">
                {isAppOpened
                    ? "앱에서 로그인을 기다리는 중..."
                    : "앱을 여는 중..."}
              </p>
              <p className="text-sm text-gray-400 mt-2 text-center">
                앱에서 로그인을 완료해주세요
              </p>
            </div>

            <div className="mt-6 space-y-3">
              <button
                  onClick={() => code && openDeepLink(code)}
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
