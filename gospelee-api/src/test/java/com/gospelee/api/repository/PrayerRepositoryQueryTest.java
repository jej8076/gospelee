package com.gospelee.api.repository;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.gospelee.api.entity.AccountBlock;
import com.gospelee.api.entity.PrayerCard;
import com.gospelee.api.entity.PrayerCardDraw;
import com.gospelee.api.entity.PrayerCardReport;
import com.gospelee.api.entity.PrayerCardResponse;
import com.gospelee.api.entity.PrayerSetting;
import com.gospelee.api.entity.PrayerSettingHistory;
import com.gospelee.api.entity.PrayerTreeEvent;
import com.gospelee.api.entity.PushGroupMember;
import com.gospelee.api.repository.jpa.prayer.PrayerCardDrawRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerCardReportRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerCardRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerCardResponseRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerSettingHistoryRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerSettingRepository;
import com.gospelee.api.repository.jpa.prayer.PrayerTreeEventRepository;
import com.gospelee.api.repository.jpa.pushnotification.PushGroupMemberRepository;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.GenericTypeResolver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.parser.PartTree;

/**
 * 기도 쪽지 리포지토리의 쿼리가 문법과 속성 이름 모두 맞는지 DB 없이 확인한다.
 * 운영(ddl-auto: none)에서는 앱이 뜰 때에야 잘못된 쿼리가 드러나므로 미리 막는다.
 */
class PrayerRepositoryQueryTest {

  private static final List<Class<? extends JpaRepository<?, ?>>> REPOSITORIES = List.of(
      PrayerCardRepository.class, PrayerCardResponseRepository.class,
      PrayerCardDrawRepository.class, PrayerCardReportRepository.class,
      PrayerSettingRepository.class, PrayerSettingHistoryRepository.class,
      PrayerTreeEventRepository.class, PushGroupMemberRepository.class);

  private static StandardServiceRegistry registry;
  private static SessionFactory sessionFactory;

  @BeforeAll
  static void bootHibernateWithoutDatabase() {
    registry = new StandardServiceRegistryBuilder()
        .applySettings(Map.of(
            "hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect",
            "hibernate.boot.allow_jdbc_metadata_access", "false",
            "hibernate.hbm2ddl.auto", "none"))
        .build();
    MetadataSources sources = new MetadataSources(registry);
    List.of(PrayerCard.class, PrayerCardResponse.class, PrayerCardDraw.class,
            PrayerCardReport.class, PrayerSetting.class, PrayerSettingHistory.class,
            PrayerTreeEvent.class, PushGroupMember.class, AccountBlock.class)
        .forEach(sources::addAnnotatedClass);
    sessionFactory = sources.buildMetadata().buildSessionFactory();
  }

  @AfterAll
  static void close() {
    if (sessionFactory != null) {
      sessionFactory.close();
    }
  }

  @Test
  void 직접_쓴_JPQL은_모두_해석된다() {
    int checked = 0;
    try (Session session = sessionFactory.openSession()) {
      for (Class<?> repository : REPOSITORIES) {
        for (Method method : repository.getDeclaredMethods()) {
          Query query = method.getAnnotation(Query.class);
          if (query != null && !query.nativeQuery()) {
            session.createQuery(query.value());
            checked++;
          }
        }
      }
    }
    assertTrue(checked > 0);
  }

  @Test
  void 메서드_이름으로_만든_쿼리의_속성_이름은_모두_엔티티에_있다() {
    List<String> checked = new ArrayList<>();
    for (Class<? extends JpaRepository<?, ?>> repository : REPOSITORIES) {
      Class<?> entity = GenericTypeResolver.resolveTypeArguments(repository,
          JpaRepository.class)[0];
      for (Method method : repository.getDeclaredMethods()) {
        if (method.getAnnotation(Query.class) != null) {
          continue;
        }
        // 속성 이름이 없으면 PartTree 가 예외를 던진다
        new PartTree(method.getName(), entity);
        checked.add(repository.getSimpleName() + "." + method.getName());
      }
    }
    assertTrue(checked.size() > 10, "검사한 메서드: " + checked);
  }
}
