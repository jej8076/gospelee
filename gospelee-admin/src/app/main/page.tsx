'use client'

import useAuth from "~/lib/auth/check-auth";

export default function Main() {
  useAuth();

  return (
      <p>대시보드</p>
  );
}
