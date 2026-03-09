package com.feirinha.api.service;

import com.feirinha.api.exception.BadRequestException;
import com.feirinha.api.exception.ConflictException;
import com.feirinha.api.exception.NotFoundException;
import com.feirinha.api.model.Item;
import com.feirinha.api.repository.ItemRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class ItemService {

    private final ItemRepository repository;

    public ItemService(ItemRepository repository) {
        this.repository = repository;
    }

    public Item createItem(Item item) {
        validateItem(item);

        repository.findByName(item.getName()).ifPresent(i -> {
            throw new ConflictException("Item com esse nome já existe");
        });

        return repository.save(item);
    }

    public List<Item> getAllItems() {
        return repository.findAll();
    }

    public Item getItemById(String idStr) {
        Long id = parseId(idStr);
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item não encontrado"));
    }

    public Item updateItem(String idStr, Item item) {
        Long id = parseId(idStr);
        validateItem(item);

        Item existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item não encontrado"));

        repository.findByName(item.getName())
                .filter(i -> !i.getId().equals(id))
                .ifPresent(i -> { throw new ConflictException("Item com esse nome já existe"); });

        existing.setName(item.getName());
        existing.setQuantity(item.getQuantity());

        return repository.save(existing);
    }

    public void deleteItem(String idStr) {
        Long id = parseId(idStr);
        Item existing = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Item não encontrado"));
        repository.delete(existing);
    }

    private void validateItem(Item item) {
        if (!StringUtils.hasText(item.getName())) {
            throw new BadRequestException("Nome do item é obrigatório");
        }
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            throw new BadRequestException("Quantidade deve ser maior que 0");
        }
    }

    private Long parseId(String idStr) {
        try {
            return Long.parseLong(idStr);
        } catch (NumberFormatException e) {
            throw new BadRequestException("ID inválido");
        }
    }
}