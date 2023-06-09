package com.example.InfBezTim10.service.base.implementation;

import com.example.InfBezTim10.model.BaseEntity;
import com.example.InfBezTim10.service.base.IJPAService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Transactional
public abstract class MongoService<T extends BaseEntity> extends CRUDService<T> implements IJPAService<T> {
    @Override
    public Iterable<T> findAll(Sort sorter) {
        return getEntityRepository().findAll(sorter);
    }

    @Override
    public Page<T> findAll(Pageable page) {
        return getEntityRepository().findAll(page);
    }
}
