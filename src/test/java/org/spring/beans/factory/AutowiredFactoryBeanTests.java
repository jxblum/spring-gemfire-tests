/*
 * Copyright 2014-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.spring.beans.factory;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

/**
 * Unit tests testing the effects of {@link Autowired @Autowiring} a {@link FactoryBean}.
 *
 * @author John Blum
 * @see <a href="https://jira.spring.io/browse/SPR-9255">SPR-9255</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@SuppressWarnings("unused")
public class AutowiredFactoryBeanTests {

  @Autowired
  private LocalDate localDate;

  @Autowired
  @Qualifier("localDatePrinter")
  private String localDatePrinter;

  private void assertLocalDate(LocalDate localDate, int year, Month month, int dayOfMonth) {
    assertThat(localDate).isNotNull();
    assertThat(localDate.getYear()).isEqualTo(year);
    assertThat(localDate.getMonth()).isEqualTo(month);
    assertThat(localDate.getDayOfMonth()).isEqualTo(dayOfMonth);
  }

  @Test
  public void localDateIs2020July1() {
    assertLocalDate(this.localDate, 2020, Month.JULY, 1);
  }

  @Test
  public void localDatePrinterIs2020July1() {
    assertThat(localDatePrinter).isEqualTo("This day on [2020-07-01]");
  }

  @Configuration
  static class TestConfiguration {

    @Bean
    LocalDatePrinterFactoryBean localDatePrinter() {
      return new LocalDatePrinterFactoryBean();
    }

    @Bean
    LocalDateFactoryBean localDate() {

      LocalDateFactoryBean localDateFactory = new LocalDateFactoryBean();

      localDateFactory.setYear(2017);
      localDateFactory.setMonth(Month.MAY);
      localDateFactory.setDayOfMonth(31);

      return localDateFactory;
    }

    @Bean
    Configurer yearConfigurer(@Value("${app.date.year:2020}") int year) {
      return (beanName, bean) -> Optional.ofNullable(beanName).filter("localDate"::equals)
        .ifPresent(it -> ((LocalDateFactoryBean) bean).setYear(year));
    }

    @Bean
    Configurer monthConfigurer(@Value("${app.date.month:JULY}") String month) {
      return (beanName, bean) -> Optional.ofNullable(beanName).filter("localDate"::equals)
        .ifPresent(it -> ((LocalDateFactoryBean) bean).setMonth(Month.valueOf(month)));
    }

    @Bean
    Configurer dayConfigurer(@Value("${app.date.day:1}") int day) {
      return (beanName, bean) -> Optional.ofNullable(beanName).filter("localDate"::equals)
        .ifPresent(it -> ((LocalDateFactoryBean) bean).setDayOfMonth(day));
    }

    @Bean
    Configurer dateFormatPattern(@Value("${app.date.format.pattern:This day on [%s]}") String formatPattern) {
      return (beanName, bean) -> Optional.ofNullable(beanName).filter("localDatePrinter"::equals)
        .ifPresent(it -> ((LocalDatePrinterFactoryBean) bean).setFormatPattern(formatPattern));
    }
  }


  interface Configurer {
    void configure(String beanName, Object bean);
  }

  static class LocalDateFactoryBean implements BeanNameAware, FactoryBean<LocalDate>, InitializingBean {

    @Autowired(required = false)
    private List<Configurer> configurers = Collections.emptyList();

    private Configurer compositeConfigurer = (beanName, bean) ->
      configurers.forEach(configurer -> configurer.configure(beanName, bean));

    private Integer year;
    private Integer dayOfMonth;

    private LocalDate localDate;

    private Month month;

    private String beanName;

    @Override
    public void afterPropertiesSet() throws Exception {
      this.compositeConfigurer.configure(this.beanName, this);
    }

    @Override
    public void setBeanName(String beanName) {
      this.beanName = beanName;
    }

    @Override
    public LocalDate getObject() throws Exception {

      return Optional.ofNullable(this.localDate).orElseGet(() -> {
        this.localDate = LocalDate.of(getYear(), getMonth(), getDayOfMonth());
        return this.localDate;
      });
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<?> getObjectType() {
      return Optional.ofNullable(this.localDate).map(Object::getClass).orElse((Class) LocalDate.class);
    }

    protected int getDayOfMonth() {
      return Optional.ofNullable(this.dayOfMonth).orElseGet(() -> LocalDate.now().getDayOfMonth());
    }

    public void setDayOfMonth(Integer dayOfMonth) {
      this.dayOfMonth = dayOfMonth;
    }

    protected Month getMonth() {
      return Optional.ofNullable(this.month).orElseGet(() -> LocalDate.now().getMonth());
    }

    public void setMonth(Month month) {
      this.month = month;
    }

    protected int getYear() {
      return Optional.ofNullable(this.year).orElseGet(() -> LocalDate.now().getYear());
    }

    public void setYear(Integer year) {
      this.year = year;
    }
  }

  static class LocalDatePrinterFactoryBean implements BeanFactoryAware, BeanNameAware, FactoryBean<String>, InitializingBean {

    private BeanFactory beanFactory;

    //@Autowired
    private LocalDate localDate;

    @Autowired(required = false)
    private List<Configurer> configurers = Collections.emptyList();

    private Configurer compositeConfigurer = (beanName, bean) ->
      configurers.forEach(configurer -> configurer.configure(beanName, bean));

    private String beanName;
    private String formatPattern = "Date is [%s]";

    @Override
    public void afterPropertiesSet() throws Exception {

      this.compositeConfigurer.configure(this.beanName, this);

      this.localDate = Optional.ofNullable(this.localDate)
        .orElseGet(() -> beanFactory.getBean("localDate", LocalDate.class));
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
      this.beanFactory = beanFactory;
    }

    @Override
    public void setBeanName(String beanName) {
      this.beanName = beanName;
    }

    @Override
    public String getObject() throws Exception {
      return String.format(this.formatPattern, this.localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

    @Override
    public Class<?> getObjectType() {
      return String.class;
    }

    public void setFormatPattern(String formatPattern) {
      this.formatPattern = Optional.ofNullable(formatPattern).filter(StringUtils::hasText).orElse("Date is [%s]");
    }
  }
}
