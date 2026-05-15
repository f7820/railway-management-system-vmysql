package com.railway.railway_management_system.controller;

import com.railway.railway_management_system.exceptions.ResourceNotFoundException;
import com.railway.railway_management_system.model.Route;
import com.railway.railway_management_system.response.ApiResponse;
import com.railway.railway_management_system.responseDto.RouteDto;
import com.railway.railway_management_system.service.IRouteService;
import com.railway.railway_management_system.requestDto.AddRouteRequest;
import com.railway.railway_management_system.requestDto.UpdateRouteRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("${api.prefix}/routes")
public class RouteController {

    private final IRouteService routeService;

    @GetMapping("/all")
    public ResponseEntity<ApiResponse> getAllRoutes() {
        try {
            List<Route> routes = routeService.getAllRoutes();
            List<RouteDto> routeDtos = routes.stream()
                    .map(routeService::convertToDto)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("All routes retrieved successfully", routeDtos));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getRouteById(@PathVariable Long id) {
        try {
            Route route = routeService.getRouteById(id);
            return ResponseEntity.ok(new ApiResponse("Route retrieved successfully",
                    routeService.convertToDto(route)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/routeNumber/{routeNumber}")
    public ResponseEntity<ApiResponse> getRouteByRouteNumber(
            @PathVariable String routeNumber) {
        try {
            Route route = routeService.getRouteByRouteNumber(routeNumber);
            return ResponseEntity.ok(new ApiResponse("Route retrieved successfully",
                    routeService.convertToDto(route)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/search/name")
    public ResponseEntity<ApiResponse> getRouteByRouteName(@RequestParam String routeName) {
        try {
            List<Route> routes = routeService.getRouteByRouteName(routeName);
            List<RouteDto> routeDtos = routes.stream()
                    .map(routeService::convertToDto)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("Routes retrieved successfully", routeDtos));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/search/stations")
    public ResponseEntity<ApiResponse> getRouteByStations(
            @RequestParam String startStation,
            @RequestParam String endStation) {
        try {
            List<Route> routes = routeService.getRouteByStations(startStation, endStation);
            List<RouteDto> routeDtos = routes.stream()
                    .map(routeService::convertToDto)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("Routes retrieved successfully", routeDtos));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/search/startStation")
    public ResponseEntity<ApiResponse> getRouteByStartStation(@RequestParam String startStation) {
        try {
            List<Route> routes = routeService.getRouteByStartStation(startStation);
            List<RouteDto> routeDtos = routes.stream()
                    .map(routeService::convertToDto)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("Routes retrieved successfully", routeDtos));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @GetMapping("/search/endStation")
    public ResponseEntity<ApiResponse> getRouteByEndStation(@RequestParam String endStation) {
        try {
            List<Route> routes = routeService.getRouteByEndStation(endStation);
            List<RouteDto> routeDtos = routes.stream()
                    .map(routeService::convertToDto)
                    .toList();
            return ResponseEntity.ok(new ApiResponse("Routes retrieved successfully", routeDtos));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PostMapping("/add")
    public ResponseEntity<ApiResponse> addRoute(@RequestBody AddRouteRequest request) {
        try {
            Route route = routeService.addRoute(request);
            return ResponseEntity.ok(new ApiResponse("Route added successfully",
                    routeService.convertToDto(route)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse("Invalid value: " + e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }


    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updateRoute(
            @RequestBody UpdateRouteRequest updateRequest,
            @PathVariable Long id) {
        try {
            Route route = routeService.updateRoute(updateRequest, id);
            return ResponseEntity.ok(new ApiResponse("Route updated successfully",
                    routeService.convertToDto(route)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PutMapping("/update/routeNumber/{routeNumber}")
    public ResponseEntity<ApiResponse> updateRouteByRouteNumber(
            @RequestBody UpdateRouteRequest updateRequest,
            @PathVariable String routeNumber) {
        try {
            Route route = routeService.updateRouteByRouteNumber(updateRequest, routeNumber);
            return ResponseEntity.ok(new ApiResponse("Route updated successfully",
                    routeService.convertToDto(route)));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deleteRouteById(@PathVariable Long id) {
        try {
            routeService.deleteRouteById(id);
            return ResponseEntity.ok(new ApiResponse("Route deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @DeleteMapping("/delete/routeNumber/{routeNumber}")
    public ResponseEntity<ApiResponse> deleteRouteByRouteNumber(
            @PathVariable String routeNumber) {
        try {
            routeService.deleteRouteByRouteNumber(routeNumber);
            return ResponseEntity.ok(new ApiResponse("Route deleted successfully", null));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(e.getMessage(), null));
        }
    }

}
