package com.example.InfBezTim10.service.userManagement;

import com.example.InfBezTim10.model.user.Authority;
import com.example.InfBezTim10.model.user.AuthorityEnum;
import com.example.InfBezTim10.service.base.IJPAService;

public interface IAuthorityService  extends IJPAService<Authority> {

    Authority getAuthority(AuthorityEnum authorityEnum);
}
