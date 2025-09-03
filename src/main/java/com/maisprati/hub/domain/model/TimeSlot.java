package com.maisprati.hub.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlot {

    private String time;
    private Boolean available;
    private Boolean booked;
}
