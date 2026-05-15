package com.railway.railway_management_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.railway.railway_management_system.model.Route;

import java.util.List;
import java.util.Optional;

public interface RouteRepository extends JpaRepository<Route,Long> {
    
    Optional<Route> findByRouteNumber(String routeNumber);
    
    List<Route> findByRouteName(String routeName);
    
    List<Route> findByStartStationAndEndStation(String startStation, String endStation);
    
    List<Route> findByStartStation(String startStation);
    
    List<Route> findByEndStation(String endStation);
}
