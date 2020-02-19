/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.awssdk.services.dynamodb.datamodeling;

import static software.amazon.awssdk.services.dynamodb.datamodeling.StandardTypeConverters.Scalar.TIME_ZONE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.TimeZone;

/**
 * Annotation to format a timestamp object using Java's standard date and time
 * patterns.
 *
 * <pre class="brush: java">
 * &#064;DynamoDBTypeConvertedTimestamp(pattern=&quot;yyyyMMddHHmmssSSS&quot;, timeZone=&quot;UTC&quot;)
 * public Date getCreatedDate()
 * </pre>
 *
 * <p>Supports the standard {@link Date} type-conversions; such as
 * {@link java.util.Calendar}, {@link Long}.</p>
 *
 * <p>Primitives such as {@code long} are not supported since the unset
 * (or null) state can't be detected.</p>
 *
 * <p>Compatible with {@link DynamoDbAutoGeneratedTimestamp}</p>
 *
 * @see DynamoDbAutoGeneratedTimestamp
 * @see DynamoDbTypeConverted
 * @see java.text.SimpleDateFormat
 * @see java.util.TimeZone
 */
@DynamoDbTypeConverted(converter = DynamoDbTypeConvertedTimestamp.Converter.class)
@DynamoDbTyped(DynamoDbMapperFieldModel.DynamoDbAttributeType.S)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD})
public @interface DynamoDbTypeConvertedTimestamp {

    /**
     * The pattern format; default is ISO8601.
     * @see java.text.SimpleDateFormat
     */
    String pattern() default "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * The time zone; default is {@code UTC}.
     * @see java.util.TimeZone
     */
    String timeZone() default "UTC";

    /**
     * Timestamp format converter.
     */
    final class Converter<T> implements DynamoDbTypeConverter<String, T> {
        private final DynamoDbTypeConverter<ZonedDateTime, T> converter;
        private final DateTimeFormatter formatter;

        Converter(Class<T> targetType, DynamoDbTypeConvertedTimestamp annotation) {
            this.formatter = new DateTimeFormatterBuilder()
                                 .appendPattern(annotation.pattern()).toFormatter()
                                 .withZone(TIME_ZONE.<TimeZone>convert(annotation.timeZone()).toZoneId());
            this.converter = StandardTypeConverters.factory().getConverter(ZonedDateTime.class, targetType);
        }

        @Override
        public String convert(final T object) {
            return formatter.format(converter.convert(object));
        }

        @Override
        public T unconvert(final String object) {
            return converter.unconvert(ZonedDateTime.parse(object, formatter));
        }
    }

}
