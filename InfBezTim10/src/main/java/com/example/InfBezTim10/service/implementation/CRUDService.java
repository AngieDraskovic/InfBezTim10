package com.example.InfBezTim10.service.implementation;

import com.example.InfBezTim10.exception.NotFoundException;
import com.example.InfBezTim10.model.BaseEntity;
import com.example.InfBezTim10.service.ICRUDService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Transactional
public abstract class CRUDService<T extends BaseEntity> implements ICRUDService<T> {

    protected abstract JpaRepository<T, Long> getEntityRepository();

    @Override
    public List<T> findAll() {
        return getEntityRepository().findAll();
    }

    @Override
    public T findById(Long id) throws EntityNotFoundException {
        return findEntityChecked(id);
    }

    @Override
    public T save(T entity) {
        return getEntityRepository().save(entity);
    }

    @Override
    public T update(T entity) {
        return save(entity);
    }

    @Override
    public void delete(Long id) {
        var entity = findEntityChecked(id);
        entity.setActive(false);
    }

    private T findEntityChecked(Long id) throws EntityNotFoundException {
        var entity = getEntityRepository().findById(id).orElseThrow(() -> new NotFoundException("Cannot find entity with id: " + id));
        if (Boolean.TRUE.equals(entity.getActive())) {
            return entity;
        }
        throw new NotFoundException("Cannot find entity with id: " + id);
    }
}
