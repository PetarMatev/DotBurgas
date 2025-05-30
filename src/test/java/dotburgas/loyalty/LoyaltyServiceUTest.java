package dotburgas.loyalty;

import dotburgas.loyalty.model.Loyalty;
import dotburgas.loyalty.model.LoyaltyTier;
import dotburgas.loyalty.repository.LoyaltyRepository;
import dotburgas.loyalty.service.LoyaltyService;
import dotburgas.user.model.User;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoyaltyServiceUTest {

    @Mock
    private LoyaltyRepository loyaltyRepository;

    @InjectMocks
    private LoyaltyService loyaltyService;

    // 01. createDefaultLoyaltyProgram
    @Test
    void givenUserThatExistInDatabase_thenCreateDefaultLoyaltyProgram() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("Petar123")
                .build();

        Loyalty loyalty = Loyalty.builder()
                .id(UUID.randomUUID())
                .loyaltyTier(LoyaltyTier.LEVEL_01)
                .owner(user)
                .build();

        when(loyaltyRepository.save(any(Loyalty.class))).thenReturn(loyalty);

        // When
        Loyalty defaultLoyaltyProgram = loyaltyService.createDefaultLoyaltyProgram(user);

        // then
        assertEquals(loyalty, defaultLoyaltyProgram);
        assertEquals(loyalty.getLoyaltyTier(), defaultLoyaltyProgram.getLoyaltyTier());
        verify(loyaltyRepository, times(1)).save(any(Loyalty.class));
    }

    // 02. updatedLoyaltySubscription
    @Test
    void givenLoyaltyThatDoesNotExistInDatabase_thenThrowsException() {

        // Given
        UUID loyaltyId = UUID.randomUUID();
        LoyaltyTier loyaltyTier = LoyaltyTier.LEVEL_02;
        when(loyaltyRepository.findById(loyaltyId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> loyaltyService.updatedLoyaltySubscription(loyaltyId, loyaltyTier));
    }

    @Test
    void givenLoyaltyThatDoesNotExistInDatabase_thenReturnsLoyaltyEntity() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("Petar123")
                .build();

        UUID loyaltyId = UUID.randomUUID();
        LoyaltyTier loyaltyTier = LoyaltyTier.LEVEL_02;
        Loyalty loyalty = Loyalty.builder()
                .id(loyaltyId)
                .loyaltyTier(loyaltyTier)
                .owner(user)
                .updatedOn(LocalDateTime.now())
                .build();

        LoyaltyTier updatedLoyaltyTier = LoyaltyTier.LEVEL_03;

        when(loyaltyRepository.findById(loyaltyId)).thenReturn(Optional.of(loyalty));

        // When
        loyaltyService.updatedLoyaltySubscription(loyaltyId, updatedLoyaltyTier);

        // Then
        assertEquals(updatedLoyaltyTier, loyalty.getLoyaltyTier());
        assertNotNull(loyalty.getUpdatedOn());
        verify(loyaltyRepository, times(1)).findById(loyaltyId);
        verify(loyaltyRepository, times(1)).save(loyalty);
    }


    @Test
    void givenLoyaltyThatDoesNotExistInDatabase_thenUpdateLoyaltyDetails() {

        // Given
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("Petar123")
                .build();

        UUID loyaltyId = UUID.randomUUID();
        LoyaltyTier loyaltyTier = LoyaltyTier.LEVEL_02;

        Loyalty loyalty = Loyalty.builder()
                .id(loyaltyId)
                .loyaltyTier(loyaltyTier)
                .owner(user)
                .updatedOn(LocalDateTime.now())
                .build();

        LoyaltyTier updatedLoyaltyTier = LoyaltyTier.LEVEL_03;

        when(loyaltyRepository.findById(loyaltyId)).thenReturn(Optional.of(loyalty));
        when(loyaltyRepository.save(any(Loyalty.class))).thenReturn(loyalty);

        // When
        loyaltyService.updatedLoyaltySubscription(loyaltyId, updatedLoyaltyTier);

        // Then
        assertEquals(updatedLoyaltyTier, loyalty.getLoyaltyTier());
        assertNotNull(loyalty.getUpdatedOn());
        verify(loyaltyRepository, times(1)).findById(loyaltyId);
        verify(loyaltyRepository, times(1)).save(loyalty);
    }
}
