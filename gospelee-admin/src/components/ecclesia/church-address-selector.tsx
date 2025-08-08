import React, {useState, useEffect} from 'react';
import {useApiClient} from "@/hooks/useApiClient";
import {fetchUpdateEcclesia} from "~/lib/api/fetch-ecclesias";

export default function ChurchAddressSelector({
                                                ecclesiaUid,
                                                churchAddress,
                                                onChurchAddressChange
                                              }: EcclesiaChurchAddressSelectorPropsWithCallback) {

  const [originalValue, setOriginalValue] = useState<string>(churchAddress || '');
  const [inputValue, setInputValue] = useState<string>(churchAddress || '');
  const {callApi} = useApiClient();

  // props가 변경될 때 원본값과 입력값 업데이트
  useEffect(() => {
    const newValue = churchAddress || '';
    setOriginalValue(newValue);
    setInputValue(newValue);
  }, [churchAddress]);

  // 값이 변경되었는지 확인
  const isChanged = inputValue !== originalValue;

  const saveChurchAddress = async (ecclesiaChurchAddressSelectorProps: EcclesiaChurchAddressSelectorProps) => {
    await callApi(() => fetchUpdateEcclesia(ecclesiaChurchAddressSelectorProps), () => {
    });
  }

  const applyHandler = () => {
    saveChurchAddress({
      ecclesiaUid: ecclesiaUid,
      churchAddress: inputValue
    }).then(r => {
      // 원본값을 새로운 값으로 업데이트 (버튼 비활성화를 위해)
      setOriginalValue(inputValue);

      // 부모 컴포넌트에 변경사항 셋팅
      onChurchAddressChange?.(inputValue);
    });
  }

  return (
      <div className="church-address-selector mt-4">
        <div className="text-sm font-medium text-gray-700 mb-2">교회 주소:</div>
        <div className="flex space-x-3">
          <input
              type="text"
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              className="flex-1 px-3 py-2 text-sm border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
              placeholder="교회 주소를 입력하세요"
          />
          <button
              type="button"
              onClick={applyHandler}
              disabled={!isChanged}
              className={`px-4 py-2 text-sm font-medium rounded-md border transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 ${
                  isChanged
                      ? 'text-white bg-indigo-600 border-indigo-600 hover:bg-indigo-700 cursor-pointer'
                      : 'text-gray-400 bg-gray-100 border-gray-300 cursor-not-allowed'
              }`}
          >
            적용
          </button>
        </div>
      </div>
  );
};
