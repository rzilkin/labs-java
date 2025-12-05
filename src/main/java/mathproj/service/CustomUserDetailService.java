package mathproj.service;

import mathproj.security.CustomUserDetails;
import mathproj.security.UserAccount;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailService implements UserDetailsService {

    private final InMemoryUserStore users;

    public CustomUserDetailService(InMemoryUserStore users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAccount acc = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Не найден"));

        var auths = acc.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.asAuthority()))
                .collect(Collectors.toSet());

        return new CustomUserDetails(acc.getId(), acc.getUsername(), acc.getPasswordHash(), auths);
    }
}



