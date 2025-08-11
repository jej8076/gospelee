'use client'

import {useRouter, useSearchParams} from 'next/navigation';
import {ChangeEvent, FormEvent, KeyboardEvent, useEffect, useRef, useState} from 'react';
import Image from 'next/image';
import {setCookie} from "~/lib/cookie/cookie-utils";
import {AuthItems} from "~/constants/auth-items";
import {useMenuListStore} from "@/hooks/useMenuList";
import PageTransition from '@/components/PageTransition';

export default function PasswordLogin() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [password, setPassword] = useState<string>('');
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const setMenuList = useMenuListStore((state) => state.setMenuList);
  const passwordInputRef = useRef<HTMLTextAreaElement>(null);
  const email = searchParams.get('email');

  // 페이지 로드 시 비밀번호 입력 필드에 자동 포커스
  useEffect(() => {
    if (passwordInputRef.current) {
      passwordInputRef.current.focus();
    }
  }, []);

  // 이메일이 없으면 로그인 페이지로 리다이렉트
  useEffect(() => {
    if (!email) {
      router.push('/login');
    }
  }, [email, router]);

  const handlePasswordChange = (event: ChangeEvent<HTMLTextAreaElement>) => {
    setPassword(event.target.value);
  };

  const handleLogin = async () => {
    if (isLoading) return; // 중복 클릭 방지

    if (!password.trim()) {
      alert('비밀번호를 입력하세요.');
      return;
    }

    setIsLoading(true);
    setMenuList([]);

    try {
      await setCookie(AuthItems.Authorization, password.trim());
      router.push('/main');
    } catch (error) {
      console.error('로그인 처리 중 오류:', error);
      alert('로그인 처리 중 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    handleLogin();
  };

  const handleKeyPress = (event: KeyboardEvent<HTMLTextAreaElement>) => {
    if (event.key === 'Enter' && !event.shiftKey) {
      event.preventDefault();
      handleLogin();
    }
  };

  const handleBackToLogin = () => {
    router.push('/login');
  };

  if (!email) {
    return null; // 이메일이 없으면 아무것도 렌더링하지 않음
  }

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

            {/* 비밀번호 입력 폼 */}
            <div className="bg-white py-8 px-6 shadow-sm rounded-lg sm:px-10">
              <div className="mb-6">
                <p className="text-sm text-gray-600">
                  <span className="font-medium">{email}</span>로 로그인하시려면
                </p>
                <p className="text-sm text-gray-600 mt-1">
                  비밀번호를 입력해야 합니다.
                </p>
              </div>

              <form className="space-y-6" onSubmit={handleSubmit}>
                <div>
                  <label htmlFor="password"
                         className="block text-sm font-medium text-gray-700 mb-2">
                    비밀번호
                  </label>
                  <textarea
                      id="password"
                      name="password"
                      required
                      ref={passwordInputRef}
                      value={password}
                      onChange={handlePasswordChange}
                      onKeyPress={handleKeyPress}
                      placeholder="password"
                      rows={6}
                      className="appearance-none block w-full px-3 py-3 border border-gray-300 rounded-md placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition duration-150 ease-in-out resize-none font-mono text-sm"
                  />
                  <p className="mt-1 text-xs text-gray-500">
                    현재 입력된 문자 수: {password.length}
                  </p>
                </div>

                <div className="space-y-3">
                  <button
                      type="submit"
                      disabled={isLoading}
                      className="w-full flex justify-center py-3 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-signature hover:bg-signatureHover focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition duration-150 ease-in-out disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {isLoading ? (
                        <div className="flex items-center">
                          <div
                              className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                          로그인 중...
                        </div>
                    ) : (
                        '로그인'
                    )}
                  </button>

                  <button
                      type="button"
                      onClick={handleBackToLogin}
                      className="w-full flex justify-center py-3 px-4 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition duration-150 ease-in-out"
                  >
                    이메일 변경
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </PageTransition>
  );
}
