package com.gospelee.socket.dto.jwt;

import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class JwkSetDTO {

  private List<JwkDTO> keys;
}
