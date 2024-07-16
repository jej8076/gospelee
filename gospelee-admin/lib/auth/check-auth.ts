import {useEffect} from 'react';
import {useRouter} from 'next/navigation';
import {getCookie} from '~/lib/cookie/cookie-utils';

const useAuth = () => {
  const router = useRouter();

  useEffect(() => {
    const checkToken = async () => {
      const token = await getCookie('id_token');
      if (!token) {
        router.push('/login');
      }
    };

    checkToken();
  }, [router]);
};

export default useAuth;
