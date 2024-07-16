import {NextRequest, NextResponse} from 'next/server';
import {cookies} from 'next/headers';

export async function GET(request: NextRequest) {
  const {searchParams} = new URL(request.url);
  const paramValue: string | null = searchParams.get('name');

  const cookieStore = cookies();
  const cookieValue = cookieStore.get(paramValue ?? '');

  return NextResponse.json({cookieValue: cookieValue ?? null});
}

export async function POST(request: NextRequest) {
  const {name, value} = await request.json();
  cookies().set({
    name,
    value,
    httpOnly: true,
    path: '/',
  });
  return NextResponse.json({code: 200, message: 'Cookie set success'});
}
