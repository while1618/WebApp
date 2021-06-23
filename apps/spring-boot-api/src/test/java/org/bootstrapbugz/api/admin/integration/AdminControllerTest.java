package org.bootstrapbugz.api.admin.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import org.bootstrapbugz.api.admin.request.AdminRequest;
import org.bootstrapbugz.api.admin.request.ChangeRoleRequest;
import org.bootstrapbugz.api.auth.redis.repository.RefreshTokenRepository;
import org.bootstrapbugz.api.auth.redis.repository.UserBlacklistRepository;
import org.bootstrapbugz.api.auth.request.LoginRequest;
import org.bootstrapbugz.api.auth.response.LoginResponse;
import org.bootstrapbugz.api.auth.util.AuthUtil;
import org.bootstrapbugz.api.auth.util.JwtUtil;
import org.bootstrapbugz.api.shared.constants.Path;
import org.bootstrapbugz.api.shared.util.TestUtil;
import org.bootstrapbugz.api.user.model.Role.RoleName;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(Lifecycle.PER_CLASS)
class AdminControllerTest {
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private RefreshTokenRepository refreshTokenRepository;
  @Autowired private UserBlacklistRepository userBlacklistRepository;

  private LoginResponse loginResponse;

  @BeforeAll
  void setUp() throws Exception {
    loginResponse = TestUtil.login(mockMvc, objectMapper, new LoginRequest("admin", "qwerty123"));
  }

  @AfterAll
  void cleanUp() {
    refreshTokenRepository.deleteById(
        JwtUtil.removeTokenTypeFromToken(loginResponse.getRefreshToken()));
    userBlacklistRepository.deleteById("user");
    userBlacklistRepository.deleteById("forUpdate1");
    userBlacklistRepository.deleteById("forUpdate2");
  }

  @Test
  void itShouldFindAllUsers() throws Exception {
    mockMvc
        .perform(
            get(Path.ADMIN + "/users")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AuthUtil.AUTH_HEADER, loginResponse.getToken()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(7));
  }

  @Test
  void itShouldChangeUsersRoles() throws Exception {
    ChangeRoleRequest changeRoleRequest =
        new ChangeRoleRequest(Set.of("user"), Set.of(RoleName.USER, RoleName.ADMIN));
    mockMvc
        .perform(
            put(Path.ADMIN + "/users/role")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AuthUtil.AUTH_HEADER, loginResponse.getToken())
                .content(objectMapper.writeValueAsString(changeRoleRequest)))
        .andExpect(status().isNoContent());
  }

  @ParameterizedTest
  @CsvSource({
    "lock, user",
    "unlock, locked",
    "deactivate, forUpdate1",
    "activate, notActivated",
  })
  void itShouldLockUnlockDeactivateActivateUsers(String path, String username) throws Exception {
    AdminRequest adminRequest = new AdminRequest(Set.of(username));
    mockMvc
        .perform(
            put(Path.ADMIN + "/users/" + path)
                .contentType(MediaType.APPLICATION_JSON)
                .header(AuthUtil.AUTH_HEADER, loginResponse.getToken())
                .content(objectMapper.writeValueAsString(adminRequest)))
        .andExpect(status().isNoContent());
  }

  @Test
  void itShouldDeleteUsers() throws Exception {
    AdminRequest adminRequest = new AdminRequest(Set.of("forUpdate2"));
    mockMvc
        .perform(
            delete(Path.ADMIN + "/users/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .header(AuthUtil.AUTH_HEADER, loginResponse.getToken())
                .content(objectMapper.writeValueAsString(adminRequest)))
        .andExpect(status().isNoContent());
  }
}
