package warehouse.config;

import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.spi.MappingContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import warehouse.departments.model.DepartmentName;
import warehouse.items.model.ItemEntity;
import warehouse.items.model.ItemViewServiceModel;
import warehouse.users.model.*;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();

         Converter<DepartmentName, String> enumConverter = new Converter<DepartmentName, String>()
        {
            @Override
            public String convert(MappingContext<DepartmentName, String> mappingContext) {
                if(mappingContext.getSource() != null) {
                    return String.valueOf(mappingContext.getSource().toString());
                }
                return null;
            }
        };


        PropertyMap<ItemEntity, ItemViewServiceModel> itemMap = new PropertyMap<ItemEntity, ItemViewServiceModel>() {
            protected void configure() {
                map().setCategory(source.getCategory().getName());
                map().setSupplier(source.getSupplier().getName());
            }
        };
        modelMapper.addMappings(itemMap);

        PropertyMap<UserServiceModel, UserViewBindingModel> userViewMap = new PropertyMap<UserServiceModel, UserViewBindingModel>() {
            protected void configure() {
                map().setPassword(null);
            }
        };
        modelMapper.addMappings(userViewMap);


        PropertyMap<UserServiceModel, UserRegisterBindingModel> userEditProfileMap = new PropertyMap<UserServiceModel, UserRegisterBindingModel>() {
            protected void configure() {
                map().setPassword(null);
                map().setRole("ROLE_USER");

            }
        };
        modelMapper.addMappings(userEditProfileMap);


        return modelMapper;


    }
}
