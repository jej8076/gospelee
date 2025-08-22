import React from 'react';
import { PhotoIcon } from '@heroicons/react/24/solid';
import NextImage from 'next/image';

interface FileUploadProps {
  files: File[];
  existingFiles?: { url: string; name: string; id: number }[];
  onFileChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  onRemoveFile: (index: number, isExisting?: boolean) => void;
  onApplyToContent?: (index: number, file: File) => void;
  showApplyButton?: boolean;
  multiple?: boolean;
  accept?: string;
}

export default function FileUpload({
  files,
  existingFiles = [],
  onFileChange,
  onRemoveFile,
  onApplyToContent,
  showApplyButton = false,
  multiple = true,
  accept = "image/*"
}: FileUploadProps) {
  return (
    <div className="mt-2 flex flex-wrap gap-4 px-6 py-4 border border-gray-200 rounded-lg bg-gray-50">
      {/* 업로드 버튼 */}
      <label
        htmlFor="file-upload"
        className="cursor-pointer text-center border border-dashed border-gray-300 rounded-lg p-6 hover:border-gray-400 w-36 h-36 flex flex-col justify-center items-center bg-white"
      >
        <PhotoIcon aria-hidden="true" className="mx-auto size-12 text-gray-300" />
        <span className="mt-2 block text-sm font-semibold text-indigo-600 hover:text-indigo-500">
          파일 업로드
        </span>
        <input
          id="file-upload"
          name="file-upload"
          type="file"
          multiple={multiple}
          accept={accept}
          onChange={onFileChange}
          className="sr-only"
        />
      </label>

      {/* 기존 파일들 */}
      {existingFiles.map((file, index) => (
        <div
          key={`existing-${index}`}
          className="flex flex-col items-center justify-center space-y-2 relative"
        >
          <div className="w-36 h-36 border rounded-lg overflow-hidden bg-white flex items-center justify-center relative">
            <NextImage
              src={file.url}
              alt={file.name}
              className="object-cover w-full h-full"
              width={144}
              height={144}
            />
            <button
              type="button"
              onClick={() => onRemoveFile(index, true)} // isExisting = true
              className="absolute top-1 right-1 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs hover:bg-red-600"
              title="기존 파일 삭제"
            >
              ×
            </button>
          </div>
          <div className="flex flex-col items-center space-y-1">
            <span className="text-xs text-gray-500 text-center max-w-36 truncate">
              {file.name}
            </span>
            <span className="text-xs text-blue-600 font-medium">기존 파일</span>
          </div>
        </div>
      ))}

      {/* 새로 업로드된 파일들 */}
      {files.map((file, index) => (
        <div
          key={`new-${index}`}
          className="flex flex-col items-center justify-center space-y-2 relative"
        >
          <div className="w-36 h-36 border rounded-lg overflow-hidden bg-white flex items-center justify-center relative">
            <NextImage
              src={URL.createObjectURL(file)}
              alt={`preview-${index}`}
              className="object-cover w-full h-full"
              width={144}
              height={144}
            />
            <button
              type="button"
              onClick={() => onRemoveFile(index, false)} // isExisting = false
              className="absolute top-1 right-1 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs hover:bg-red-600"
              title="새 파일 삭제"
            >
              ×
            </button>
          </div>
          <div className="flex flex-col items-center space-y-1">
            <span className="text-xs text-gray-500 text-center max-w-36 truncate">
              {file.name}
            </span>
            <div className="flex items-center space-x-2">
              <span className="text-xs text-green-600 font-medium">새 파일</span>
              {showApplyButton && onApplyToContent && (
                <button
                  type="button"
                  className="rounded-md bg-indigo-600 px-2 py-1 text-xs font-semibold text-white shadow-xs hover:bg-indigo-500 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-indigo-600 cursor-pointer"
                  onClick={() => onApplyToContent(index, file)}
                >
                  적용
                </button>
              )}
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
