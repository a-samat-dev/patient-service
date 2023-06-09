package kz.smarthealth.patientservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(Arrays.asList(
                new OffsetDateTimeReadConverter(),
                new OffsetDateTimeWriteConverter(),
                new LocalDateWriteConverter(),
                new LocalDateReadConverter()
        ));
    }

    static class OffsetDateTimeWriteConverter implements Converter<OffsetDateTime, Date> {

        @Override
        public Date convert(OffsetDateTime source) {
            return Date.from(source.toInstant().atZone(ZoneOffset.UTC).toInstant());
        }
    }

    static class OffsetDateTimeReadConverter implements Converter<Date, OffsetDateTime> {

        @Override
        public OffsetDateTime convert(Date source) {
            return source.toInstant().atOffset(ZoneOffset.UTC);
        }
    }

    static class LocalDateWriteConverter implements Converter<LocalDate, Date> {

        @Override
        public Date convert(LocalDate source) {
            return Date.from(source.atStartOfDay().toInstant(ZoneOffset.UTC));
        }
    }

    static class LocalDateReadConverter implements Converter<Date, LocalDate> {

        @Override
        public LocalDate convert(Date source) {
            return source.toInstant().atOffset(ZoneOffset.UTC).toLocalDate();
        }
    }
}
