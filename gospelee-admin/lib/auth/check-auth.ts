import {useEffect, useState} from 'react';
import {useRouter} from 'next/navigation';
import {expireCookie, getCookie} from '~/lib/cookie/cookie-utils';
import {Users} from "~/lib/api/fetch-users";
import {AuthItems} from "~/constants/auth-items";

const useAuth = () => {
  const router = useRouter();
  const [token, setToken] = useState<string | null>(null);

  useEffect(() => {

    const initializeToken = async () => {
      try {
        const cookieToken = await getCookie('Authorization'); // getCookie에서 await 사용
        setToken(cookieToken);
      } catch (error) {
        console.error('Failed to get token:', error);
        setToken(null);
      }
    };

    const checkToken = async () => {
      if (!token) {
        router.push('/login');
      }
    };

    const auth = async (): Promise<Users> => {
      const response = await fetch("/api/account/auth", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `${AuthItems.Bearer}${token}`,
        },
      });

      const responseBody = await response.json();

      if (!response.ok || responseBody.status !== 200) {
        await expireCookie(AuthItems.Authorization);

        if (responseBody.status === 403) {
          router.push("/apply/ecclesia");
          return responseBody;
        }
        
        router.push('/login');
        return responseBody;
      }

      const responseBodyString = JSON.stringify(responseBody);
      localStorage.setItem(AuthItems.LastAuthInfo, responseBodyString);

      return responseBody;
    };

    const initializeAuth = async () => {
      await initializeToken(); // 토큰 초기화
      if (token) {
        await checkToken();
        await auth();
      }
    };

    initializeAuth();
  }, [router, token]);
};

export default useAuth;
