'use client'

import {useEffect} from 'react';
import {useRouter} from 'next/navigation';
import {getCookie} from "~/provider/CookieProvider";

export default function Main() {

  const router = useRouter();

  useEffect(() => {
    const token = getCookie("id_token")
    if (!token) {
      router.push('/login');
    }
  }, []);

  return (
      <p>대시보드</p>
  );
}
