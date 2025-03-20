package dotburgas.apartment.service;

import dotburgas.apartment.model.Apartment;
import dotburgas.apartment.repository.ApartmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ApartmentService {

    private final ApartmentRepository apartmentRepository;

    @Autowired
    public ApartmentService(ApartmentRepository apartmentRepository) {
        this.apartmentRepository = apartmentRepository;
    }

    public String findApartmentNameByID(UUID apartmentId) {
        Apartment apartment = apartmentRepository.findById(apartmentId).orElseThrow(() -> new EntityNotFoundException("Apartment name not found with Id: " + apartmentId));
        return apartment.getName();
    }

    public Apartment getById(UUID apartmentId) {
        return apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new EntityNotFoundException("Apartment not found with Id: " + apartmentId));
    }

    @Cacheable("apartments")
    public List<Apartment> getApartments() {
        return apartmentRepository.findAll();
    }

    @CacheEvict(value = "apartments", allEntries = true)
    public void save(Apartment apartment) {

        if (apartment.getName() == null || apartment.getName().isBlank()) {
            throw new IllegalArgumentException("Apartment name cannot be empty!");
        }

        apartmentRepository.save(apartment);
        log.info("Apartment [%s] has been saved with Id: %s".formatted(apartment.getName(), apartment.getId()));
    }
}
