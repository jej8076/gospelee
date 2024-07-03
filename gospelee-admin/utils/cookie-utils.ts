import {NextApiResponse} from 'next';
import cookie from 'cookie';

export function setCookie(res: NextApiResponse, name: string, value: unknown, options: cookie.CookieSerializeOptions = {}) {
  const stringValue = typeof value === 'object' ? 'j:' + JSON.stringify(value) : String(value);

  if ('maxAge' in options) {
    options.expires = new Date(Date.now() + options.maxAge * 1000);
    options.maxAge = options.maxAge; // maxAge in seconds
  }

  res.setHeader('Set-Cookie', cookie.serialize(name, stringValue, options));
}
