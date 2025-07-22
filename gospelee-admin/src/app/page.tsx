"use client"

import {useEffect} from 'react';
import {useRouter} from "next/navigation";

/**
 * redirect => "/" to "/main"
 * @constructor
 */
export default function Home() {
  const router = useRouter();

  useEffect(() => {
    router.push('/main');
  }, [router]);

  return null;
}
