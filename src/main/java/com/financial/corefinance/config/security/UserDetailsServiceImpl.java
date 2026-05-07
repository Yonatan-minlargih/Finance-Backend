package com.financial.corefinance.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final RestTemplate restTemplate;
    
    // This would typically connect to a user service or database
    // For now, we'll create a simple implementation that could be extended
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);
        
        try {
            // In a real implementation, this would call a user service
            // Map<String, Object> userResponse = restTemplate.getForObject(
            //     "http://user-service/api/v1/users/username/" + username, 
            //     Map.class);
            
            // For now, return a mock user - this should be replaced with actual user service integration
            if ("admin@corefinance.com".equals(username)) {
                return User.builder()
                    .username(username)
                    .password("$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.") // password: password
                    .roles("SYSTEM_ADMIN", "FINANCE_MANAGER")
                    .build();
            } else if ("accountant@corefinance.com".equals(username)) {
                return User.builder()
                    .username(username)
                    .password("$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.") // password: password
                    .roles("ACCOUNTANT")
                    .build();
            } else if ("manager@corefinance.com".equals(username)) {
                return User.builder()
                    .username(username)
                    .password("$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.") // password: password
                    .roles("FINANCE_MANAGER")
                    .build();
            } else if ("auditor@corefinance.com".equals(username)) {
                return User.builder()
                    .username(username)
                    .password("$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.") // password: password
                    .roles("AUDITOR")
                    .build();
            }
            
            throw new UsernameNotFoundException("User not found: " + username);
            
        } catch (Exception e) {
            log.error("Error loading user details for username: {}", username, e);
            throw new UsernameNotFoundException("User not found: " + username, e);
        }
    }
    
    // This method would be used to integrate with an actual user service
    private UserDetails createUserDetailsFromResponse(Map<String, Object> userResponse) {
        String username = (String) userResponse.get("username");
        String password = (String) userResponse.get("password");
        Boolean enabled = (Boolean) userResponse.getOrDefault("enabled", true);
        Boolean accountNonExpired = (Boolean) userResponse.getOrDefault("accountNonExpired", true);
        Boolean credentialsNonExpired = (Boolean) userResponse.getOrDefault("credentialsNonExpired", true);
        Boolean accountNonLocked = (Boolean) userResponse.getOrDefault("accountNonLocked", true);
        
        @SuppressWarnings("unchecked")
        java.util.List<String> roles = (java.util.List<String>) userResponse.getOrDefault("roles", 
            Collections.singletonList("USER"));
        
        return User.builder()
            .username(username)
            .password(password)
            .disabled(!enabled)
            .roles(roles.toArray(new String[0]))
            .build();
    }
}
