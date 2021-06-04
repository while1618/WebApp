package org.bootstrapbugz.api.user.dto;

import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
public class UserDto {
  private Long id;
  private String firstName;
  private String lastName;
  private String username;
  private String email;
  private boolean activated;
  private boolean nonLocked;
  private Set<RoleDto> roles;

  @Getter
  @Setter
  @NoArgsConstructor
  @EqualsAndHashCode
  public static final class RoleDto {
    private String name;
  }
}
