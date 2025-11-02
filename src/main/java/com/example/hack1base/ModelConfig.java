package com.example.hack1base;

import com.example.hack1base.User.domain.User;
import com.example.hack1base.User.domain.UserResponse;
import com.example.hack1base.Sale.domain.Sale;
import com.example.hack1base.Sale.web.SaleResponse;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        mapper.addMappings(new PropertyMap<User, UserResponse>() {
            @Override
            protected void configure() {
                map().setRole(source.getRole().name());
            }
        });

        mapper.addMappings(new PropertyMap<Sale, SaleResponse>() {
            @Override
            protected void configure() {
                using(ctx -> {
                    User createdBy = (User) ctx.getSource();
                    return createdBy != null ? createdBy.getUsername() : null;
                }).map(source.getCreatedBy(), destination.getCreatedBy());
            }
        });

        return mapper;
    }
}