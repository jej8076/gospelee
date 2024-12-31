package com.gospelee.api.service;

import com.gospelee.api.dto.ecclesia.EcclesiaInsertDTO;
import com.gospelee.api.entity.Ecclesia;
import com.gospelee.api.repository.EcclesiaRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class EcclesiaServiceImpl implements EcclesiaService {

  private final EcclesiaRepository ecclesiaRepository;

  public EcclesiaServiceImpl(EcclesiaRepository ecclesiaRepository) {
    this.ecclesiaRepository = ecclesiaRepository;
  }

  public List<Ecclesia> getEcclesia() {
    return ecclesiaRepository.findAll();
  }

  public Ecclesia saveEcclesia(EcclesiaInsertDTO ecclesiaInsertDTO) {
    Ecclesia ecclesia = Ecclesia.builder()
        .name(ecclesiaInsertDTO.getName())
        .churchIdentificationNumber(ecclesiaInsertDTO.getChurchIdentificationNumber())
        .build();

    return ecclesiaRepository.save(ecclesia);
  }


}


