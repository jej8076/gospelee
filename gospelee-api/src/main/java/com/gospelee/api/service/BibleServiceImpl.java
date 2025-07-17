package com.gospelee.api.service;

import com.gospelee.api.dto.account.AccountAuthDTO;
import com.gospelee.api.dto.bible.AccountBibleWriteDTO;
import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.AccountBibleWrite;
import com.gospelee.api.entity.Bible;
import com.gospelee.api.repository.jpa.AccountBibleWriteRepository;
import com.gospelee.api.repository.jpa.AccountRepository;
import com.gospelee.api.repository.jpa.BibleRepository;
import com.gospelee.api.utils.AuthenticatedUserUtils;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class BibleServiceImpl implements BibleService {

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

    return Optional.of(
        accountBibleWriteRepository.findByUniqueConstraint(account.getUid(),
                bibleWriteDTO.getBook(), bibleWriteDTO.getChapter())
            .map(write -> {

              // 값이 있을 경우 count를 1올린다
              int result = accountBibleWriteRepository.increaseCountByIdx(write.getIdx());

              // 증가된 count가 적용된 최신 상태의 엔티티를 다시 조회
              return result > 0 ? accountBibleWriteRepository.findById(write.getIdx()).orElse(write)
                  : write;

              // 값이 없으면 데이터를 저장한다
            }).orElseGet(
                () -> accountBibleWriteRepository.save(bibleWriteDTO.toEntity(account.getUid()))));

  }
}
