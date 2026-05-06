import {useEffect} from 'react';
import {useRouter} from 'next/navigation';
import {AuthItems} from "~/constants/auth-items";
import {apiFetch} from "~/lib/api-client";
import {ResponseItems} from "~/constants/response-items";
import {useMenuListStore} from "@/hooks/useMenuList";
import {getUserMenuList} from "@/utils/menu-utils";

const useAuth = () => {
  const router = useRouter();
  const setMenuList = useMenuListStore((state) => state.setMenuList);

  useEffect(() => {
    const abortController = new AbortController();

    const initializeAuth = async () => {
      try {
        const response = await apiFetch(`/api/account/auth/validate`, {
          method: "POST",
          signal: abortController.signal,
        });

        if (response.status === 401) {
          router.push('/login');
          return;
        }

        const responseBody = await response.json();
        const responseBodyString = JSON.stringify(responseBody.data);

        if (typeof window !== 'undefined') {
          localStorage.setItem(AuthItems.LastAuthInfo, responseBodyString);
        }

        if (responseBody.code !== ResponseItems.SUCC) {
          if (responseBody.code === ResponseItems.ECCL_101) {
            router.push("/ecclesia/apply");
            return;
          }

          if (responseBody.code === ResponseItems.ECCL_102) {
            router.push("/ecclesia/wait");
            return;
          }

          router.push('/login');
          return;
        }

        const menuList: MenuType[] = getUserMenuList();
        setMenuList(menuList);
      } catch (error) {
        if (error instanceof Error && error.name === 'AbortError') {
          return;
        }

        console.error('Auth initialization failed:', error);
        router.push('/login');
      }
    };

    initializeAuth();

    return () => {
      abortController.abort();
    };
  }, [router, setMenuList]);
};

export default useAuth;
