package com.gospelee.api.dto.jwt;

import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class JwkSetDTO {

  private List<JwkDTO> keys;
}
