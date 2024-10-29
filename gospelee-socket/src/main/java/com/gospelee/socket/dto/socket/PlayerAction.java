package com.gospelee.socket.dto.socket;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PlayerAction {

  private String id;
  private Double positionX;
  private Double positionY;
  private String movement;

  @Builder
  public PlayerAction(String id, Double positionX, Double positionY, String movement) {
    this.id = id;
    this.positionX = positionX;
    this.positionY = positionY;
    this.movement = movement;
  }
}
