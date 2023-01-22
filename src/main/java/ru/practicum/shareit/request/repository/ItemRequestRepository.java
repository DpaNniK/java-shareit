package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.model.Request;

import java.util.Collection;

@Repository
public interface ItemRequestRepository extends JpaRepository<Request, Integer> {

    Collection<Request> getDistinctByRequestorIdOrderByCreatedDesc(Integer requestorId);

    @Query("select r from Request r where r.requestorId <> ?1 order by r.created desc")
    Page<Request> getDistinctForPagination(Integer requestorId, Pageable pageable);
}
