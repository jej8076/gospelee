import {useEffect} from 'react';
import {useRouter} from 'next/navigation';
import {expireCookie, getCookie, getCookies} from '~/lib/cookie/cookie-utils';
import {Users} from "~/lib/api/fetch-users";
import {AuthItems} from "~/constants/auth-items";
import {apiFetch} from "~/lib/api-client";
import {ResponseItems} from "~/constants/response-items";
import {useMenuListStore} from "@/hooks/useMenuList";
import {getUserMenuList} from "@/utils/menu-utils";

type CookieItem = { name: string; value: string | null };

const useAuth = () => {
  const router = useRouter();
  const setMenuList = useMenuListStore((state) => state.setMenuList);

  useEffect(() => {
    const abortController = new AbortController();

    const initializeToken = async (): Promise<CookieItem[] | null> => {
      try {
        return await getCookies([AuthItems.Authorization, AuthItems.SocialAccessToken, AuthItems.SocialRefreshToken])
      } catch (error) {
        console.error('Failed to get token:', error);
        return null; // 실패 시 null 반환
      }
    };

    const auth = async (cookies: CookieItem[]): Promise<Users> => {

      const customHeaders: { [key: string]: string } = {};
      for (const cookie of cookies) {
        if (cookie.value) {
          customHeaders[cookie.name] = cookie.value;
        }
      }
      try {
        const response = await apiFetch(`/api/account/auth/validate`, {
          method: "POST",
          headers: customHeaders,
          signal: abortController.signal, // AbortController 신호 추가
        });

        const responseBody = await response.json();

        const responseBodyString = JSON.stringify(responseBody.data);

        // 클라이언트에서만 localStorage 사용
        if (typeof window !== 'undefined') {
          localStorage.setItem(AuthItems.LastAuthInfo, responseBodyString);
        }

        if (responseBody.code !== ResponseItems.SUCC) {

          // 계정이 존재하고 로그인에 성공했지만 교회가 없음
          if (responseBody.code === ResponseItems.ECCL_101) {
            router.push("/ecclesia/apply");
            return responseBody;
          }

          // 계정이 존재하고 로그인에 성공했고 교회도 있지만 교회가 승인되지 않음
          if (responseBody.code === ResponseItems.ECCL_102) {
            router.push("/ecclesia/wait");
            return responseBody;
          }

          // 토큰 만료
          await expireCookie(AuthItems.Authorization);

          router.push('/login');
          return responseBody;
        }

        return responseBody;
      } catch (error) {
        // AbortError는 정상적인 취소이므로 로그만 출력
        if (error instanceof Error && error.name === 'AbortError') {
          console.log('Auth request was aborted');
          return {} as Users; // 빈 객체 반환
        }

        console.error('Auth API call failed:', error);

        // 토큰 만료 처리
        await expireCookie(AuthItems.Authorization);

        // 에러를 다시 throw하여 상위에서 처리하도록 함
        throw error;
      }
    }

    const initializeAuth = async () => {
      try {
        const token = await initializeToken(); // 토큰을 직접 받아옴

        if (token === null) {
          router.push('/login');
          return;
        }

        await auth(token);

        const menuList: MenuType[] = getUserMenuList();
        setMenuList(menuList);
      } catch (error) {
        // AbortError는 정상적인 취소이므로 처리하지 않음
        if (error instanceof Error && error.name === 'AbortError') {
          console.log('Auth initialization was aborted');
          return;
        }

        console.error('Auth initialization failed:', error);

        // 토큰 만료 처리
        await expireCookie(AuthItems.Authorization);
        router.push('/login');
      }
    };

    initializeAuth();

    // Cleanup function: 컴포넌트 언마운트 시 요청 취소
    return () => {
      abortController.abort();
    };
  }, [router, setMenuList]);
};

export default useAuth;
