package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.util.Collection;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Integer> {

    Collection<ItemRequest> getDistinctByRequesterOrderByCreatedDesc(User requester);

    @Query("SELECT ir FROM ItemRequest ir WHERE ir.requester <> ?1")
    Page<ItemRequest> getDistinctByRequesterNotContainingOrderByCreatedDesc(User requester,
                                                                            Pageable pageable);
}
