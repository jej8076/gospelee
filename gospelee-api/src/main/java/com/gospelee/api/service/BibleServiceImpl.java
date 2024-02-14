package com.gospelee.api.service;

import com.gospelee.api.entity.Account;
import com.gospelee.api.entity.AccountBibleWrite;
import com.gospelee.api.entity.Bible;
import com.gospelee.api.repository.AccountBibleWriteRepository;
import com.gospelee.api.repository.AccountRepository;
import com.gospelee.api.repository.BibleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BibleServiceImpl implements BibleService {

    private final AccountRepository accountRepository;

    private final BibleRepository bibleRepository;

    private final AccountBibleWriteRepository accountBibleWriteRepository;

    public BibleServiceImpl(AccountRepository accountRepository, BibleRepository bibleRepository, AccountBibleWriteRepository accountBibleWriteRepository) {
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
                .orElseThrow(() -> new IllegalArgumentException("phone에 대한 account 정보가 없음 [phone : " + phone));
        return accountBibleWriteRepository.findAllByAccountUid(account.getUid());
    }
}
