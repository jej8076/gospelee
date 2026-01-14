'use client'

import {useRouter} from 'next/navigation';
import {ChangeEvent, FormEvent, KeyboardEvent, useEffect, useRef, useState} from 'react';
import Image from 'next/image';
import {useMenuListStore} from "@/hooks/useMenuList";
import PageTransition from '@/components/PageTransition';
import {isMobile} from "@/utils/common-utils";

export default function Login() {
  const router = useRouter();
  const [email, setEmail] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const setMenuList = useMenuListStore((state) => state.setMenuList);
  const emailInputRef = useRef<HTMLInputElement>(null);

  // 페이지 로드 시 이메일 입력 필드에 자동 포커스
  useEffect(() => {
    if (emailInputRef.current) {
      emailInputRef.current.focus();
    }
  }, []);

  const handleEmailChange = (event: ChangeEvent<HTMLInputElement>) => {
    setEmail(event.target.value);
  };

  const handleLogin = async () => {
    if (isLoading) return; // 중복 클릭 방지

    setMenuList([]);

    if (email) {
      setIsLoading(true);

      // super@super.com인 경우 비밀번호 입력 페이지로 이동
      if (email === 'super@super.com') {
        setTimeout(() => {
          router.push(`/login/password?email=${encodeURIComponent(email)}`);
        }, 300);
      } else if (isMobile()) {
        // 모바일 사용자는 딥링크 로그인 페이지로 이동
        setTimeout(() => {
          router.push(`/login/mobile?email=${encodeURIComponent(email)}`);
        }, 300);
      } else {
        // 데스크톱 사용자는 QR 페이지로 이동
        setTimeout(() => {
          router.push(`/login/qr?email=${encodeURIComponent(email)}`);
        }, 300);
      }
    } else {
      alert('이메일 주소를 입력하세요.');
    }
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    handleLogin();
  };

  const handleKeyPress = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') {
      handleLogin();
    }
  };

  return (
      <PageTransition>
        <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
          <div className="sm:mx-auto sm:w-full sm:max-w-md">
            {/* 로고 */}
            <div className="flex justify-center mb-8">
              <Image
                  className="h-16 w-auto"
                  src="/images/logo/logo_oog.svg"
                  alt="Gospelee"
                  width={64}
                  height={64}
              />
            </div>

            {/* 로그인 폼 */}
            <div className="bg-white py-8 px-6 shadow-sm rounded-lg sm:px-10">
              <form className="space-y-6" onSubmit={handleSubmit}>
                <div>
                  <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                    이메일 주소
                  </label>
                  <input
                      id="email"
                      name="email"
                      type="email"
                      autoComplete="email"
                      required
                      ref={emailInputRef}
                      value={email}
                      onChange={handleEmailChange}
                      onKeyPress={handleKeyPress}
                      placeholder="이메일을 입력하세요"
                      className="appearance-none block w-full px-3 py-3 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-150 ease-in-out"
                  />
                </div>

                <div>
                  <button
                      type="submit"
                      disabled={isLoading}
                      className="w-full flex justify-center py-3 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-signature hover:bg-signatureHover focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition duration-150 ease-in-out disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {isLoading ? (
                        <div className="flex items-center">
                          <div
                              className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                          처리 중...
                        </div>
                    ) : (
                        '로그인'
                    )}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </PageTransition>
  );
}
