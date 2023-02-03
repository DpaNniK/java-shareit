package ru.practicum.shareit.item;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.Map;

@Service
public class ItemClient extends BaseClient {
    private static final String API_PREFIX = "/items";

    @Autowired
    public ItemClient(@Value("${shareit-server.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> createItem(Integer userId, ItemDto itemDto) {
        return post("", userId, itemDto);
    }

    public ResponseEntity<Object> createComment(Integer userId, Integer itemId, CommentDto comment) {
        return post("/" + itemId + "/comment", userId, comment);
    }

    public ResponseEntity<Object> updateItem(Integer userId, ItemDto itemDto, Integer itemId) {
        return patch("/" + itemId, userId, itemDto);
    }

    public ResponseEntity<Object> getItemById(Integer userId, Integer itemId) {
        return get("/" + itemId, userId);
    }

    public ResponseEntity<Object> getAllItemsOwner(Integer from, Integer size, Integer userId) {
        if (from == null) {
            return get("", userId);
        } else {
            Map<String, Object> parameters = Map.of(
                    "from", from,
                    "size", size
            );
            return get("?from={from}&size={size}", userId, parameters);
        }
    }

    public ResponseEntity<Object> searchItemByText(Integer from, Integer size, Integer userId, String text) {
        Map<String, Object> parameters;
        if (from == null) {
            parameters = Map.of(
                    "text", text
            );
            return get("/search?text={text}", userId, parameters);
        } else {
            parameters = Map.of(
                    "from", from,
                    "size", size,
                    "text", text
            );
        }
        return get("/search?text={text}&from={from}&size={size}", userId, parameters);
    }
}
