package ru.practicum.shareit.booking.model;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Entity
@Table(name = "bookings", schema = "public")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @DateTimeFormat
    @Column(name = "start_date")
    private LocalDateTime start;
    @DateTimeFormat
    @Column(name = "end_date")
    private LocalDateTime end;
    private Integer itemId;
    private Integer bookerId;
    @Enumerated(EnumType.STRING)
    private Status status;
}
