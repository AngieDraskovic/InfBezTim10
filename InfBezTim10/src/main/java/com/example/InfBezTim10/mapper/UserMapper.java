package com.example.InfBezTim10.mapper;

import com.example.InfBezTim10.dto.UserDetailsDTO;
import com.example.InfBezTim10.dto.UserRegistrationDTO;
import com.example.InfBezTim10.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDetailsDTO userToUserDetailsDTO(User user);
    
    User userRegistrationDTOtoUser(UserRegistrationDTO userRegistrationDTO);
}
