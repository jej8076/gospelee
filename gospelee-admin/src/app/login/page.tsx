'use client'

import {useRouter} from 'next/navigation';
import {ChangeEvent, FormEvent, KeyboardEvent, useEffect, useRef, useState} from 'react';
import {setCookie} from "~/lib/cookie/cookie-utils";
import {AuthItems} from "~/constants/auth-items";
import {useMenuListStore} from "@/hooks/useMenuList";

export default function Login() {
  const router = useRouter();
  const [email, setEmail] = useState<string>('');
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

  const handleLogin = () => {
    setMenuList([]);

    if (email === 'super@super.com') {
      setCookie(AuthItems.Authorization, "SUPER").then(r => router.push(`/main`));
      return;
    }

    if (email) {
      router.push(`/login/qr?email=${encodeURIComponent(email)}`);
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
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8">
      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        {/* 로고 */}
        <div className="flex justify-center mb-8">
          <img
            className="h-16 w-auto"
            src="/images/logo/logo_oog.svg"
            alt="Gospelee"
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
                className="w-full flex justify-center py-3 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition duration-150 ease-in-out"
              >
                로그인
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
