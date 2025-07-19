import {useEffect, useRef, useCallback} from 'react';

// 마운트 후 의존성 변경 시에만 실행 (기존 동작 유지)
const useDidMountEffect = (func: () => void, deps: React.DependencyList) => {
  const didMount = useRef(false);
  const memoizedFunc = useCallback(func, [func]);

  useEffect(() => {
    if (didMount.current) {
      memoizedFunc();
    } else {
      didMount.current = true;
    }
  }, [memoizedFunc, deps]);
};

// 마운트 시 즉시 실행하는 새로운 훅
export const useOnMountEffect = (func: () => void) => {
  const memoizedFunc = useCallback(func, [func]);
  
  useEffect(() => {
    memoizedFunc();
  }, [memoizedFunc]);
};

export default useDidMountEffect;
