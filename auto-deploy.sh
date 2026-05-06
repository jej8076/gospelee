#!/bin/bash

# ============================================================
# Gospelee Auto Deploy Script
# - GHCR에서 최신 이미지 확인 후 변경 시 재배포
# - 로그: /var/log/gospelee-deploy/deploy.log
# ============================================================

# ─── 설정 ────────────────────────────────────────────────────
GHCR_REGISTRY="ghcr.io"
GITHUB_REPOSITORY="your-org/gospelee"   # 실제 repo로 변경 (예: myorg/gospelee)
GHCR_TOKEN=""                            # GHCR PAT (read:packages 권한) 또는 환경변수로 주입

COMPOSE_FILE="/opt/gospelee/docker-compose.yml"  # 실제 경로로 변경
COMPOSE_PROJECT="gospelee"

LOG_DIR="/var/log/gospelee-deploy"
LOG_FILE="${LOG_DIR}/deploy.log"
MAX_LOG_LINES=10000   # 로그 파일이 이 줄 수를 초과하면 rotate

IMAGES=(
  "gospelee-api"
  "gospelee-admin"
  "gospelee-landing"
)

# ─── 유틸 ────────────────────────────────────────────────────
mkdir -p "${LOG_DIR}"

log() {
  local level="$1"; shift
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] [${level}] $*" | tee -a "${LOG_FILE}"
}

rotate_log() {
  local lines
  lines=$(wc -l < "${LOG_FILE}" 2>/dev/null || echo 0)
  if [ "${lines}" -gt "${MAX_LOG_LINES}" ]; then
    local backup="${LOG_FILE}.$(date '+%Y%m%d_%H%M%S').bak"
    mv "${LOG_FILE}" "${backup}"
    gzip "${backup}" &
    log "INFO" "Log rotated → ${backup}.gz"
  fi
}

# ─── GHCR 토큰 로드 ──────────────────────────────────────────
# 환경변수 GHCR_TOKEN이 없으면 파일에서 읽기 (보안 권장)
load_token() {
  if [ -z "${GHCR_TOKEN}" ]; then
    local token_file="/etc/gospelee/.ghcr_token"
    if [ -f "${token_file}" ]; then
      GHCR_TOKEN=$(cat "${token_file}")
    else
      log "ERROR" "GHCR_TOKEN이 설정되지 않았습니다. /etc/gospelee/.ghcr_token 파일을 만들거나 환경변수를 설정하세요."
      exit 1
    fi
  fi
}

# ─── 원격 이미지 digest 조회 ─────────────────────────────────
get_remote_digest() {
  local image="$1"
  local url="https://${GHCR_REGISTRY}/v2/${GITHUB_REPOSITORY}/${image}/manifests/latest"

  local digest
  digest=$(curl -fsSL \
    -H "Authorization: Bearer ${GHCR_TOKEN}" \
    -H "Accept: application/vnd.docker.distribution.manifest.v2+json" \
    -H "Accept: application/vnd.oci.image.manifest.v1+json" \
    --head \
    "${url}" 2>/dev/null \
    | grep -i "docker-content-digest" \
    | awk '{print $2}' \
    | tr -d '\r')

  echo "${digest}"
}

# ─── 로컬 이미지 digest 조회 ─────────────────────────────────
get_local_digest() {
  local image="$1"
  local full_image="${GHCR_REGISTRY}/${GITHUB_REPOSITORY}/${image}:latest"

  docker inspect --format='{{index .RepoDigests 0}}' "${full_image}" 2>/dev/null \
    | awk -F'@' '{print $2}'
}

# ─── 메인 ────────────────────────────────────────────────────
main() {
  rotate_log
  log "INFO" "========== 배포 체크 시작 =========="
  load_token

  # GHCR 로그인
  echo "${GHCR_TOKEN}" | docker login "${GHCR_REGISTRY}" -u _token --password-stdin > /dev/null 2>&1
  if [ $? -ne 0 ]; then
    log "ERROR" "GHCR 로그인 실패"
    exit 1
  fi

  local updated=false

  for image in "${IMAGES[@]}"; do
    local full_image="${GHCR_REGISTRY}/${GITHUB_REPOSITORY}/${image}:latest"

    log "INFO" "[${image}] 원격 digest 확인 중..."
    local remote_digest
    remote_digest=$(get_remote_digest "${image}")

    if [ -z "${remote_digest}" ]; then
      log "WARN" "[${image}] 원격 digest 조회 실패. 스킵합니다."
      continue
    fi

    local local_digest
    local_digest=$(get_local_digest "${image}")

    log "INFO" "[${image}] 원격: ${remote_digest}"
    log "INFO" "[${image}] 로컬: ${local_digest:-없음}"

    if [ "${remote_digest}" != "${local_digest}" ]; then
      log "INFO" "[${image}] 새 이미지 감지 → pull 시작"
      if docker pull "${full_image}" >> "${LOG_FILE}" 2>&1; then
        log "INFO" "[${image}] pull 완료"
        updated=true
      else
        log "ERROR" "[${image}] pull 실패"
      fi
    else
      log "INFO" "[${image}] 변경 없음. 스킵합니다."
    fi
  done

  # 하나라도 업데이트됐으면 재배포
  if [ "${updated}" = true ]; then
    log "INFO" "변경된 이미지가 있어 재배포를 시작합니다."

    if [ ! -f "${COMPOSE_FILE}" ]; then
      log "ERROR" "docker-compose 파일을 찾을 수 없습니다: ${COMPOSE_FILE}"
      exit 1
    fi

    log "INFO" "docker compose down..."
    if docker compose -f "${COMPOSE_FILE}" -p "${COMPOSE_PROJECT}" down >> "${LOG_FILE}" 2>&1; then
      log "INFO" "docker compose down 완료"
    else
      log "ERROR" "docker compose down 실패"
      exit 1
    fi

    log "INFO" "docker compose up..."
    if docker compose -f "${COMPOSE_FILE}" -p "${COMPOSE_PROJECT}" up -d >> "${LOG_FILE}" 2>&1; then
      log "INFO" "docker compose up 완료 → 배포 성공"
    else
      log "ERROR" "docker compose up 실패 → 배포 실패"
      exit 1
    fi
  else
    log "INFO" "모든 이미지가 최신 상태입니다. 재배포 생략."
  fi

  log "INFO" "========== 배포 체크 종료 =========="
}

main "$@"
