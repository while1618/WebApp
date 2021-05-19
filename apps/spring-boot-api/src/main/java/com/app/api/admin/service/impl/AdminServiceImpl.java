package com.app.api.admin.service.impl;

import com.app.api.admin.request.AdminRequest;
import com.app.api.admin.request.ChangeRoleRequest;
import com.app.api.admin.service.AdminService;
import com.app.api.shared.error.ErrorDomain;
import com.app.api.shared.error.exception.ResourceNotFound;
import com.app.api.user.dto.UserDto;
import com.app.api.user.mapper.UserMapper;
import com.app.api.user.model.Role;
import com.app.api.user.model.User;
import com.app.api.user.repository.RoleRepository;
import com.app.api.user.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminServiceImpl implements AdminService {
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final MessageSource messageSource;
  private final UserMapper userMapper;

  public AdminServiceImpl(
      UserRepository userRepository,
      RoleRepository roleRepository,
      MessageSource messageSource,
      UserMapper userMapper) {
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.messageSource = messageSource;
    this.userMapper = userMapper;
  }

  @Override
  public List<UserDto> findAllUsers() {
    List<User> users = userRepository.findAllForAdmin();
    if (users.isEmpty())
      throw new ResourceNotFound(
          messageSource.getMessage("users.notFound", null, LocaleContextHolder.getLocale()),
          ErrorDomain.USER);
    return userMapper.usersToUserDtos(users);
  }

  @Override
  public void changeRole(ChangeRoleRequest changeRoleRequest) {
    List<User> users = userRepository.findAllByUsernameIn(changeRoleRequest.getUsernames());
    users.forEach(
        user -> {
          List<Role> roles = roleRepository.findAllByNameIn(changeRoleRequest.getRoleNames());
          user.setRoles(new HashSet<>(roles));
          user.updateUpdatedAt();
        });
    userRepository.saveAll(users);
  }

  @Override
  public void lock(AdminRequest adminRequest) {
    List<User> users = userRepository.findAllByUsernameIn(adminRequest.getUsernames());
    users.forEach(
        user -> {
          user.setNonLocked(false);
          user.updateUpdatedAt();
        });
    userRepository.saveAll(users);
  }

  @Override
  public void unlock(AdminRequest adminRequest) {
    List<User> users = userRepository.findAllByUsernameIn(adminRequest.getUsernames());
    users.forEach(
        user -> {
          user.setNonLocked(true);
          user.updateUpdatedAt();
        });
    userRepository.saveAll(users);
  }

  @Override
  public void activate(AdminRequest adminRequest) {
    List<User> users = userRepository.findAllByUsernameIn(adminRequest.getUsernames());
    users.forEach(
        user -> {
          user.setActivated(true);
          user.updateUpdatedAt();
        });
    userRepository.saveAll(users);
  }

  @Override
  public void deactivate(AdminRequest adminRequest) {
    List<User> users = userRepository.findAllByUsernameIn(adminRequest.getUsernames());
    users.forEach(
        user -> {
          user.setActivated(false);
          user.updateUpdatedAt();
        });
    userRepository.saveAll(users);
  }

  @Override
  @Transactional
  public void delete(AdminRequest adminRequest) {
    List<User> users = userRepository.findAllByUsernameIn(adminRequest.getUsernames());
    users.forEach(userRepository::delete);
  }
}