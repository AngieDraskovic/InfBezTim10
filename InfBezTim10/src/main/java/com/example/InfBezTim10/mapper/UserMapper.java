package com.example.InfBezTim10.mapper;

import com.example.InfBezTim10.dto.user.UserDetailsDTO;
import com.example.InfBezTim10.dto.user.UserRegistrationDTO;
import com.example.InfBezTim10.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserDetailsDTO userToUserDetailsDTO(User user);
    
    User userRegistrationDTOtoUser(UserRegistrationDTO userRegistrationDTO);
}
