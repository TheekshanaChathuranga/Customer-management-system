package com.example.backend.controller;

import com.example.backend.model.City;
import com.example.backend.model.Country;
import com.example.backend.service.MasterDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/master")
public class MasterDataController {

    private final MasterDataService masterDataService;

    public MasterDataController(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @GetMapping("/countries")
    public ResponseEntity<List<Map<String, Object>>> getCountries() {
        List<Map<String, Object>> result = masterDataService.getAllCountries().stream()
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("name", c.getName());
                    map.put("code", c.getCode());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/cities")
    public ResponseEntity<List<Map<String, Object>>> getCities(
            @RequestParam(required = false) Long countryId) {

        List<City> cities = countryId != null ?
                masterDataService.getCitiesByCountry(countryId) :
                masterDataService.getAllCities();

        List<Map<String, Object>> result = cities.stream()
                .map(c -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", c.getId());
                    map.put("name", c.getName());
                    map.put("countryId", c.getCountry().getId());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
