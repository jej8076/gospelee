export const ResponseItems = {
  SUCC: "100",
  ECCL_101: "ECCL-101",
  ECCL_102: "ECCL-102",
} as const;

export type ResponseItems = typeof ResponseItems[keyof typeof ResponseItems];
