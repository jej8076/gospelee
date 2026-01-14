export const AuthItems = {
  LastAuthInfo: "last-auth-info",
  Bearer: "Bearer ",
  Authorization: "Authorization",
  SocialLoginPlatform: "Social-Login-Platform",
  SocialAccessToken: "Social-Access-Token",
  SocialRefreshToken: "Social-Refresh-Token",

} as const;

export type AuthItemsType = typeof AuthItems[keyof typeof AuthItems];
