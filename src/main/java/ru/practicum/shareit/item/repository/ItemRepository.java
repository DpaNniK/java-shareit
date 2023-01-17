package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.Collection;

@Repository
public interface ItemRepository extends JpaRepository<Item, Integer> {

    Collection<Item> findItemsByOwnerId(Integer ownerId);

    @Query(" select i from Item i " +
            "where i.available = ?2 and upper(i.name) like upper(concat('%', ?1, '%')) " +
            "or i.available = ?2 and upper(i.description) like upper(concat('%', ?1, '%'))")
    Collection<Item> searchByText(String text, Boolean access);
}
