import type {Metadata, Viewport} from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'Gospelee - 성경과 함께하는 새로운 경험',
  description: 'Gospelee와 함께 성경을 더 깊이 이해하고, 일상 속에서 말씀을 실천해보세요.',
  keywords: '성경, 말씀, 묵상, 기도, 크리스천, 앱',
  authors: [{name: 'Gospelee Team'}],
  robots: 'index, follow',
  metadataBase: new URL('https://gospelee.com'),
  openGraph: {
    title: 'Gospelee - 성경과 함께하는 새로운 경험',
    description: 'Gospelee와 함께 성경을 더 깊이 이해하고, 일상 속에서 말씀을 실천해보세요.',
    type: 'website',
    locale: 'ko_KR',
  },
}

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
}

export default function RootLayout({
                                     children,
                                   }: {
  children: React.ReactNode
}) {
  return (
      <html lang="ko">
      <head>
        {/* 폰트 프리로드 */}
        <link
          rel="preload"
          href="/fonts/pretendard/Pretendard-Regular.otf"
          as="font"
          type="font/otf"
          crossOrigin="anonymous"
        />
        <link
          rel="preload"
          href="/fonts/pretendard/Pretendard-SemiBold.otf"
          as="font"
          type="font/otf"
          crossOrigin="anonymous"
        />
        <link
          rel="preload"
          href="/fonts/pretendard/Pretendard-Bold.otf"
          as="font"
          type="font/otf"
          crossOrigin="anonymous"
        />
      </head>
      <body>{children}</body>
      </html>
  )
}
