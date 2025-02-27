import {AuthItems} from "~/constants/auth-items";

export const getLastLoginOrElseNull = (): AuthInfoType => {
  const authInfoString: string | null = localStorage.getItem(AuthItems.LastAuthInfo);
  return authInfoString ? JSON.parse(authInfoString) : null;
};
