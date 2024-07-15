/** @type {import('next').NextConfig} */
const nextConfig = {
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://13.124.200.73:8008/api/:path*',
      },
    ];
  },
};

export default nextConfig;
