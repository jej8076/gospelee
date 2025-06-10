"use client";

import React, {useEffect, useState} from 'react';
import dynamic from 'next/dynamic';
// import commands from '@uiw/react-md-editor'; // commands 임포트
// import commands from '@uiw/react-md-editor';
import MDEditor, {commands, ICommand} from "@uiw/react-md-editor";

// import ICommand from '@uiw/react-md-editor';
// import * as mdCommands from '@uiw/react-md-editor/lib/commands'; // <--- 이 부분이 핵심!

// SSR 문제를 피하기 위해 MDEditor 컴포넌트를 동적으로 임포트합니다.
// const MDEditor = dynamic(
//     () => import('@uiw/react-md-editor'),
//     {ssr: false} // SSR을 비활성화하여 클라이언트에서만 렌더링되도록 합니다.
// );

interface MarkdownEditorFieldProps {
  value: string;
  onMarkdownChange: (markdown: string) => void; // 변경될 때 콜백 함수
}

export default function MarkdownEditorField({
                                              value = '',
                                              onMarkdownChange
                                            }: MarkdownEditorFieldProps) {

  const handleEditorChange = (newValue?: string) => {
    const content = newValue ?? '';
    // setInternalValue(content); // internalValue를 업데이트할 필요 없음
    onMarkdownChange(content); // 부모 컴포넌트로 변경된 값 전달
  };

  // markdown 이미지 삽입 버튼 제거
  const filteredCommands = commands.getCommands().filter(
      (cmd: ICommand) => cmd.name !== 'image'
  );

  return (
      <div className="col-span-full">
        <div className="mt-2">
          <div data-color-mode="light">
            <MDEditor
                id="markdown-editor" // label의 htmlFor와 연결하기 위한 id
                value={value}
                onChange={handleEditorChange}
                height={600} // 에디터의 높이 설정 (원하는 대로 조절)
                preview="live" // 실시간 미리보기 (edit, preview도 가능)
                // 에디터의 외형을 `textarea`처럼 보이게 하려면 `outline` 속성을 추가할 수 있습니다.
                // 하지만 @uiw/react-md-editor는 자체적인 디자인을 가집니다.
                // 스타일을 추가하려면 `className` 또는 `wrapperProps`를 사용할 수 있습니다.
                // 예: wrapperProps={{ className: "border border-gray-300 rounded-md" }}
                className="custom-md-editor" // 여기에 커스텀 클래스 추가
                commands={filteredCommands}
            />
          </div>
        </div>
        <input type="hidden" name="본문" value={value}/> {/* 폼 제출을 위한 숨겨진 필드 */}
      </div>
  );
}
