#!/bin/bash

# 프로젝트 절대 경로
OOG_DEPLOY=/home/ubuntu/admin

# aws pem key 파일 절대 경로
PEM_FILE=/Users/jej/dev/aws/oog.pem

# jar 파일 파일명
JAR_FILE=gospelee-api-0.0.1-SNAPSHOT.jar

# aws 접속 정보
USER_HOST=ubuntu@13.124.200.73

./gradlew gospelee-api:bootJar

scp -i $PEM_FILE ./gospelee-api/build/libs/$JAR_FILE $USER_HOST:$OOG_DEPLOY
ssh -i $PEM_FILE $USER_HOST ${OOG_DEPLOY}/restart.sh
