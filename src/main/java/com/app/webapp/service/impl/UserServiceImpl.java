package com.app.webapp.service.impl;

import com.app.webapp.dto.model.user.UserDto;
import com.app.webapp.dto.request.user.ChangePasswordRequest;
import com.app.webapp.dto.request.user.EditUserRequest;
import com.app.webapp.error.ErrorDomains;
import com.app.webapp.error.exception.BadRequestException;
import com.app.webapp.error.exception.ResourceNotFound;
import com.app.webapp.event.OnResendVerificationEmailEvent;
import com.app.webapp.hal.user.UserDtoModelAssembler;
import com.app.webapp.model.user.User;
import com.app.webapp.repository.user.UserRepository;
import com.app.webapp.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.hateoas.CollectionModel;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final MessageSource messageSource;
    private final UserDtoModelAssembler assembler;
    private final PasswordEncoder bCryptPasswordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public UserServiceImpl(UserRepository userRepository, MessageSource messageSource, UserDtoModelAssembler assembler,
                           PasswordEncoder bCryptPasswordEncoder, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.messageSource = messageSource;
        this.assembler = assembler;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public CollectionModel<UserDto> findAll() {
        List<User> users = userRepository.findAll();
        if (users.isEmpty())
            throw new ResourceNotFound(messageSource.getMessage("users.notFound", null, LocaleContextHolder.getLocale()), ErrorDomains.USER);
        return assembler.toCollectionModel(map(users));
    }

    private CollectionModel<UserDto> map(List<User> users) {
        ModelMapper modelMapper = new ModelMapper();
        Collection<UserDto> collection = new ArrayList<>();
        for (User user: users) {
            collection.add(modelMapper.map(user, UserDto.class));
        }
        return new CollectionModel<>(collection);
    }

    @Override
    public UserDto findByUsername(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(
                () -> new ResourceNotFound(messageSource.getMessage("user.notFound", null, LocaleContextHolder.getLocale()), ErrorDomains.USER));
        return assembler.toModel(new ModelMapper().map(user, UserDto.class));
    }

    @Override
    public UserDto edit(EditUserRequest editUserRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName()).orElseThrow(
                () -> new ResourceNotFound(messageSource.getMessage("user.notFound", null, LocaleContextHolder.getLocale()), ErrorDomains.USER));
        user.setFirstName(editUserRequest.getFirstName());
        user.setLastName(editUserRequest.getLastName());
        setUsername(user, editUserRequest.getUsername());
        setEmail(user, editUserRequest.getEmail());
        return assembler.toModel(new ModelMapper().map(userRepository.save(user), UserDto.class));
    }

    private void setUsername(User user, String username) {
        if (user.getUsername().equals(username))
            return;
        if (userRepository.existsByUsername(username))
            throw new BadRequestException(messageSource.getMessage("username.exists", null, LocaleContextHolder.getLocale()), ErrorDomains.USER);

        user.setUsername(username);
        user.updateUpdatedAt();
    }

    private void setEmail(User user, String email) {
        if (user.getEmail().equals(email))
            return;
        if (userRepository.existsByEmail(email))
            throw new BadRequestException(messageSource.getMessage("email.exists", null, LocaleContextHolder.getLocale()), ErrorDomains.USER);

        user.setEmail(email);
        user.setActivated(false);
        user.updateUpdatedAt();
        eventPublisher.publishEvent(new OnResendVerificationEmailEvent(user));
    }

    @Override
    public void changePassword(ChangePasswordRequest changePasswordRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName()).orElseThrow(
                () -> new ResourceNotFound(messageSource.getMessage("user.notFound", null, LocaleContextHolder.getLocale()), ErrorDomains.USER));
        if (!bCryptPasswordEncoder.matches(changePasswordRequest.getOldPassword(), user.getPassword()))
            throw new BadRequestException(messageSource.getMessage("changePassword.badOldPassword", null, LocaleContextHolder.getLocale()), ErrorDomains.USER);
       changePassword(user, changePasswordRequest.getNewPassword());
    }

    private void changePassword(User user, String password) {
        user.setPassword(bCryptPasswordEncoder.encode(password));
        user.updateUpdatedAt();
        userRepository.save(user);
    }

    @Override
    public void logoutFromAllDevices() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName()).orElseThrow(
                () -> new ResourceNotFound(messageSource.getMessage("user.notFound", null, LocaleContextHolder.getLocale()), ErrorDomains.USER));
        user.updateLogoutFromAllDevicesAt();
        userRepository.save(user);
    }
}
