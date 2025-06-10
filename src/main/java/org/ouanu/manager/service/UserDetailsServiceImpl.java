package org.ouanu.manager.service;

import lombok.RequiredArgsConstructor;
import org.ouanu.manager.model.User;
import org.ouanu.manager.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            User user = userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User isn't exists: " + username));
            return new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    new HashSet<>(Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole())))
            );
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }

//        return user;
    }




}
