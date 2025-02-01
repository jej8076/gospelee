import {useEffect} from 'react';
import {useRouter} from 'next/navigation';
import {expireCookie, getCookie} from '~/lib/cookie/cookie-utils';
import {Users} from "~/lib/api/fetch-users";
import {AuthItems} from "~/constants/auth-items";
import {apiFetch} from "~/lib/api-client";

const useAuth = () => {
  const router = useRouter();
  // const [token, setToken] = useState<string | null>(null);

  useEffect(() => {

    const initializeToken = async (): Promise<string | null> => {
      try {
        const cookieToken = await getCookie('Authorization'); // getCookie에서 await 사용
        // setToken(cookieToken);
        return cookieToken; // 토큰 값을 반환
      } catch (error) {
        console.error('Failed to get token:', error);
        // setToken(null);
        return null; // 실패 시 null 반환
      }
    };

    const auth = async (token: string): Promise<Users> => {
      const response = await apiFetch(`/api/account/auth`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `${AuthItems.Bearer}${token}`,
        },
      });

      const responseBody = await response.json();

      if (!response.ok || response.status !== 200) {
        await expireCookie(AuthItems.Authorization);

        // 계정이 존재하고 로그인에 성공했지만 교회가 없음
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
      // await initializeToken(); // 토큰 초기화
      const token = await initializeToken(); // 토큰을 직접 받아옴

      if (!token) {
        router.push('/login');
        return;
      }

      await auth(token);
    };

    initializeAuth();
  }, [router]);
};

export default useAuth;
