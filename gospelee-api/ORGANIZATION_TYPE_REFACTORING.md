# OrganizationType 리팩토링 가이드

## 변경 사항

### 기존 문제점
- `OrganizationType` enum이 Entity와 강하게 결합되어 있음
- 새로운 조직 타입 추가 시 반드시 Entity를 생성해야 함
- QueryDSL 쿼리가 Entity 존재를 전제로 작성됨

### 개선된 구조

#### 1. OrganizationType enum 단순화
```java
public enum OrganizationType {
  ECCLESIA("교회", true),        // Entity 있음
  MINISTRY("사역단체", false),    // Entity 없음
  DEPARTMENT("부서", false),     // Entity 없음
  SMALL_GROUP("소그룹", false);  // Entity 없음

  private final String displayName;
  private final boolean hasEntity; // Entity 존재 여부
}
```

#### 2. 유연한 쿼리 구조
- Entity가 있는 경우에만 JOIN 수행
- Entity가 없는 경우 고정 문자열 반환
- 타입별 분기 처리로 확장성 확보

#### 3. 조직 서비스 분리
- `OrganizationService`를 통해 조직명 조회 로직 분리
- Entity 존재 여부에 따른 처리 로직 캡슐화

## 사용법

### 새로운 조직 타입 추가 (Entity 없는 경우)

1. `OrganizationType` enum에 추가
```java
NEW_TYPE("새로운타입", false)
```

2. 추가 작업 없음 - 바로 사용 가능

### 새로운 조직 타입 추가 (Entity 있는 경우)

1. `OrganizationType` enum에 추가
```java
NEW_TYPE("새로운타입", true)
```

2. `AnnouncementRepositoryCustomImpl`에 JOIN 로직 추가
```java
case NEW_TYPE:
    query.leftJoin(QNewType.newType)
        .on(QAnnouncement.announcement.organizationId.eq(QNewType.newType.uid));
    break;
```

3. `OrganizationService`에 조회 로직 추가
```java
case NEW_TYPE:
    return newTypeRepository.findById(organizationId)
        .map(entity -> entity.getName())
        .orElse(organizationType.getDisplayName());
```

## 장점

1. **유연성**: Entity 없이도 새로운 조직 타입 추가 가능
2. **확장성**: Entity가 필요한 경우에만 추가 작업 수행
3. **성능**: 불필요한 JOIN 제거
4. **유지보수성**: 각 타입별 처리 로직이 명확히 분리됨

## 마이그레이션 가이드

기존 코드에서 `OrganizationType`의 `getEntity()`, `getNameField()`, `getIdField()` 메서드를 사용하던 부분은 새로운 구조로 변경해야 합니다.

### Before
```java
OrganizationType type = OrganizationType.fromName(organizationType);
queryFactory.select(...)
    .leftJoin(type.getEntity())
    .on(announcement.organizationId.eq(type.getIdField()))
```

### After
```java
OrganizationType type = OrganizationType.fromName(organizationType);
if (type.isHasEntity()) {
    // Entity별 개별 처리
}
```
