package com.railway.railway_management_system.service;

import com.railway.railway_management_system.model.Route;
import com.railway.railway_management_system.requestDto.AddRouteRequest;
import com.railway.railway_management_system.requestDto.UpdateRouteRequest;
import com.railway.railway_management_system.responseDto.RouteDto;
import java.util.List;

public interface IRouteService {
    List<Route> getAllRoutes();
    RouteDto convertToDto(Route route);
    Route getRouteById(Long id);
    Route getRouteByRouteNumber(String routeNumber);
    List<Route> getRouteByRouteName(String routeName);
    List<Route> getRouteByStations(String startStation, String endStation);
    List<Route> getRouteByStartStation(String startStation);
    List<Route> getRouteByEndStation(String endStation);
    Route addRoute(AddRouteRequest request);
    Route updateRoute(UpdateRouteRequest updateRequest, Long id);
    Route updateRouteByRouteNumber(UpdateRouteRequest updateRequest, String routeNumber);
    void deleteRouteById(Long id);
    void deleteRouteByRouteNumber(String routeNumber);
}
