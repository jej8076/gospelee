import {AuthItems} from "~/constants/auth-items";
import {AppRouterInstance} from "next/dist/shared/lib/app-router-context.shared-runtime";
import {apiFetch} from "~/lib/api-client";
import {tryParseJson} from "@/utils/json-utils";

export const getLastLoginOrElseNull = (): AuthInfoType | null => {
  if (typeof window === 'undefined') {
    return null;
  }

  const authInfoString: string | null = localStorage.getItem(AuthItems.LastAuthInfo);
  const result = tryParseJson<AuthInfoType>(authInfoString);
  return result.success ? result.data : null;
};

export const logout = async (router: AppRouterInstance) => {
  try {
    await apiFetch('/api/auth/logout', {method: 'POST'});

    if (typeof window !== 'undefined') {
      localStorage.removeItem(AuthItems.LastAuthInfo);
    }

    await router.push('/login');
  } catch (error) {
    console.error('Logout error:', error);
    await router.push('/login');
  }
}
