#!/bin/bash

#	1.	Next.js standalone 빌드
#	2.	Teleport(tsh)로 원격 로그인
#	3.	원격 디렉토리 생성 및 빌드 파일 업로드
#	4.	PM2용 실행 스크립트 생성 및 업로드
#	5.	원격 서버에서 PM2를 통해 앱 실행 또는 재시작

# 설정값
REMOTE_HOST="your.remote.host"         # Teleport에서 정의된 호스트
REMOTE_USER="your_user"                # 원격 사용자명
APP_NAME="nextjs-app"                  # PM2 프로세스 이름
REMOTE_DIR="/home/$REMOTE_USER/$APP_NAME"
START_SCRIPT="start-with-pm2.sh"

# 1. Next.js standalone 빌드
echo "🔧 Next.js 프로젝트 standalone 모드로 빌드 중..."
rm -rf .next out
npx next build

mkdir -p .next/standalone
cp -r .next/static .next/standalone/
cp -r public .next/standalone/
cp -r .next/server .next/standalone/
cp -r node_modules .next/standalone/
cp package.json .next/standalone/

# 2. Teleport 로그인
echo "🔐 Teleport 로그인 중..."
tsh login --proxy=myproxy.example.com --user=$REMOTE_USER

# 3. 원격 디렉토리 생성
echo "📁 원격 디렉토리 생성..."
tsh ssh $REMOTE_USER@$REMOTE_HOST "mkdir -p $REMOTE_DIR"

# 4. 빌드 파일 전송
echo "📤 빌드 파일 원격지로 전송 중..."
tsh scp -r .next/standalone/* $REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/

# 5. PM2 실행 스크립트 생성
echo "📝 PM2 실행 스크립트 생성 중..."
cat << EOF > $START_SCRIPT
#!/bin/bash
cd $REMOTE_DIR

# PM2 설치 여부 확인 후 설치
if ! command -v pm2 &> /dev/null
then
    echo "PM2가 설치되어 있지 않습니다. 설치 중..."
    npm install -g pm2
fi

# PM2로 앱 실행 (존재 시 재시작)
if pm2 list | grep -q "$APP_NAME"; then
    echo "PM2에서 기존 프로세스 재시작..."
    pm2 restart $APP_NAME
else
    echo "PM2로 새로운 프로세스 실행..."
    pm2 start server.js --name "$APP_NAME"
fi
EOF

chmod +x $START_SCRIPT

# 6. 실행 스크립트 전송
echo "🚀 PM2 실행 스크립트 전송 중..."
tsh scp $START_SCRIPT $REMOTE_USER@$REMOTE_HOST:$REMOTE_DIR/

# 7. 원격 실행
echo "▶️ PM2를 이용한 애플리케이션 실행 중..."
tsh ssh $REMOTE_USER@$REMOTE_HOST "bash $REMOTE_DIR/$START_SCRIPT"

# 완료
echo "✅ 배포 및 실행 완료!"