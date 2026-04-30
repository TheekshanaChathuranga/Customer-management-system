package com.example.backend.service;

import com.example.backend.model.City;
import com.example.backend.model.Country;
import com.example.backend.repository.CityRepository;
import com.example.backend.repository.CountryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MasterDataService {

    private final CountryRepository countryRepository;
    private final CityRepository cityRepository;

    public MasterDataService(CountryRepository countryRepository, CityRepository cityRepository) {
        this.countryRepository = countryRepository;
        this.cityRepository = cityRepository;
    }

    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }

    public List<City> getCitiesByCountry(Long countryId) {
        return cityRepository.findByCountryId(countryId);
    }

    public List<City> getAllCities() {
        return cityRepository.findAll();
    }
}
