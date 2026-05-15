package com.railway.railway_management_system.responseDto;


import lombok.Data;
@Data
public class RouteDto {
   
    private String routeNumber; // i.e(RT001, RT009)

    private String routeName;
    
    private String startStation;

    private String endStation;
   
    private Double distanceKm;

    private Integer stops;
    
}
