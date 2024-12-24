export const AuthItems = {
  LastAuthInfo: "last-auth-info",
  Bearer: "Bearer ",
  Authorization: "Authorization",
} as const;

export type AuthItemsType = typeof AuthItems[keyof typeof AuthItems];
