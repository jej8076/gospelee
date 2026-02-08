import {NextRequest, NextResponse} from 'next/server';
import {cookies} from 'next/headers';

export async function GET(request: NextRequest) {
  const {searchParams} = new URL(request.url);
  const paramValue: string | null = searchParams.get('name');

  const cookieStore = await cookies();
  const cookieValue = cookieStore.get(paramValue ?? '');

  return NextResponse.json({cookieValue: cookieValue ?? null});
}

export async function POST(request: NextRequest) {
  let body;
  try {
    body = await request.json();
  } catch {
    return NextResponse.json({code: 400, message: 'Invalid JSON body'}, {status: 400});
  }

  const {name, value} = body;
  if (!name) {
    return NextResponse.json({code: 400, message: 'Cookie name is required'}, {status: 400});
  }

  (await cookies()).set({
    name,
    value: value ?? '',
    httpOnly: true,
    path: '/',
  });
  return NextResponse.json({code: 200, message: 'Cookie set success'});
}

export async function DELETE(request: NextRequest) {
  let body;
  try {
    body = await request.json();
  } catch {
    return NextResponse.json({code: 400, message: 'Invalid JSON body'}, {status: 400});
  }

  const {name} = body;
  if (!name) {
    return NextResponse.json({code: 400, message: 'Cookie name is required'}, {status: 400});
  }

  (await cookies()).set({
    name,
    value: '',
    httpOnly: true,
    path: '/',
    maxAge: 0,
  });

  return NextResponse.json({code: 200, message: 'Cookie expired successfully'});
}
