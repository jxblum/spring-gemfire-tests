package org.spring.data.gemfire.mapping;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Map;

import com.gemstone.gemfire.pdx.PdxReader;
import com.gemstone.gemfire.pdx.PdxSerializer;
import com.gemstone.gemfire.pdx.PdxWriter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.spring.data.gemfire.mapping.MappingPdxSerializerTest.MappingPdxSerializerTestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.mapping.MappingPdxSerializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * The MappingPdxSerializerTest class is a test suite of test cases testing the addition of "custom" PdxSerializers
 * to the Spring Data GemFire MappingPdxSerializer class for handling (de)serialization of Enumerated values.
 *
 * @author John Blum
 * @see org.springframework.data.gemfire.mapping.MappingPdxSerializer
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see com.gemstone.gemfire.pdx.PdxSerializer
 * @since 1.7.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { MappingPdxSerializerTestConfiguration.class })
@SuppressWarnings("unused")
public class MappingPdxSerializerTest {

  @Autowired
  private CustomMappingPdxSerializer pdxSerializer;

  @Test
  public void getCustomSerializerForEnumTypes() {
    PdxSerializer enumCustomSerializer = pdxSerializer.getCustomSerializer(Gender.class);

    assertTrue(enumCustomSerializer instanceof EnumPdxSerializer);
    assertNull(pdxSerializer.getCustomSerializer(Person.class));

    PdxSerializer anotherEnumCustomSerializer = pdxSerializer.getCustomSerializer(Degree.class);

    assertTrue(anotherEnumCustomSerializer instanceof EnumPdxSerializer);
    assertSame(enumCustomSerializer, anotherEnumCustomSerializer);
  }

  public static class Person {

    private Degree degree;

    private Gender gender;

    private String firstName;
    private String lastName;
  }

  public static enum Gender {
    FEMALE,
    MALE
  }

  public static enum Degree {
    ASSOCIATES,
    BA,
    BS,
    MA,
    MS,
    PDH
  }

  protected static final class CustomMappingPdxSerializer extends MappingPdxSerializer {

    @Override
    protected PdxSerializer getCustomSerializer(final Class<?> type) {
      for (PdxSerializer pdxSerializer : getCustomSerializers().values()) {
        if (pdxSerializer instanceof EnumPdxSerializer && ((EnumPdxSerializer) pdxSerializer).canSerialize(type)) {
          return pdxSerializer;
        }
      }

      return null;
    }

    protected Map<Class<?>, PdxSerializer> getCustomSerializers() {
      return null;
    }
  }

  protected static final class EnumPdxSerializer extends BasePdxSerializerAdapter {

    @Override
    public boolean canSerialize(final Class<?> type) {
      return (type != null && Enum.class.isAssignableFrom(type));
    }
  }

  protected static abstract class BasePdxSerializerAdapter implements PdxSerializer {

    public boolean canSerializer(Object obj) {
      return (obj != null && canSerialize(obj.getClass()));
    }

    public abstract boolean canSerialize(Class<?> type);

    @Override public boolean toData(final Object obj, final PdxWriter pdxWriter) {
      throw new UnsupportedOperationException("Not Implemented!");
    }

    @Override public Object fromData(final Class<?> aClass, final PdxReader pdxReader) {
      throw new UnsupportedOperationException("Not Implemented!");
    }
  }

  @Configuration
  public static class MappingPdxSerializerTestConfiguration {

    @Bean
    public MappingPdxSerializer pdxSerializer() {
      MappingPdxSerializer pdxSerializer = new CustomMappingPdxSerializer();
      pdxSerializer.setCustomSerializers(Collections.<Class<?>, PdxSerializer>singletonMap(
        Enum.class, new EnumPdxSerializer()));
      return pdxSerializer;
    }
  }

}
