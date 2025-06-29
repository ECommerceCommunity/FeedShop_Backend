package com.cMall.feedShop.user.domain.service; // 또는 적절한 패키지 경로

import com.cMall.feedShop.user.domain.model.User;
import com.cMall.feedShop.user.domain.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// 스프링 컨테이너에 빈으로 등록되도록 @Service 어노테이션을 붙입니다.
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // UserRepository를 주입받습니다.
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 여기서 'username' 파라미터는 React에서 보낸 'email' 값입니다.
        // 따라서, email로 사용자를 조회해야 합니다.
        User user = userRepository.findByEmail(username) // <-- findByLoginId 대신 findByEmail 사용
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        // User 엔티티가 UserDetails를 구현하고 있으므로, User 객체 자체를 반환할 수 있습니다.
        // UserDetails 구현 메서드들이 User.java에 올바르게 구현되어 있는지 다시 확인해야 합니다.
        // 특히 getAuthorities(), isEnabled() 등.
        return user;
    }

//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        // 데이터베이스에서 loginId를 사용하여 User 엔티티를 조회합니다.
//        User user = userRepository.findByLoginId(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with loginId: " + username));
//
//        return org.springframework.security.core.userdetails.User.builder()
//                .username(user.getLoginId())
//                .password(user.getPassword())
//                .authorities(user.getRole().name()) // UserRole ENUM의 이름을 문자열 권한으로 사용
//                .accountExpired(false) // 만료되지 않은 계정
//                .accountLocked(false)  // 잠기지 않은 계정
//                .credentialsExpired(false) // 비밀번호 만료되지 않음
//                .disabled(user.getStatus() != com.cMall.feedShop.user.domain.enums.UserStatus.ACTIVE) // 활성 상태에 따라 계정 비활성화 여부 결정
//                .build();
//    }

}