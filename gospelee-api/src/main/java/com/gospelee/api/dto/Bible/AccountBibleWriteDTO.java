package com.gospelee.api.dto.Bible;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class AccountBibleWriteDTO {

	private String phone;

	// 구약 = 1, 신약 = 2
	private int cate;

	private int book;

	// N장
	private int chapter;
}
