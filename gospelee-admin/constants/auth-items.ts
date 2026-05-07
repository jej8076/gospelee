export const AuthItems = {
  LastAuthInfo: "last-auth-info",
} as const;

export type AuthItemsType = typeof AuthItems[keyof typeof AuthItems];
