package ireader.domain.usecases.remote

import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.remote.User
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Unit tests for authentication use cases
 * Tests sign in, sign up, and sign out functionality
 */
class AuthenticationUseCaseTest {
    
    private lateinit var signInUseCase: SignInUseCase
    private lateinit var signUpUseCase: SignUpUseCase
    private lateinit var signOutUseCase: SignOutUseCase
    private lateinit var remoteRepository: RemoteRepository
    
    @BeforeTest
    fun setup() {
        remoteRepository = mockk()
        signInUseCase = SignInUseCase(remoteRepository)
        signUpUseCase = SignUpUseCase(remoteRepository)
        signOutUseCase = SignOutUseCase(remoteRepository)
    }
    
    @AfterTest
    fun tearDown() {
        unmockkAll()
    }
    
    @Test
    fun `signIn should return success with valid credentials`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "password123"
        val expectedUser = User(
            id = "user-1",
            email = email,
            username = "testuser",
            createdAt = 0L
        )
        coEvery { remoteRepository.signIn(email, password) } returns Result.success(expectedUser)
        
        // When
        val result = signInUseCase(email, password)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())
        coVerify { remoteRepository.signIn(email, password) }
    }
    
    @Test
    fun `signIn should return failure with invalid credentials`() = runTest {
        // Given
        val email = "test@example.com"
        val password = "wrongpassword"
        val exception = Exception("Invalid credentials")
        coEvery { remoteRepository.signIn(email, password) } returns Result.failure(exception)
        
        // When
        val result = signInUseCase(email, password)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
    
    @Test
    fun `signUp should create new user successfully`() = runTest {
        // Given
        val email = "newuser@example.com"
        val password = "password123"
        val username = "newuser"
        val expectedUser = User(
            id = "user-2",
            email = email,
            username = username,
            createdAt = System.currentTimeMillis()
        )
        coEvery { remoteRepository.signUp(email, password, username) } returns Result.success(expectedUser)
        
        // When
        val result = signUpUseCase(email, password, username)
        
        // Then
        assertTrue(result.isSuccess)
        assertEquals(expectedUser, result.getOrNull())
        coVerify { remoteRepository.signUp(email, password, username) }
    }
    
    @Test
    fun `signUp should fail with existing email`() = runTest {
        // Given
        val email = "existing@example.com"
        val password = "password123"
        val username = "newuser"
        val exception = Exception("Email already exists")
        coEvery { remoteRepository.signUp(email, password, username) } returns Result.failure(exception)
        
        // When
        val result = signUpUseCase(email, password, username)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
    
    @Test
    fun `signOut should clear user session`() = runTest {
        // Given
        coEvery { remoteRepository.signOut() } returns Result.success(Unit)
        
        // When
        val result = signOutUseCase()
        
        // Then
        assertTrue(result.isSuccess)
        coVerify { remoteRepository.signOut() }
    }
    
    @Test
    fun `signOut should handle errors gracefully`() = runTest {
        // Given
        val exception = Exception("Network error")
        coEvery { remoteRepository.signOut() } returns Result.failure(exception)
        
        // When
        val result = signOutUseCase()
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
