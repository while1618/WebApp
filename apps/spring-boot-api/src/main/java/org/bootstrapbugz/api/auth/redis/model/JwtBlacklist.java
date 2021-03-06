package org.bootstrapbugz.api.auth.redis.model;

import java.io.Serial;
import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "JwtBlacklist")
public class JwtBlacklist implements Serializable {
  @Serial private static final long serialVersionUID = 7371548317284111557L;

  @Id private String token;

  @TimeToLive private long timeToLive;
}
