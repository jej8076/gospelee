'use client'
import {useMemo} from "react";
import 'react-quill/dist/quill.snow.css'
import dynamic from 'next/dynamic'

interface QuillEditorProps {
  value: string;
  onChange: (value: string) => void;
  height?: string; // 높이를 설정하기 위한 prop 추가
}

const formats = [
  'font',
  'header',
  'bold',
  'italic',
  'underline',
  'strike',
  'blockquote',
  'list',
  'bullet',
  'indent',
  'link',
  'align',
  'color',
  'background',
  'size',
  'h1',
];

const QuillWrapper = dynamic(() => import('react-quill'), {
  ssr: false,
  loading: () => <p>Loading ...</p>,
})

export default function QuillEditor({value, onChange, height = '200px'}: QuillEditorProps) {

  const modules = useMemo(
      () => ({
        toolbar: {
          container: [
            [{size: ['small', false, 'large', 'huge']}],
            [{align: []}],
            ['bold', 'italic', 'underline', 'strike'],
            [{list: 'ordered'}, {list: 'bullet'}],
            [
              {
                color: [],
              },
              {background: []},
            ],
          ],
        },
      }),
      []
  );

  return (
      <QuillWrapper
          theme="snow"
          modules={modules}
          formats={formats}
          value={value}
          onChange={onChange}
          style={{height}}
      />
  )
}
