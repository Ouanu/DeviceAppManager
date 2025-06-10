package org.ouanu.manager.service;

import lombok.RequiredArgsConstructor;
import org.ouanu.manager.exception.DeviceNotFoundException;
import org.ouanu.manager.model.Device;
import org.ouanu.manager.repository.DeviceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceDetailsServiceImpl implements UserDetailsService {
    private final DeviceRepository deviceRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Device device = deviceRepository.findByUuid(username).orElseThrow(() -> new DeviceNotFoundException(HttpStatus.NOT_FOUND, "Device is not been found."));
            return new User(
                    device.getUsername(),
                    device.getPassword(),
                    device.getAuthorities()
            );
        } catch (DeviceNotFoundException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
