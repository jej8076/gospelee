export const isObject = (value: unknown): boolean => {
  return value !== null && typeof value === 'object' && !Array.isArray(value);
};

export const isEmpty = (value: unknown): boolean => {
  // null 이면 true
  if (value == null) return true;

  // string 타입이면서 공백이면 true
  if (typeof value === 'string' && value.trim() === '') return true;

  // array 타입이면서 배열에 담긴 게 없으면 true
  if (Array.isArray(value) && value.length === 0) return true;

  // object 타입이면서 선언된 key가 없으면 true
  if (isObject(value) && Object.keys(value).length === 0) return true;
  return false;
};

export const isValidPhoneNumber = (phone: string): boolean => {
  const phoneRegex = /^[0-9]{10,11}$/;
  return phoneRegex.test(phone);
};
