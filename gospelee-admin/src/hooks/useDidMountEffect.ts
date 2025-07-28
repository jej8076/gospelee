import {useCallback, useEffect, useRef} from 'react';

// 마운트 후 의존성 변경 시에만 실행 (기존 동작 유지)
const useDidMountEffect = (func: () => void, deps: React.DependencyList) => {
  const didMount = useRef(false);
  const prevDeps = useRef<React.DependencyList>();

  useEffect(() => {
    // 첫 번째 마운트인지 확인
    if (!didMount.current) {
      didMount.current = true;
      prevDeps.current = deps;
      return;
    }

    // 의존성이 실제로 변경되었는지 확인
    const depsChanged = !prevDeps.current || 
      deps.length !== prevDeps.current.length ||
      deps.some((dep, index) => dep !== prevDeps.current![index]);

    if (depsChanged) {
      func();
      prevDeps.current = deps;
    }
  });
};

// 마운트 시 즉시 실행하는 새로운 훅
export const useOnMountEffect = (func: () => void) => {
  const memoizedFunc = useCallback(func, [func]);

  useEffect(() => {
    memoizedFunc();
  }, [memoizedFunc]);
};

export default useDidMountEffect;
