package com.eduvault.user.service;
import com.eduvault.user.UserInfoUserDetails;

import com.eduvault.user.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserInfoUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        return repository.findByEmail(identifier)
                .or(() -> repository.findByMatricNumber(identifier))
                .map(UserInfoUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("user not found " + identifier));

    }
}
