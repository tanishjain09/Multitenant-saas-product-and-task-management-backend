package om.tanish.saas.user;

import om.tanish.saas.tenant.Tenant;
import om.tanish.saas.tenant.TenantContext;
import om.tanish.saas.tenant.TenantRepository;
import om.tanish.saas.tenant.TenantStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private Tenant testTenant;
    private UUID testTenantId;
    private CreateUserRequest validRequest;

    @BeforeEach
    void setUp() {
        testTenantId = UUID.randomUUID();
        testTenant = new Tenant();
        testTenant.setId(testTenantId);
        testTenant.setTenantKey("test-tenant");
        testTenant.setName("Test Tenant");
        testTenant.setStatus(TenantStatus.ACTIVE);
        testTenant.setCreatedAt(Instant.now());

        validRequest = new CreateUserRequest();
        validRequest.setEmail("test@example.com");
        validRequest.setUsername("testuser");
        validRequest.setPassword("ValidPass123");
        validRequest.setRole("USER");
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void createUser_Success() {
        // Arrange
        TenantContext.setTenant(testTenantId);
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(userRepository.existsByTenantAndEmail(testTenant, validRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByTenantAndUsername(testTenant, validRequest.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setEmail(validRequest.getEmail());
        savedUser.setUsername(validRequest.getUsername());
        savedUser.setTenant(testTenant);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // Act
        User result = userService.createUser(validRequest);

        // Assert
        assertNotNull(result);
        assertEquals(validRequest.getEmail(), result.getEmail());
        assertEquals(validRequest.getUsername(), result.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_EmailAlreadyExists_ThrowsException() {
        // Arrange
        TenantContext.setTenant(testTenantId);
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(userRepository.existsByTenantAndEmail(testTenant, validRequest.getEmail())).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.createUser(validRequest)
        );
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Email already exists"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_UsernameAlreadyExists_ThrowsException() {
        // Arrange
        TenantContext.setTenant(testTenantId);
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(userRepository.existsByTenantAndEmail(testTenant, validRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByTenantAndUsername(testTenant, validRequest.getUsername())).thenReturn(true);

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.createUser(validRequest)
        );
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Username already exists"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_TenantContextMissing_ThrowsException() {
        // Arrange
        TenantContext.clear();

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.createUser(validRequest)
        );
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Tenant context missing"));
    }

    @Test
    void createUser_PasswordTooShort_ThrowsException() {
        // Arrange
        TenantContext.setTenant(testTenantId);
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(userRepository.existsByTenantAndEmail(testTenant, validRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByTenantAndUsername(testTenant, validRequest.getUsername())).thenReturn(false);

        validRequest.setPassword("Short1");

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.createUser(validRequest)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("Password must be at least 8 characters"));
    }

    @Test
    void createUser_PasswordNoUppercase_ThrowsException() {
        // Arrange
        TenantContext.setTenant(testTenantId);
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(userRepository.existsByTenantAndEmail(testTenant, validRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByTenantAndUsername(testTenant, validRequest.getUsername())).thenReturn(false);

        validRequest.setPassword("noupppercase123");

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.createUser(validRequest)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("uppercase letter"));
    }

    @Test
    void createUser_PasswordNoLowercase_ThrowsException() {
        // Arrange
        TenantContext.setTenant(testTenantId);
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(userRepository.existsByTenantAndEmail(testTenant, validRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByTenantAndUsername(testTenant, validRequest.getUsername())).thenReturn(false);

        validRequest.setPassword("NOLOWERCASE123");

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.createUser(validRequest)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("lowercase letter"));
    }

    @Test
    void createUser_PasswordNoDigit_ThrowsException() {
        // Arrange
        TenantContext.setTenant(testTenantId);
        when(tenantRepository.findById(testTenantId)).thenReturn(Optional.of(testTenant));
        when(userRepository.existsByTenantAndEmail(testTenant, validRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByTenantAndUsername(testTenant, validRequest.getUsername())).thenReturn(false);

        validRequest.setPassword("NoDigitPass");

        // Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> userService.createUser(validRequest)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertTrue(exception.getReason().contains("digit"));
    }

}