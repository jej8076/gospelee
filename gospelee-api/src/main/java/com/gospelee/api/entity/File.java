package com.gospelee.api.entity;

import com.gospelee.api.entity.common.EditInfomation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(
    name = "FILE_GENERATOR",
    sequenceName = "FILE_SEQ01",
    allocationSize = 1
)
@ToString
public class File extends EditInfomation {

  @Id
  @GeneratedValue(
      strategy = GenerationType.AUTO,
      generator = "FILE_GENERATOR"
  )
  private Long id;

  @Column
  private String accountUid;

  @Column
  private String category;

  @Column
  private Long totalCount;

  @Column
  private String delYn;

}
