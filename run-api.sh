#!/bin/bash

# 변수 설정
APP_NAME="gospelee-api"
APP_DIR="/home/jej/oog/api"
LOG_DIR="$APP_DIR/logs"
JAR_FILE="$APP_DIR/$APP_NAME.jar"

# 색상 정의
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# 로그 디렉토리 확인 및 생성
if [ ! -d "$LOG_DIR" ]; then
    mkdir -p "$LOG_DIR"
    echo -e "${GREEN}로그 디렉토리를 생성했습니다: $LOG_DIR${NC}"
fi

# JAR 파일 존재 확인
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${RED}Error: JAR 파일이 존재하지 않습니다: $JAR_FILE${NC}"
    exit 1
fi

# 실행 중인 프로세스 찾기 및 종료
echo -e "${YELLOW}실행 중인 $APP_NAME 프로세스를 확인합니다...${NC}"
PROCESS_ID=$(pgrep -f "java.*$APP_NAME.jar" || echo "")

if [ -n "$PROCESS_ID" ]; then
    echo -e "${YELLOW}실행 중인 $APP_NAME 프로세스($PROCESS_ID)를 종료합니다.${NC}"
    kill $PROCESS_ID

    # 프로세스가 완전히 종료될 때까지 대기
    for i in {1..10}; do
        if ! ps -p $PROCESS_ID > /dev/null; then
            break
        fi
        echo "프로세스 종료 대기 중... ($i/10)"
        sleep 1
    done

    # 여전히 실행 중이면 강제 종료
    if ps -p $PROCESS_ID > /dev/null; then
        echo -e "${RED}프로세스가 응답하지 않습니다. 강제 종료합니다.${NC}"
        kill -9 $PROCESS_ID
        sleep 1
    fi
else
    echo -e "${YELLOW}실행 중인 $APP_NAME 프로세스가 없습니다.${NC}"
fi

# 애플리케이션 실행
echo -e "${YELLOW}$APP_NAME을 시작합니다...${NC}"
cd "$APP_DIR"

# 현재 시간을 파일명에 추가하여 로그 파일 생성
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
APP_LOG="$LOG_DIR/application_$TIMESTAMP.log"
ERROR_LOG="$LOG_DIR/error_$TIMESTAMP.log"

# nohup을 사용하여 백그라운드에서 실행
nohup java -Dspring.profiles.active=prod -jar "$JAR_FILE" > "$APP_LOG" 2> "$ERROR_LOG" &

# 애플리케이션 시작 확인
sleep 5
NEW_PROCESS_ID=$(pgrep -f "java.*$APP_NAME.jar" || echo "")

if [ -n "$NEW_PROCESS_ID" ]; then
    echo -e "${GREEN}$APP_NAME이 성공적으로 시작되었습니다. (PID: $NEW_PROCESS_ID)${NC}"
    echo -e "${YELLOW}애플리케이션 로그: $APP_LOG${NC}"
    echo -e "${YELLOW}에러 로그: $ERROR_LOG${NC}"

    # 최근 로그 표시
    echo -e "${YELLOW}애플리케이션 로그 (최근 5줄):${NC}"
    tail -n 5 "$APP_LOG" 2>/dev/null || echo "로그가 아직 생성되지 않았습니다."

    # 심볼릭 링크 생성으로 최신 로그 파일 쉽게 접근
    ln -sf "$APP_LOG" "$LOG_DIR/latest.log"
    ln -sf "$ERROR_LOG" "$LOG_DIR/latest_error.log"

    exit 0
else
    echo -e "${RED}$APP_NAME 시작에 실패했습니다.${NC}"
    echo -e "${RED}오류 로그:${NC}"
    cat "$ERROR_LOG" 2>/dev/null || echo "오류 로그가 없습니다."
    exit 1
fi
