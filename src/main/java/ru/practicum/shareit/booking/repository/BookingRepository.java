package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;

import java.time.LocalDateTime;
import java.util.Collection;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Integer> {

    Collection<Booking> getBookingsByBookerIdOrderByIdDesc(Integer bookerId);

    @Query("select b from Booking b " +
            "left join Item i on i.id = b.itemId where i.ownerId =?1 order by b.start desc")
    Collection<Booking> getBookingsByOwnerId(Integer ownerId);

    @Query("select b from Booking b where b.bookerId = ?1 and b.start >= ?2 order by b.start desc")
    Collection<Booking> getFutureBookingsForUser(Integer bookerId, LocalDateTime now);

    @Query("select b from Booking b where b.bookerId = ?1 " +
            "and b.start <= current_timestamp and b.end >= current_timestamp order by b.start desc")
    Collection<Booking> getCurrentBookingForUser(Integer bookerId);


    Collection<Booking> getBookingsByBookerIdAndStatus(Integer bookerId, Status status);

    Collection<Booking> getBookingsByBookerIdAndEndBefore(Integer bookerId, LocalDateTime now);

    @Query("select b from Booking b left join Item i on b.itemId = i.id " +
            "where i.ownerId = ?1 and b.start >= current_timestamp " +
            "order by b.start desc")
    Collection<Booking> getFutureBookingForOwner(Integer ownerId);

    @Query("select b from Booking b " +
            "left join Item i on b.itemId = i.id where i.ownerId = ?1 and b.start <= current_timestamp " +
            "and b.end >= current_timestamp order by b.start desc")
    Collection<Booking> getCurrentBookingForOwner(Integer ownerId);

    @Query("select b from Booking b " +
            "left join Item i on b.itemId = i.id " +
            "where i.ownerId = ?1 and b.status = ?2 order by b.start desc")
    Collection<Booking> getBookingForOwnerByStatus(Integer ownerId, Status status);

    Collection<Booking> getBookingsByItemIdOrderByStartAsc(Integer itemId);

    Collection<Booking> getBookingsByBookerIdAndItemId(Integer bookerId, Integer itemId);

    @Query("select b from Booking b " +
            "left join Item i on b.itemId = i.id " +
            "where i.ownerId = ?1 and b.end <= ?2 order by b.start desc")
    Collection<Booking> getPastBookingForByOwnerId(Integer ownerId, LocalDateTime now);
}
