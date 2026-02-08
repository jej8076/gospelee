package com.gospelee.api.service;

import com.gospelee.api.dto.bible.AccountBibleWriteDTO;
import com.gospelee.api.dto.bible.BibleWriteStatsDTO;
import com.gospelee.api.entity.AccountBibleWrite;
import com.gospelee.api.entity.Bible;
import java.util.List;
import java.util.Optional;

public interface BibleService {

  Optional<List<Bible>> findByBookAndChapter(Integer book, Integer chapter);

  Optional<List<Bible>> findKorByShortLabelAndChapter(String short_label, Integer chapter);

  Optional<List<AccountBibleWrite>> findBibleWriteByPhone(String phone);

  Optional<AccountBibleWrite> saveBibleWrite(AccountBibleWriteDTO dto);

  BibleWriteStatsDTO getBibleWriteStats(Long accountUid);
}
