package com.edutech.logisticsmanagementandtrackingsystem.dto;

public class CargoStatusResponse {

    private Long id;
    private String content;
    private String size;
    private String status;
    private String driverName;

    public CargoStatusResponse(
            Long id,
            String content,
            String size,
            String status,
            String driverName
    ) {
        this.id = id;
        this.content = content;
        this.size = size;
        this.status = status;
        this.driverName = driverName;
    }

    public Long getId() { return id; }
    public String getContent() { return content; }
    public String getSize() { return size; }
    public String getStatus() { return status; }
    public String getDriverName() { return driverName; }
}

