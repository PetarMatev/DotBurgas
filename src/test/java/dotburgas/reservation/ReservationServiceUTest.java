package dotburgas.reservation;

import dotburgas.apartment.service.ApartmentService;
import dotburgas.reporting.service.ReportingService;
import dotburgas.reservation.repository.ReservationRepository;
import dotburgas.reservation.service.ReservationService;
import dotburgas.wallet.service.WalletService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSender;

@ExtendWith(MockitoExtension.class)
public class ReservationServiceUTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private ApartmentService apartmentService;
    @Mock
    private MailSender mailSender;
    @Mock
    private WalletService walletService;
    @Mock
    private ReportingService reportingService;

    @InjectMocks
    private ReservationService reservationService;

}
