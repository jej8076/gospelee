package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.bible.AccountBibleWriteDTO;
import com.gospelee.api.dto.bible.BibleWriteStatsDTO;
import com.gospelee.api.dto.bible.BookStatDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.AccountBibleWrite;
import com.gospelee.api.entity.Bible;
import com.gospelee.api.repository.jpa.account.AccountBibleWriteRepository;
import com.gospelee.api.repository.jpa.account.AccountRepository;
import com.gospelee.api.repository.jpa.bible.BibleRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class BibleServiceImpl implements BibleService {

  // 성경 66권 총 장 수 (1-66번 책 순서)
  private static final int[] BOOK_CHAPTERS = {
      50, 40, 27, 36, 34, 24, 21, 4, 31, 24,  // 창세기~사무엘하
      22, 25, 29, 36, 10, 13, 10, 42, 150, 31, // 열왕기상~잠언
      12, 8, 66, 52, 5, 48, 12, 14, 3, 9,     // 전도서~아모스
      1, 4, 7, 3, 3, 3, 2, 14, 4,             // 오바댜~말라기
      28, 16, 24, 21, 28, 16, 16, 13, 6, 6,   // 마태복음~에베소서
      4, 4, 5, 3, 6, 4, 3, 1, 13, 5,          // 빌립보서~야고보서
      5, 3, 5, 1, 1, 1, 22                    // 베드로전서~요한계시록
  };

  // 성경 66권 이름
  private static final String[] BOOK_NAMES = {
      "창세기", "출애굽기", "레위기", "민수기", "신명기", "여호수아", "사사기", "룻기", "사무엘상", "사무엘하",
      "열왕기상", "열왕기하", "역대상", "역대하", "에스라", "느헤미야", "에스더", "욥기", "시편", "잠언",
      "전도서", "아가", "이사야", "예레미야", "예레미야애가", "에스겔", "다니엘", "호세아", "요엘", "아모스",
      "오바댜", "요나", "미가", "나훔", "하박국", "스바냐", "학개", "스가랴", "말라기",
      "마태복음", "마가복음", "누가복음", "요한복음", "사도행전", "로마서", "고린도전서", "고린도후서", "갈라디아서", "에베소서",
      "빌립보서", "골로새서", "데살로니가전서", "데살로니가후서", "디모데전서", "디모데후서", "디도서", "빌레몬서", "히브리서", "야고보서",
      "베드로전서", "베드로후서", "요한일서", "요한이서", "요한삼서", "유다서", "요한계시록"
  };

  private static final int TOTAL_CHAPTERS = 1189;
  private static final int OLD_TESTAMENT_BOOKS = 39;

  private final AccountRepository accountRepository;

  private final BibleRepository bibleRepository;

  private final AccountBibleWriteRepository accountBibleWriteRepository;

  public BibleServiceImpl(AccountRepository accountRepository, BibleRepository bibleRepository,
      AccountBibleWriteRepository accountBibleWriteRepository) {
    this.accountRepository = accountRepository;
    this.bibleRepository = bibleRepository;
    this.accountBibleWriteRepository = accountBibleWriteRepository;
  }

  @Override
  public Optional<List<Bible>> findByBookAndChapter(Integer book, Integer chapter) {
    return bibleRepository.findByBookAndChapter(book, chapter);
  }

  @Override
  public Optional<List<Bible>> findKorByShortLabelAndChapter(String short_label, Integer chapter) {
    return bibleRepository.findByShortLabelAndChapter(short_label, chapter);
  }

  @Override
  public Optional<List<AccountBibleWrite>> findBibleWriteByPhone(String phone) {
    Account account = accountRepository.findByPhone(phone)
        .orElseThrow(
            () -> new IllegalArgumentException("phone에 대한 account 정보가 없음 [phone : " + phone));
    return accountBibleWriteRepository.findAllByAccountUid(account.getUid());
  }

  @Override
  public Optional<AccountBibleWrite> saveBibleWrite(AccountBibleWriteDTO bibleWriteDTO) {
    AccountAuthDTO account = AuthenticatedUserUtils.getAuthenticatedUserOrElseThrow();
    // 중복 체크 없이 단순 저장 (같은 book/chapter라도 새 레코드로 저장)
    return Optional.of(accountBibleWriteRepository.save(bibleWriteDTO.toEntity(account.getUid())));
  }

  @Override
  public BibleWriteStatsDTO getBibleWriteStats(Long accountUid) {
    List<Object[]> completedByBook = accountBibleWriteRepository.getCompletedChaptersByBook(accountUid);

    List<BookStatDTO> bookStats = new ArrayList<>();
    int totalCompleted = 0;
    int oldTestamentCount = 0;
    int newTestamentCount = 0;

    for (Object[] row : completedByBook) {
      int book = ((Number) row[0]).intValue();
      int completedChapters = ((Number) row[1]).intValue();

      // 유효한 책 번호인지 확인 (1-66)
      if (book < 1 || book > 66) {
        continue;
      }

      int bookIndex = book - 1;
      int totalChaptersForBook = BOOK_CHAPTERS[bookIndex];
      String bookName = BOOK_NAMES[bookIndex];

      bookStats.add(BookStatDTO.builder()
          .book(book)
          .bookName(bookName)
          .totalChapters(totalChaptersForBook)
          .completedChapters(completedChapters)
          .build());

      totalCompleted += completedChapters;

      // 구약/신약 구분 (1-39: 구약, 40-66: 신약)
      if (book <= OLD_TESTAMENT_BOOKS) {
        oldTestamentCount += completedChapters;
      } else {
        newTestamentCount += completedChapters;
      }
    }

    return BibleWriteStatsDTO.builder()
        .totalChapters(TOTAL_CHAPTERS)
        .completedChapters(totalCompleted)
        .oldTestamentCount(oldTestamentCount)
        .newTestamentCount(newTestamentCount)
        .bookStats(bookStats)
        .build();
  }
}
