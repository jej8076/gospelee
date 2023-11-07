package com.gospelee.api.entity.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@ToString
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class EditInfomation {
    @Column(name = "insert_user")
    private String insertUser;

    @Column(name = "update_user")
    private String updateUser;

    @CreatedDate
    @Column(name = "insert_time")
    private LocalDateTime insertTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
