package dotburgas.apartment.service;

import dotburgas.apartment.model.Apartment;
import dotburgas.apartment.repository.ApartmentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ApartmentService {

    private final ApartmentRepository apartmentRepository;

    @Autowired
    public ApartmentService(ApartmentRepository apartmentRepository) {
        this.apartmentRepository = apartmentRepository;
    }


    @Cacheable("apartments")
    public List<Apartment> getApartments() {
        return apartmentRepository.findAll();
    }

    @CacheEvict(value = "apartments", allEntries = true)
    public void save(Apartment apartment) {
        apartmentRepository.save(apartment);
    }

    public String findApartmentNameByID(UUID apartmentId) {
        Apartment apartment = apartmentRepository.findById(apartmentId).orElse(null);
        return apartment.getName();
    }


    public Apartment getById(UUID apartmentId) {
        return apartmentRepository.findById(apartmentId)
                .orElseThrow(() -> new EntityNotFoundException("Apartment not found with ID: " + apartmentId));
    }
}
