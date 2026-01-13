package com.vansh.manger.Manger.Config;

import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;

@Configuration
public class ModalMapper {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }



}
