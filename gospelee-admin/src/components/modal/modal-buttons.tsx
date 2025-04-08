export const grayButton = (text: string, onClick: () => void) => {
  return (
      <button onClick={onClick}
              className="px-4 py-2 bg-gray-200 text-gray-800 rounded hover:bg-gray-300 transition-colors">
        {text}
      </button>
  )
}

export const blueButton = (text: string, onClick: () => void) => {
  return (
      <button onClick={onClick}
              className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors">
        {text}
      </button>
  )
}
