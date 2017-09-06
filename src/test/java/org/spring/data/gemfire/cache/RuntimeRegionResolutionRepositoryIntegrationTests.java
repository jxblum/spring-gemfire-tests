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

package org.spring.data.gemfire.cache;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Supplier;
import javax.annotation.Resource;

import org.apache.geode.cache.CacheFactory;
import org.apache.geode.cache.GemFireCache;
import org.apache.geode.cache.Region;
import org.apache.geode.cache.RegionService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.GemfireTemplate;
import org.springframework.data.gemfire.PartitionedRegionFactoryBean;
import org.springframework.data.gemfire.config.annotation.PeerCacheApplication;
import org.springframework.data.gemfire.mapping.GemfireMappingContext;
import org.springframework.data.gemfire.mapping.GemfirePersistentEntity;
import org.springframework.data.gemfire.mapping.GemfirePersistentProperty;
import org.springframework.data.gemfire.repository.GemfireRepository;
import org.springframework.data.gemfire.repository.query.GemfireEntityInformation;
import org.springframework.data.gemfire.repository.query.GemfireQueryMethod;
import org.springframework.data.gemfire.repository.query.PartTreeGemfireRepositoryQuery;
import org.springframework.data.gemfire.repository.query.StringBasedGemfireRepositoryQuery;
import org.springframework.data.gemfire.repository.support.GemfireRepositoryFactory;
import org.springframework.data.gemfire.repository.support.GemfireRepositoryFactoryBean;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StringUtils;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * The RuntimeRegionResolutionRepositoryIntegrationTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class RuntimeRegionResolutionRepositoryIntegrationTests {

  private Customer pieDoe = Customer.newCustomer(1L, "Pie", "Doe");
  private Customer sourDoe = Customer.newCustomer(1L, "Sour", "Doe");

  @Autowired
  private CustomerRepository customerRepository;

  @Resource(name = "CustomersOne")
  private Region<Long, Customer> customersOne;

  @Resource(name = "CustomersTwo")
  private Region<Long, Customer> customersTwo;

  @Autowired
  private NamedRegionResolver<Long, Customer> customersRegionResolver;

  @Before
  public void setup() {
    save(this.customersOne, this.pieDoe);
    save(this.customersTwo, this.sourDoe);
  }

  private Customer save(Region<Long, Customer> customers, Customer customer) {
    customers.put(customer.getId(), customer);
    return customer;
  }

  @Test
  public void regionResolutionWorksForFindById() {

    this.customersRegionResolver.setRegionName("CustomersOne");

    assertThat(this.customerRepository.findById(1L).orElse(null)).isEqualTo(pieDoe);

    this.customersRegionResolver.setRegionName("CustomersTwo");

    assertThat(this.customerRepository.findById(1L).orElse(null)).isEqualTo(sourDoe);
  }

  @Test
  public void regionResolutionFailsForApplicationSpecificQueryMethods() {

    this.customersRegionResolver.setRegionName("CustomersOne");

    assertThat(this.customerRepository.findByLastName("Doe")).isEqualTo(pieDoe);

    this.customersRegionResolver.setRegionName("CustomersTwo");

    assertThat(this.customerRepository.findByLastName("Doe")).isEqualTo(sourDoe);
  }

  @Configuration
  @PeerCacheApplication
  static class TestConfiguration {

    PartitionedRegionFactoryBean<Long, Customer> newPartitionRegion(GemFireCache gemfireCache) {

      PartitionedRegionFactoryBean<Long, Customer> partitionRegion = new PartitionedRegionFactoryBean<>();

      partitionRegion.setCache(gemfireCache);
      partitionRegion.setClose(false);
      partitionRegion.setPersistent(false);

      return partitionRegion;
    }

    @Bean(name = "Customers")
    PartitionedRegionFactoryBean<Long, Customer> customersRegion(GemFireCache gemfireCache) {
      return newPartitionRegion(gemfireCache);
    }

    @Bean(name = "CustomersOne")
    PartitionedRegionFactoryBean<Long, Customer> anotherCustomersRegion(GemFireCache gemfireCache) {
      return newPartitionRegion(gemfireCache);
    }

    @Bean(name = "CustomersTwo")
    PartitionedRegionFactoryBean<Long, Customer> yetAnotherCustomersRegion(GemFireCache gemfireCache) {
      return newPartitionRegion(gemfireCache);
    }

    @Bean
    RegionResolver<Long, Customer> customersRegionResolver(GemFireCache gemfireCache) {
      return new NamedRegionResolver<>(gemfireCache);
    }

    @Bean
    GemfireRepositoryFactoryBean customersRepository() {

      RegionResolvingGemfireRepositoryFactoryBean<CustomerRepository, Customer, Long> gemfireRepositoryFactoryBean =
        new RegionResolvingGemfireRepositoryFactoryBean<>(CustomerRepository.class);

      gemfireRepositoryFactoryBean.setGemfireMappingContext(new GemfireMappingContext());

      return gemfireRepositoryFactoryBean;
    }
  }

  static class RegionResolvingGemfireRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
      extends GemfireRepositoryFactoryBean<T, S, ID> {

    public RegionResolvingGemfireRepositoryFactoryBean(Class<T> repositoryInterface) {
      super(repositoryInterface);
    }

    @Override
    protected RepositoryFactorySupport createRepositoryFactory() {
      return new RegionResolvingGemfireRepositoryFactory(getRegions(), getGemfireMappingContext());
    }
  }

  static class RegionResolvingGemfireRepositoryFactory extends GemfireRepositoryFactory {

    private BeanFactory beanFactory;

    private final MappingContext<? extends GemfirePersistentEntity<?>, GemfirePersistentProperty> mappingContext;

    RegionResolvingGemfireRepositoryFactory(Iterable<Region<?, ?>> regions,
        MappingContext<? extends GemfirePersistentEntity<?>, GemfirePersistentProperty> mappingContext) {

      super(regions, mappingContext);
      this.mappingContext = mappingContext;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
      super.setBeanFactory(beanFactory);
      this.beanFactory = beanFactory;
    }

    @Override
    protected Object getTargetRepository(RepositoryInformation repositoryInformation) {

      GemfireEntityInformation<?, Serializable> entityInformation =
        getEntityInformation(repositoryInformation.getDomainType());

      GemfireTemplate gemfireTemplate = newGemfireTemplate(entityInformation);

      return getTargetRepositoryViaReflection(repositoryInformation, gemfireTemplate, entityInformation);
    }

    private GemfireTemplate newGemfireTemplate(RepositoryMetadata repositoryMetadata) {
      return newGemfireTemplate(getEntityInformation(repositoryMetadata.getDomainType()));
    }

    private GemfireTemplate newGemfireTemplate(GemfireEntityInformation<?, Serializable> entityInformation) {
      return newGemfireTemplate(() ->
        String.format("%1$s%2$s", entityInformation.getRegionName().toLowerCase(), "RegionResolver"));
    }

    private GemfireTemplate newGemfireTemplate(Supplier<String> regionResolverBeanNameSupplier) {

      RegionResolver regionResolver =
        this.beanFactory.getBean(regionResolverBeanNameSupplier.get(), RegionResolver.class);

      return new RegionResolvingGemfireTemplate(regionResolver);
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(Key key,
        EvaluationContextProvider evaluationContextProvider) {

      return Optional.of(
        (Method method, RepositoryMetadata metadata, ProjectionFactory factory, NamedQueries namedQueries) -> {

          GemfireQueryMethod queryMethod = new GemfireQueryMethod(method, metadata, factory, mappingContext);

          GemfireTemplate template = newGemfireTemplate(metadata);

          if (queryMethod.hasAnnotatedQuery()) {
            return new StringBasedGemfireRepositoryQuery(queryMethod, template).asUserDefinedQuery();
          }

          if (namedQueries.hasQuery(queryMethod.getNamedQueryName())) {
            return new StringBasedGemfireRepositoryQuery(namedQueries.getQuery(queryMethod.getNamedQueryName()),
              queryMethod, template).asUserDefinedQuery();
          }

          return new PartTreeGemfireRepositoryQuery(queryMethod, template);
        });
    }
  }

  static class RegionResolvingGemfireTemplate extends GemfireTemplate {

    private RegionResolver regionResolver;

    RegionResolvingGemfireTemplate(RegionResolver regionResolver) {
      this.regionResolver = Optional.ofNullable(regionResolver)
        .orElseThrow(() -> new IllegalArgumentException("RegionResolver is required"));
    }

    @SuppressWarnings("unchecked")
    protected <K, V> RegionResolver<K, V> getRegionResolver() {
      return this.regionResolver;
    }

    @Override
    public <K, V> Region<K, V> getRegion() {
      return this.<K, V>getRegionResolver().resolve();
    }
  }

  interface RegionResolver<K, V> {
    Region<K, V> resolve();
  }

  static class NamedRegionResolver<K, V> implements RegionResolver<K, V> {

    private final RegionService regionService;

    private String regionName;

    public NamedRegionResolver(RegionService regionService) {
      this.regionService = Optional.ofNullable(regionService)
        .orElseThrow(() -> new IllegalArgumentException("RegionService must not be null"));
    }

    @Override
    public Region<K, V> resolve() {

      return Optional.<Region<K, V>>ofNullable(resolveRegionService().getRegion(getRegionName()))
        .orElseThrow(() -> new IllegalStateException(String.format("Failed to find a Region with name [%s]",
          getRegionName())));
    }

    private RegionService resolveRegionService() {
      return Optional.ofNullable(this.regionService).orElseGet(CacheFactory::getAnyInstance);
    }

    protected String getRegionName() {

      return Optional.ofNullable(this.regionName)
        .filter(StringUtils::hasText)
        .orElseThrow(() -> new IllegalStateException("Region name must be specified"));
    }

    public void setRegionName(String regionName) {
      this.regionName = regionName;
    }
  }

  @Data
  @RequiredArgsConstructor(staticName = "newCustomer")
  @org.springframework.data.gemfire.mapping.annotation.Region("Customers")
  static class Customer implements Serializable {

    @Id @NonNull
    Long id;

    @NonNull String firstName;
    @NonNull String lastName;

    @Override
    public String toString() {
      return String.format("%1$s %2$s - %3$d", getFirstName(), getLastName(), getId());
    }
  }

  interface CustomerRepository extends GemfireRepository<Customer, Long> {
    Customer findByLastName(String lastName);
  }
}
