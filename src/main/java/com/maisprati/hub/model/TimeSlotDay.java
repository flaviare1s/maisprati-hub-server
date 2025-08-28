package com.maisprati.hub.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document(collection = "time_slots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeSlotDay {

    @Id
    private String id;

    private LocalDate date;
    private List<TimeSlot> slots;
}
