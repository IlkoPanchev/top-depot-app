package warehouse.config;

import com.google.gson.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import warehouse.items.validation.CategoryValidator;
import warehouse.items.validation.SupplierValidator;
import warehouse.utils.file.FileIOUtil;
import warehouse.utils.file.impl.FileIOUtilImpl;
import warehouse.utils.time.TimeBordersConvertor;
import warehouse.utils.time.impl.TimeBordersConvertorImpl;
import warehouse.utils.validation.ValidationUtil;
import warehouse.utils.validation.ValidationUtilImpl;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class ApplicationBeanConfiguration {

    @Bean
    public TimeBordersConvertor timeBordersConvertor(){

        return new TimeBordersConvertorImpl();
    }


    @Bean
    public Gson gson(){
        return new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                    @Override
                    public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                    }
                })
                .registerTypeAdapter(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                    @Override
                    public JsonElement serialize(LocalDateTime localDateTime, Type srcType, JsonSerializationContext context) {
                        return new JsonPrimitive(formatter.format(localDateTime));
                    }
                })
                .setPrettyPrinting().create();
    }

    @Bean
    public FileIOUtil fileIOUtil(){
        return new FileIOUtilImpl();
    }

    @Bean
    public ValidationUtil validationUtil() {
        return new ValidationUtilImpl();
    }

}
