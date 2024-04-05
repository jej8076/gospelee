package com.gospelee.api.dto.jwt;

import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class JwkSet {

  private List<Jwk> keys;
}