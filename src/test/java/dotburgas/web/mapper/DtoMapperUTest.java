package dotburgas.web.mapper;

import dotburgas.user.model.User;
import dotburgas.web.dto.UserEditRequest;
import dotburgas.web.dto.mapper.DtoMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DtoMapperUTest {

    // Test
    @Test
    void givenHappyPath_whenMappingUserToUserEditRequest() {

        // Given
        User user = User.builder()
                .firstName("Petar")
                .lastName("Matev")
                .email("petar_matev@yahoo.co.uk")
                .profilePicture("www.profilePicture.com")
                .build();
        // When
        UserEditRequest requestDto = DtoMapper.mapUserToUserEditRequest(user);

        // Then
        assertEquals(user.getFirstName(), requestDto.getFirstName());
        assertEquals(user.getLastName(), requestDto.getLastName());
        assertEquals(user.getEmail(), requestDto.getEmail());
        assertEquals(user.getProfilePicture(), requestDto.getProfilePicture());
     }
}
