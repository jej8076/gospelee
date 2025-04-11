// 색상 테마가 적용된 버튼 형태의 상태 선택기 (TSX)
import React, {useState} from 'react';
import {
  convertEcclesiaStatusType,
  EcclesiaStatus,
  EcclesiaStatusType,
  getEcclesiaStatusOptions
} from '@/enums/ecclesia/status';

export default function StatusSelector({currentStatus}: { currentStatus: string }) {
  const statusOptions = getEcclesiaStatusOptions();
  const [ecclesiaStatus, setEcclesiaStatus] = useState<EcclesiaStatusType>(convertEcclesiaStatusType(currentStatus));

  // 각 상태별 버튼 스타일 (활성화 상태)
  const getActiveButtonStyle = (status: string) => {
    switch (status) {
      case EcclesiaStatus.REQUEST:
        return 'bg-yellow-600 text-white border-yellow-600';
      case EcclesiaStatus.REJECT:
        return 'bg-red-600 text-white border-red-600';
      case EcclesiaStatus.APPROVAL:
        return 'bg-green-600 text-white border-green-600';
      default:
        return 'bg-gray-600 text-white border-gray-600';
    }
  };

  // 각 상태별 버튼 스타일 (비활성화 상태 - 호버 효과 포함)
  const getInactiveButtonStyle = (status: string) => {
    switch (status) {
      case EcclesiaStatus.REQUEST:
        return 'bg-white text-yellow-600 border-yellow-300 hover:bg-yellow-50';
      case EcclesiaStatus.REJECT:
        return 'bg-white text-red-600 border-red-300 hover:bg-red-50';
      case EcclesiaStatus.APPROVAL:
        return 'bg-white text-green-600 border-green-300 hover:bg-green-50';
      default:
        return 'bg-white text-gray-600 border-gray-300 hover:bg-gray-50';
    }
  };

  const changeHandler = (status: string) => {
    setEcclesiaStatus(convertEcclesiaStatusType(status))

    // 실제 구현 시에는 여기서 API 호출을 통해 상태 변경을 서버에 저장할 수 있습니다
    // saveEcclesiaStatus(selectedEcclesia.ecclesiaUid, newStatus);
  }

  return (
      <div className="status-selector mt-4">
        <div className="text-sm font-medium text-gray-700 mb-2">상태:</div>
        <div className="flex space-x-3">
          {statusOptions.map(option => (
              <button
                  key={option.value}
                  type="button"
                  onClick={() => changeHandler(option.value)}
                  className={`px-4 py-2 text-sm font-medium rounded-md border transition-all duration-200
                      ${ecclesiaStatus === option.value
                      ? getActiveButtonStyle(option.value)
                      : getInactiveButtonStyle(option.value)}`
                  }
              >
                {option.label}
              </button>
          ))}
        </div>
      </div>
  );
};
