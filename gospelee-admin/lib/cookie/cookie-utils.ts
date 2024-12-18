export const getCookie = async (cookieName: string) => {
  const response = await fetch(`/api-next/cookies?name=${cookieName}`, {
    method: 'GET',
  });

  if (!response.ok) {
    throw new Error('Failed to fetch cookie');
  }

  const data = await response.json();
  return data.cookieValue ? data.cookieValue.value : null;
};

export const setCookie = async (name: string, value: string) => {
  const response = await fetch('/api-next/cookies', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({name, value}),
  });

  if (!response.ok) {
    throw new Error('Failed to set cookie');
  }

  const data = await response.json();
  return data.code;
};

export const expireCookie = async (name: string) => {
  const response = await fetch('/api-next/cookies', {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({name: name}),
  });

  if (!response.ok) {
    throw new Error('Failed to expire cookie');
  }

  const data = await response.json();
  return data.code;
};
