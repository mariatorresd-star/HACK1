package com.example.hack1base;


import com.example.hack1base.User.domain.User;
import com.example.hack1base.User.domain.UserResponse;
import com.example.hack1base.Sale.domain.Sale;
import com.example.hack1base.Sale.web.SaleResponse;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();

        Converter<Enum<?>, String> enumToStringConverter = ctx ->
                ctx.getSource() != null ? ctx.getSource().name() : null;

        mapper.addMappings(new PropertyMap<User, UserResponse>() {
            @Override
            protected void configure() {
                using(enumToStringConverter).map(source.getRole(), destination.getRole());
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