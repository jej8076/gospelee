"use client";

import React, { useState } from "react";
import { QRCodeSVG } from "qrcode.react";
import Modal from "./modal";
import { blueButton, grayButton } from "./modal-buttons";

interface InviteModalProps {
  isOpen: boolean;
  onClose: () => void;
}

export default function InviteModal({ isOpen, onClose }: InviteModalProps) {
  const [activeTab, setActiveTab] = useState<'url' | 'qr'>('url');
  const [copySuccess, setCopySuccess] = useState(false);
  
  // TODO: 실제 환경에서는 환경변수나 설정에서 가져와야 함
  const inviteUrl = `${process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080'}/invite`;

  const handleCopyUrl = async () => {
    try {
      await navigator.clipboard.writeText(inviteUrl);
      setCopySuccess(true);
      setTimeout(() => setCopySuccess(false), 2000);
    } catch (err) {
      console.error('URL 복사 실패:', err);
    }
  };

  const handleDownloadQR = () => {
    const svg = document.getElementById('qr-code');
    if (!svg) return;

    const svgData = new XMLSerializer().serializeToString(svg);
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    const img = new Image();
    
    img.onload = () => {
      canvas.width = img.width;
      canvas.height = img.height;
      ctx?.drawImage(img, 0, 0);
      
      const pngFile = canvas.toDataURL('image/png');
      const downloadLink = document.createElement('a');
      downloadLink.download = 'gospelee-invite-qr.png';
      downloadLink.href = pngFile;
      downloadLink.click();
    };
    
    img.src = 'data:image/svg+xml;base64,' + btoa(svgData);
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="초대하기"
      footer={
        <>
          {grayButton("닫기", onClose)}
        </>
      }
    >
      <div className="w-full max-w-md mx-auto">
        {/* 탭 버튼 */}
        <div className="flex mb-6 bg-gray-100 rounded-lg p-1">
          <button
            onClick={() => setActiveTab('url')}
            className={`flex-1 py-2 px-4 rounded-md text-sm font-medium transition-colors ${
              activeTab === 'url'
                ? 'bg-white text-blue-600 shadow-sm'
                : 'text-gray-600 hover:text-gray-800'
            }`}
          >
            URL 복사하기
          </button>
          <button
            onClick={() => setActiveTab('qr')}
            className={`flex-1 py-2 px-4 rounded-md text-sm font-medium transition-colors ${
              activeTab === 'qr'
                ? 'bg-white text-blue-600 shadow-sm'
                : 'text-gray-600 hover:text-gray-800'
            }`}
          >
            QR 코드 공유
          </button>
        </div>

        {/* URL 복사 탭 */}
        {activeTab === 'url' && (
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">
                초대 링크
              </label>
              <div className="flex">
                <input
                  type="text"
                  value={inviteUrl}
                  readOnly
                  className="flex-1 px-3 py-2 border border-gray-300 rounded-l-md bg-gray-50 text-sm text-gray-600"
                />
                <button
                  onClick={handleCopyUrl}
                  className={`px-4 py-2 rounded-r-md text-sm font-medium transition-colors ${
                    copySuccess
                      ? 'bg-green-600 text-white'
                      : 'bg-blue-600 text-white hover:bg-blue-700'
                  }`}
                >
                  {copySuccess ? '복사됨!' : '복사'}
                </button>
              </div>
            </div>
            <p className="text-xs text-gray-500">
              이 링크를 통해 새로운 사용자를 초대할 수 있습니다.
            </p>
          </div>
        )}

        {/* QR 코드 탭 */}
        {activeTab === 'qr' && (
          <div className="space-y-4">
            <div className="flex flex-col items-center">
              <div className="bg-white p-4 rounded-lg border-2 border-gray-200">
                <QRCodeSVG
                  id="qr-code"
                  value={inviteUrl}
                  size={200}
                  level="M"
                  includeMargin={true}
                />
              </div>
              <button
                onClick={handleDownloadQR}
                className="mt-4 px-4 py-2 bg-blue-600 text-white rounded-md text-sm font-medium hover:bg-blue-700 transition-colors"
              >
                QR 코드 다운로드
              </button>
            </div>
            <p className="text-xs text-gray-500 text-center">
              QR 코드를 스캔하여 앱에 접속할 수 있습니다.
            </p>
          </div>
        )}
      </div>
    </Modal>
  );
}
