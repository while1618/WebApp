package org.bootstrapbugz.api.admin.request;

import org.bootstrapbugz.api.shared.constants.Regex;
import java.util.Set;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AdminRequest {
  @NotEmpty(message = "{usernames.empty}")
  protected Set<@Pattern(regexp = Regex.USERNAME, message = "{username.invalid}") String> usernames;
}