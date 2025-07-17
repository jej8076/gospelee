import {useEffect, useRef} from 'react';

// 마운트 후 의존성 변경 시에만 실행 (기존 동작 유지)
const useDidMountEffect = (func: () => void, deps: React.DependencyList) => {
  const didMount = useRef(false);

  useEffect(() => {
    if (didMount.current) {
      func();
    } else {
      didMount.current = true;
    }
  }, deps);
};

// 마운트 시 즉시 실행하는 새로운 훅
export const useOnMountEffect = (func: () => void) => {
  useEffect(() => {
    func();
  }, []);
};

export default useDidMountEffect;
