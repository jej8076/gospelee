import {AuthItems} from "~/constants/auth-items";
import {AppRouterInstance} from "next/dist/shared/lib/app-router-context.shared-runtime";
import {expireCookie} from "~/lib/cookie/cookie-utils";

export const getLastLoginOrElseNull = (): AuthInfoType => {
  const authInfoString: string | null = localStorage.getItem(AuthItems.LastAuthInfo);
  return authInfoString ? JSON.parse(authInfoString) : null;
};

export const logout = async (router: AppRouterInstance) => {

  try {
    // 토큰 만료
    await expireCookie(AuthItems.Authorization);

    // 마지막 로그인 정보 제거
    localStorage.removeItem(AuthItems.LastAuthInfo)

    // 이동
    await router.push('/login');
  } catch (error) {
    console.error('Error expiring cookie:', error);
  }
}
