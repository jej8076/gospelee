'use client'

import useAuth from "~/lib/auth/check-auth";

export default function Main() {
  useAuth();

  return (
      <p>준비중 입니다</p>
  );
}
