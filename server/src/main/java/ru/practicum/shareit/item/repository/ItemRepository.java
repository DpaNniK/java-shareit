package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    Collection<Item> findItemsByOwnerId(Integer ownerId);

    Page<Item> findItemsByOwnerId(Integer ownerId, Pageable pageable);

    @Query("select i from Item i " +
            "where i.available = ?2 and upper(i.name) like upper(concat('%', ?1, '%')) " +
            "or i.available = ?2 and upper(i.description) like upper(concat('%', ?1, '%'))")
    Collection<Item> searchByText(String text, Boolean access);

    @Query("select i from Item i " +
            "where i.available = ?2 and upper(i.name) like upper(concat('%', ?1, '%')) " +
            "or i.available = ?2 and upper(i.description) like upper(concat('%', ?1, '%'))")
    Page<Item> searchByText(String text, Boolean access, Pageable pageable);

    @Query("select distinct i from Item i where i.requestId in :currentIds")
    Collection<Item> getItemsByRequestIds(@Param("currentIds") Collection<Integer> requestId);

    @Query("select distinct i from Item i where i.requestId = ?1")
    Collection<Item> getItemsByRequestId(Integer requestId);
}
