#!/bin/bash

# 변수 설정
PROJECT_DIR="$(pwd)"  # 현재 디렉토리를 프로젝트 디렉토리로 설정
MODULE_NAME="gospelee-api"
JAR_FILE="$PROJECT_DIR/$MODULE_NAME/build/libs/$MODULE_NAME.jar"

# Teleport 변수
TELEPORT_PROXY="teleport.oog.kr"
TARGET_HOST="localhost" # proxy 상태이기 때문에 localhost가 가능함
REMOTE_PATH="/home/jej/oog/api"
REMOTE_JAR="$REMOTE_PATH/$MODULE_NAME.jar"
REMOTE_SCRIPT="$REMOTE_PATH/run-api.sh"

# 변수 설정 부분에 로컬 스크립트 경로 추가
LOCAL_SCRIPT="$PROJECT_DIR/run-api.sh"  # 프로젝트 디렉토리에 있는 run-api.sh 파일
REMOTE_SCRIPT="$REMOTE_PATH/run-api.sh"

# 색상 정의
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 사용법 표시
function show_usage {
    echo -e "${BLUE}사용법:${NC}"
    echo -e "  $0               : 프로젝트 빌드만 실행"
    echo -e "  $0 deploy        : 프로젝트 빌드 후 원격 서버에 배포"
    echo
}

# 프로젝트 빌드 함수
function build_project {
    echo -e "${YELLOW}[Info] $MODULE_NAME 모듈 빌드를 시작합니다...${NC}"

    # Gradle 래퍼 존재 여부 확인
    if [ ! -f "$PROJECT_DIR/gradlew" ]; then
        echo -e "${RED}[Error] gradlew 파일을 찾을 수 없습니다. 올바른 프로젝트 디렉토리에서 실행 중인지 확인하세요.${NC}"
        exit 1
    fi

    # jar 태스크 실행
    cd "$PROJECT_DIR"
    ./gradlew $MODULE_NAME:build -x test

    # 빌드 결과 확인
    if [ $? -ne 0 ]; then
        echo -e "${RED}[Error] 빌드에 실패했습니다.${NC}"
        exit 1
    fi

    # JAR 파일 존재 확인
    if [ ! -f "$JAR_FILE" ]; then
        echo -e "${RED}[Error] 빌드된 JAR 파일($JAR_FILE)을 찾을 수 없습니다.${NC}"
        exit 1
    fi

    echo -e "${GREEN}[Success] $MODULE_NAME 모듈 빌드 완료: $JAR_FILE${NC}"
}

# 배포 함수
function deploy_project {
    echo -e "${YELLOW}[Info] 배포 프로세스를 시작합니다...${NC}"

    # 원격 실행 스크립트 생성
#    create_remote_script
    # create_remote_script 함수 호출 대신, 로컬 스크립트 존재 확인
    if [ ! -f "$LOCAL_SCRIPT" ]; then
        echo -e "${RED}[Error] 실행 스크립트($LOCAL_SCRIPT)를 찾을 수 없습니다.${NC}"
        exit 1
    fi

    echo -e "${YELLOW}[Info] Teleport 로그인을 진행합니다...${NC}"
    # Teleport tsh 로그인 (인터랙티브 로그인)
    tsh login --proxy=$TELEPORT_PROXY

    # 로그인 성공 확인
    if [ $? -ne 0 ]; then
        echo -e "${RED}[Error] Teleport 로그인에 실패했습니다. 배포를 중단합니다.${NC}"
        exit 1
    fi

    echo -e "${GREEN}[Success] Teleport 로그인 성공${NC}"

    # 원격 서버에 디렉토리가 존재하는지 확인
    echo -e "${YELLOW}[Info] 원격 서버의 디렉토리 확인...${NC}"
    tsh ssh $TARGET_HOST "if [ !-d \"$REMOTE_PATH\" ]; then mkdir -p \"$REMOTE_PATH\"; echo \"배포 디렉토리를 생성했습니다.\"; fi"

    echo -e "${YELLOW}[Info] JAR 파일을 원격 서버로 전송합니다...${NC}"

    # 파일 전송 (기존 파일 덮어쓰기)
    tsh scp $JAR_FILE $TARGET_HOST:$REMOTE_PATH/

    if [ $? -ne 0 ]; then
        echo -e "${RED}[Error] JAR 파일 전송에 실패했습니다.${NC}"
        exit 1
    fi

    echo -e "${YELLOW}[Info] 실행 스크립트를 원격 서버로 전송합니다...${NC}"

    # 실행 스크립트 전송
#    tsh scp /tmp/run-api.sh $TARGET_HOST:$REMOTE_SCRIPT
    # 실행 스크립트 전송 (로컬 파일 경로 변경)
    tsh scp $LOCAL_SCRIPT $TARGET_HOST:$REMOTE_SCRIPT

    if [ $? -ne 0 ]; then
        echo -e "${RED}[Error] 실행 스크립트 전송에 실패했습니다.${NC}"
        exit 1
    fi

    # 실행 권한 부여
    tsh ssh $TARGET_HOST "chmod +x $REMOTE_SCRIPT"

    echo -e "${GREEN}[Success] 파일 전송 완료${NC}"
    echo -e "${YELLOW}[Info] 원격 서버에서 애플리케이션을 실행합니다...${NC}"

    # 원격 실행 스크립트 실행
    tsh ssh $TARGET_HOST "$REMOTE_SCRIPT"

    if [ $? -ne 0 ]; then
        echo -e "${RED}[Error] 원격 서버에서 애플리케이션 실행에 실패했습니다.${NC}"
        exit 1
    fi

    echo -e "${GREEN}[Success] 배포가 성공적으로 완료되었습니다!${NC}"
    echo -e "${YELLOW}[Info] 애플리케이션 로그 확인: tsh ssh $TARGET_HOST 'tail -f $REMOTE_PATH/logs/latest.log'${NC}"
    echo -e "${YELLOW}[Info] 오류 로그 확인: tsh ssh $TARGET_HOST 'tail -f $REMOTE_PATH/logs/latest_error.log'${NC}"
}

# 메인 실행 로직
echo -e "${BLUE}===== $MODULE_NAME 빌드 및 배포 스크립트 =====${NC}"

# 빌드 실행
build_project

# 배포 여부 확인 및 실행
if [ "$1" = "deploy" ]; then
    deploy_project
else
    echo -e "${YELLOW}[Info] 배포를 실행하려면 'deploy' 파라미터를 추가하세요:${NC}"
    echo -e "  $0 deploy"
fi
