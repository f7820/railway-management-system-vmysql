package com.railway.railway_management_system.requestDto;

import lombok.Data;
@Data
public class AddRouteRequest {

    private String routeName;
    
    private String startStation;

    private String endStation;
   
    private Double distanceKm;

    private Integer stops;
    
}
