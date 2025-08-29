import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TempBcryptGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "password123!";
        String encodedPassword = encoder.encode(password);
        System.out.println("Original password: " + password);
        System.out.println("BCrypt hash: " + encodedPassword);
        
        // 검증
        boolean matches = encoder.matches(password, encodedPassword);
        System.out.println("Verification: " + matches);
    }
}
