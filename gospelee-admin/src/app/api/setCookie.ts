import type {NextApiRequest, NextApiResponse} from 'next';
import {setCookie} from '~/utils/cookie-utils';

export default function handler(req: NextApiRequest, res: NextApiResponse) {
  if (req.method === 'POST') {
    const {id_token} = req.body; // 요청 본문에서 id_token 추출

    if (!id_token) {
      return res.status(400).json({message: 'id_token is required'});
    }

    setCookie(res, 'id_token', id_token, {
      httpOnly: true,
      secure: process.env.NODE_ENV !== 'development',
      maxAge: 60 * 60 * 24 * 7, // 1 week
      path: '/',
    });

    res.status(200).json({message: 'Cookie set successfully'});
  } else {
    res.status(405).json({message: 'Method not allowed'});
  }
}
