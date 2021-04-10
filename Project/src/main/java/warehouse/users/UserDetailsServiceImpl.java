package warehouse.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import warehouse.users.model.UserEntity;
import warehouse.users.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Primary
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Autowired
    public UserDetailsServiceImpl(UserService userService) {
        this.userService = userService;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<UserEntity> userOptional = this.userService.findUserByUsername(username);

        return userOptional.
                map(this::map).
                orElseThrow(() -> new UsernameNotFoundException("No such user " + username));
    }

    private UserDetails map(UserEntity userEntity) {

        List<GrantedAuthority> authorities = userEntity.
                getRoles().
                stream().
                map(r -> new SimpleGrantedAuthority(r.getRole().name())).
                collect(Collectors.toList());

        User user = new User(userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.isEnabled(),
                true,
                true,
                true,
                authorities);

        return user;


    }
}
