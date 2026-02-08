import {AuthItems} from "~/constants/auth-items";
import {AppRouterInstance} from "next/dist/shared/lib/app-router-context.shared-runtime";
import {expireCookie} from "~/lib/cookie/cookie-utils";
import {isEmpty} from "@/utils/validators";
import {tryParseJson} from "@/utils/json-utils";

export const getLastLoginOrElseNull = (): AuthInfoType | null => {
  // 서버 사이드에서는 null 반환
  if (typeof window === 'undefined') {
    return null;
  }

  const authInfoString: string | null = localStorage.getItem(AuthItems.LastAuthInfo);
  const result = tryParseJson<AuthInfoType>(authInfoString);
  return result.success ? result.data : null;
};

export const logout = async (router: AppRouterInstance) => {

  try {
    // 토큰 만료
    await expireCookie(AuthItems.Authorization);
    await expireCookie(AuthItems.SocialLoginPlatform);
    await expireCookie(AuthItems.SocialAccessToken);
    await expireCookie(AuthItems.SocialRefreshToken);

    // 마지막 로그인 정보 제거 (클라이언트에서만)
    if (typeof window !== 'undefined') {
      localStorage.removeItem(AuthItems.LastAuthInfo);
    }

    // 이동
    await router.push('/login');
  } catch (error) {
    console.error('Error expiring cookie:', error);
  }
}
