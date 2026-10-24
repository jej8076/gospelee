package com.gospelee.api.service.reaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.reaction.ReactionSummaryDTO;
import com.gospelee.api.enums.ReactionTargetType;
import com.gospelee.api.enums.ReactionType;
import com.gospelee.api.repository.jdbc.ReactionJdbcRepository;
import com.gospelee.api.repository.jpa.reaction.ReactionRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

class ReactionServiceImplTest {

  private static final ReactionType PRAY = ReactionType.PRAY;
  private static final ReactionTargetType ANN = ReactionTargetType.ANNOUNCEMENT;

  private ReactionRepository repository;
  private ReactionJdbcRepository jdbcRepository;
  private ReactionCounter counter;
  private ReactionTargetPolicy policy;
  private ReactionServiceImpl service;
  private AccountAuthDTO account;

  @BeforeEach
  void setUp() {
    repository = mock(ReactionRepository.class);
    jdbcRepository = mock(ReactionJdbcRepository.class);
    counter = mock(ReactionCounter.class);
    policy = mock(ReactionTargetPolicy.class);
    when(policy.targetType()).thenReturn(ANN);
    service = new ReactionServiceImpl(repository, jdbcRepository, counter, List.of(policy));
    account = AccountAuthDTO.builder().uid(7L).ecclesiaUid(2L).build();
    // 카운터는 요청받은 모든 id 에 대해 5 를 돌려준다고 가정
    when(counter.getCounts(eq(PRAY), eq(ANN), anyList(), any())).thenAnswer(inv -> {
      Map<Long, Long> counts = new HashMap<>();
      for (Long id : inv.<List<Long>>getArgument(2)) {
        counts.put(id, 5L);
      }
      return counts;
    });
  }

  @Test
  void 처음_누르면_카운터가_1_오른다() {
    when(jdbcRepository.insertIgnore("PRAY", "ANNOUNCEMENT", 10L, 7L)).thenReturn(1);

    ReactionSummaryDTO result = service.set(account, PRAY, ANN, 10L, true);

    verify(counter).applyDelta(PRAY, ANN, 10L, 1);
    assertTrue(result.isMine());
    assertEquals(5L, result.getCount());
  }

  @Test
  void 같은_요청이_겹쳐_이미_있으면_카운터를_올리지_않는다() {
    when(jdbcRepository.insertIgnore("PRAY", "ANNOUNCEMENT", 10L, 7L)).thenReturn(0);

    service.set(account, PRAY, ANN, 10L, true);

    verify(counter, never()).applyDelta(any(), any(), anyLong(), anyLong());
  }

  @Test
  void 취소하면_카운터가_1_내려간다() {
    when(repository.deleteOne("PRAY", "ANNOUNCEMENT", 10L, 7L)).thenReturn(1);

    ReactionSummaryDTO result = service.set(account, PRAY, ANN, 10L, false);

    verify(counter).applyDelta(PRAY, ANN, 10L, -1);
    assertFalse(result.isMine());
  }

  @Test
  void 이미_취소된_상태에서_또_취소해도_카운터는_그대로다() {
    when(repository.deleteOne("PRAY", "ANNOUNCEMENT", 10L, 7L)).thenReturn(0);

    service.set(account, PRAY, ANN, 10L, false);

    verify(counter, never()).applyDelta(any(), any(), anyLong(), anyLong());
  }

  @Test
  void 반응할_수_없는_대상이면_저장하지_않는다() {
    doThrow(new AccessDeniedException("x")).when(policy).assertReactable(account, 10L);

    assertThrows(AccessDeniedException.class, () -> service.set(account, PRAY, ANN, 10L, true));

    verify(jdbcRepository, never()).insertIgnore(any(), any(), anyLong(), anyLong());
  }

  @Test
  void 취소는_대상_규칙을_확인하지_않는다() {
    doThrow(new AccessDeniedException("x")).when(policy).assertReactable(account, 10L);
    when(repository.deleteOne("PRAY", "ANNOUNCEMENT", 10L, 7L)).thenReturn(1);

    service.set(account, PRAY, ANN, 10L, false);

    verify(counter).applyDelta(PRAY, ANN, 10L, -1);
  }

  @Test
  void 요약은_요청한_순서대로_내_반응_여부를_채운다() {
    when(repository.findMyTargetIds(eq(7L), eq("PRAY"), eq("ANNOUNCEMENT"), anyList()))
        .thenReturn(List.of(3L));

    List<ReactionSummaryDTO> result = service.summarize(7L, PRAY, ANN, List.of(2L, 3L, 2L));

    assertEquals(List.of(2L, 3L),
        result.stream().map(ReactionSummaryDTO::getTargetId).toList());
    assertFalse(result.get(0).isMine());
    assertTrue(result.get(1).isMine());
  }
}
