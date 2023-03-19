package com.example.InfBezTim10.repository;

import com.example.InfBezTim10.model.Authority;
import com.example.InfBezTim10.model.AuthorityEnum;
import com.example.InfBezTim10.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IAuthorityRepository extends JpaRepository<Authority, Long> {


    public Authority findByAuthorityName(String authorityName);
}
