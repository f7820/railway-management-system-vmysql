package com.railway.railway_management_system.service;

import com.railway.railway_management_system.exceptions.ResourceNotFoundException;
import com.railway.railway_management_system.model.Route;
import com.railway.railway_management_system.repository.RouteRepository;
import com.railway.railway_management_system.requestDto.AddRouteRequest;
import com.railway.railway_management_system.requestDto.UpdateRouteRequest;
import com.railway.railway_management_system.responseDto.RouteDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RouteService implements IRouteService {

    private final RouteRepository routeRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<Route> getAllRoutes() {
        List<Route> routes = routeRepository.findAll();
        if (routes.isEmpty()) {
            throw new ResourceNotFoundException("No routes found!");
        }
        return routes;
    }

    @Override
    public RouteDto convertToDto(Route route) {
        return modelMapper.map(route, RouteDto.class);
    }

    @Override
    public Route getRouteById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found!"));
    }

    @Override
    public Route getRouteByRouteNumber(String routeNumber) {
        return routeRepository.findByRouteNumber(routeNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found!"));
    }

    @Override
    public List<Route> getRouteByRouteName(String routeName) {
        List<Route> routes = routeRepository.findByRouteName(routeName);
        if (routes.isEmpty()) {
            throw new ResourceNotFoundException("No routes found with name: " + routeName);
        }
        return routes;
    }

    @Override
    public List<Route> getRouteByStations(String startStation, String endStation) {
        List<Route> routes = routeRepository.findByStartStationAndEndStation(startStation, endStation);
        if (routes.isEmpty()) {
            throw new ResourceNotFoundException("No routes found between " + startStation + " and " + endStation);
        }
        return routes;
    }

    @Override
    public List<Route> getRouteByStartStation(String startStation) {
        List<Route> routes = routeRepository.findByStartStation(startStation);
        if (routes.isEmpty()) {
            throw new ResourceNotFoundException("No routes found from " + startStation);
        }
        return routes;
    }

    @Override
    public List<Route> getRouteByEndStation(String endStation) {
        List<Route> routes = routeRepository.findByEndStation(endStation);
        if (routes.isEmpty()) {
            throw new ResourceNotFoundException("No routes found to " + endStation);
        }
        return routes;
    }

    @Override
    public Route addRoute(AddRouteRequest request) {
        Route route = new Route();
        route.setRouteName(request.getRouteName());
        route.setStartStation(request.getStartStation());
        route.setEndStation(request.getEndStation());
        route.setDistanceKm(request.getDistanceKm());
        route.setStops(request.getStops());
        return routeRepository.save(route);
    }

    @Override
    public Route updateRoute(UpdateRouteRequest updateRequest, Long id) {
        Optional<Route> routeOptional = routeRepository.findById(id);

        if (routeOptional.isPresent()) {
            Route route = routeOptional.get();
            route.setRouteName(updateRequest.getRouteName());
            route.setStartStation(updateRequest.getStartStation());
            route.setEndStation(updateRequest.getEndStation());
            route.setDistanceKm(updateRequest.getDistanceKm());
            route.setStops(updateRequest.getStops());
            return routeRepository.save(route);
        } else {
            throw new ResourceNotFoundException("Route not found!");
        }
    }

    @Override
    public Route updateRouteByRouteNumber(UpdateRouteRequest updateRequest, String routeNumber) {
        Optional<Route> routeOptional = routeRepository.findByRouteNumber(routeNumber);

        if (routeOptional.isPresent()) {
            Route route = routeOptional.get();
            route.setRouteName(updateRequest.getRouteName());
            route.setStartStation(updateRequest.getStartStation());
            route.setEndStation(updateRequest.getEndStation());
            route.setDistanceKm(updateRequest.getDistanceKm());
            route.setStops(updateRequest.getStops());
            return routeRepository.save(route);
        } else {
            throw new ResourceNotFoundException("Route not found!");
        }
    }

    @Override
    public void deleteRouteById(Long id) {
        routeRepository.findById(id)
                .ifPresentOrElse(routeRepository::delete,
                        () -> {throw new ResourceNotFoundException("Route not found!");});
    }

    @Override
    public void deleteRouteByRouteNumber(String routeNumber) {
        routeRepository.findByRouteNumber(routeNumber)
                .ifPresentOrElse(routeRepository::delete,
                        () -> {throw new ResourceNotFoundException("Route not found!");});
    }
}
