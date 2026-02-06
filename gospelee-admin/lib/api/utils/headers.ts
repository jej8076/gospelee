import {AuthItems} from "~/constants/auth-items";
import {getCookie} from "~/lib/cookie/cookie-utils";

export const authHeaders = async () => {
  return {
    "Content-Type": "application/json",
    [AuthItems.Authorization]:
        AuthItems.Bearer + (await getCookie(AuthItems.Authorization)),
    [AuthItems.SocialLoginPlatform]:
        (await getCookie(AuthItems.SocialLoginPlatform)) || "",
    [AuthItems.SocialAccessToken]:
        (await getCookie(AuthItems.SocialAccessToken)) || "",
    [AuthItems.SocialRefreshToken]:
        (await getCookie(AuthItems.SocialRefreshToken)) || "",
  };
};

export const authHeadersWithoutContentsType = async () => {
  return {
    [AuthItems.Authorization]:
        AuthItems.Bearer + (await getCookie(AuthItems.Authorization)),
    [AuthItems.SocialLoginPlatform]:
        (await getCookie(AuthItems.SocialLoginPlatform)) || "",
    [AuthItems.SocialAccessToken]:
        (await getCookie(AuthItems.SocialAccessToken)) || "",
    [AuthItems.SocialRefreshToken]:
        (await getCookie(AuthItems.SocialRefreshToken)) || "",
  };
};

