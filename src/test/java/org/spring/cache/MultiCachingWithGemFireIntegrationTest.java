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

package org.spring.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.annotation.Id;
import org.springframework.data.gemfire.mapping.annotation.Region;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

/**
 * The MultiCachingWithGemFireIntegrationTest class is a test suite of test cases testing the contract and functionality
 * of Spring's Cache Abstraction using GemFire as a caching provider and caching application domain entities under
 * different keys in the same GemFire Cache Region.
 *
 * @author John Blum
 * @see org.junit.Test
 * @see org.junit.runner.RunWith
 * @see org.springframework.cache.annotation.Cacheable
 * @see org.springframework.test.context.ContextConfiguration
 * @see org.springframework.test.context.junit4.SpringJUnit4ClassRunner
 * @see org.apache.geode.cache.Region
 * @since 1.0.0
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@SuppressWarnings("unused")
public class MultiCachingWithGemFireIntegrationTest {

  @Autowired
  private BookService bookService;

  @Resource(name = "Books")
  @SuppressWarnings("all")
  private org.apache.geode.cache.Region<Object, Book> books;

  protected Object getKey(Book book) {
    assertFalse(books.containsKey(book.getIsbn()));

    return (books.containsKey(book.getIsbn().getTenDigitNumber()) ? book.getIsbn().getTenDigitNumber()
      : (books.containsKey(book.getIsbn().getThirteenDigitNumber()) ? book.getIsbn().getThirteenDigitNumber()
      : (books.containsKey(bookService.toUpc(book.getIsbn())) ? bookService.toUpc(book.getIsbn())
      : null)));
  }

  protected void assertBooks(Book... books) {
    assertEquals(books.length, this.books.size());

    Book previousBook = null;

    for (Book currentBook : books) {
      assertNotSame(previousBook, currentBook);
      assertTrue(previousBook == null || currentBook.equals(previousBook));
      assertEquals(currentBook, this.books.get(getKey(currentBook)));
      previousBook = currentBook;
    }
  }

  @Test
  public void cacheHitsAndMisses() {
    assertBooks();
    assertFalse(bookService.wasCacheMiss());

    Book tenDigitIsbnBasedBook = bookService.findByTenDigit(bookService.generateIsbn());

    assertNotNull(tenDigitIsbnBasedBook);
    assertTrue(bookService.wasCacheMiss());
    assertBooks(tenDigitIsbnBasedBook);

    Book thirteenDigitIsbnBasedBook = bookService.findByThirteenDigit(tenDigitIsbnBasedBook.getIsbn());

    assertNotNull(thirteenDigitIsbnBasedBook);
    assertTrue(bookService.wasCacheMiss());
    assertBooks(tenDigitIsbnBasedBook, thirteenDigitIsbnBasedBook);

    Book upcBasedBook = bookService.findByUpcFor(tenDigitIsbnBasedBook.getIsbn());

    assertNotNull(upcBasedBook);
    assertTrue(bookService.wasCacheMiss());
    assertBooks(tenDigitIsbnBasedBook, thirteenDigitIsbnBasedBook, upcBasedBook);

    // pre-assertions on 'Books' Region...
    assertFalse(books.isEmpty());
    assertEquals(3, books.size());

    Book cachedTenDigitIsbnBasedBook = bookService.findByTenDigit(tenDigitIsbnBasedBook.getIsbn());

    assertEquals(tenDigitIsbnBasedBook, cachedTenDigitIsbnBasedBook);
    assertFalse(bookService.wasCacheMiss());

    Book cachedThirteenDigitIsbnBasedBook = bookService.findByThirteenDigit(thirteenDigitIsbnBasedBook.getIsbn());

    assertEquals(thirteenDigitIsbnBasedBook, cachedThirteenDigitIsbnBasedBook);
    assertFalse(bookService.wasCacheMiss());

    Book cachedUpcBasedBook = bookService.findByUpcFor(upcBasedBook.getIsbn());

    assertEquals(upcBasedBook, cachedUpcBasedBook);
    assertFalse(bookService.wasCacheMiss());

    // post-assertions on 'Books' Region...
    assertFalse(books.isEmpty());
    assertEquals(3, books.size());
  }

  @Service("bookService")
  public static class BookService {

    private BookRepository bookRepository;

    private IsbnToUpcConverter converter;

    @Autowired
    public final void setBookRepository(BookRepository bookRepository) {
      Assert.notNull(bookRepository, "The 'BookRepository' must not be null!");
      this.bookRepository = bookRepository;
    }

    protected BookRepository getBookRepository() {
      Assert.state(bookRepository != null,
        "The reference to the 'BookRepository' was not properly configured!");
      return bookRepository;
    }

    @Autowired
    public final void setConverter(IsbnToUpcConverter converter) {
      Assert.notNull(converter, "The ISBN to UPC Converter must not be null!");
      this.converter = converter;
    }

    protected IsbnToUpcConverter getConverter() {
      Assert.state(converter != null, "The reference to the ISBN to UPC Converter was not properly configured!");
      return converter;
    }

    @Cacheable(value = "Books", key = "#isbn.tenDigitNumber")
    public Book findByTenDigit(ISBN isbn) {
      return getBookRepository().load(isbn);
    }

    @Cacheable(value = "Books", key = "#isbn.thirteenDigitNumber")
    public Book findByThirteenDigit(ISBN isbn) {
      return getBookRepository().load(isbn);
    }

    //@Cacheable(value = "Books", key = "@isbnToUpcConverter.convert(#isbn)")
    @Cacheable(value = "Books",
      key = "T(org.spring.cache.MultiCachingWithGemFireIntegrationTest$IsbnToUpcConverter).getInstance().convert(#isbn)")
    public Book findByUpcFor(ISBN isbn) {
      return getBookRepository().load(isbn);
    }

    public ISBN generateIsbn() {
      return getBookRepository().generateIsbn();
    }

    public UPC toUpc(ISBN isbn) {
      return getConverter().convert(isbn);
    }

    public boolean wasCacheMiss() {
      return getBookRepository().wasCacheMiss();
    }
  }

  @Repository("bookRepository")
  public static class BookRepository {

    private volatile boolean cacheMiss = false;

    private static final AtomicInteger BOOK_TITLE_SEQUENCE = new AtomicInteger(0);

    private static final Random ISBN_NUMBER_GENERATOR = new Random(System.currentTimeMillis());

    private static final Set<ISBN> ISBN_CACHE = new ConcurrentSkipListSet<>();

    private static final String BOOK_TITLE_FORMAT = "Title %1$d";

    protected Book newBook(ISBN isbn) {
      return newBook(isbn, generateTitle());
    }

    protected Book newBook(String title) {
      return newBook(generateIsbn(), title);
    }

    protected Book newBook(ISBN isbn, String title) {
      return new Book(isbn, title);
    }

    protected ISBN generateIsbn() {
      ISBN isbn;

      do {
        String isbnNumber = "";

        for (int count = 13; count > 0; count--) {
          isbnNumber += ISBN_NUMBER_GENERATOR.nextInt(10);
        }

        isbn = ISBN.valueOf(isbnNumber);
      }
      while (!ISBN_CACHE.add(isbn));

      return isbn;
    }

    protected String generateTitle() {
      return String.format(BOOK_TITLE_FORMAT, BOOK_TITLE_SEQUENCE.incrementAndGet());
    }

    public boolean wasCacheMiss() {
      boolean localCacheMiss = cacheMiss;
      cacheMiss = false;
      return localCacheMiss;
    }

    public Book load(final ISBN isbn) {
      cacheMiss = true;
      return newBook(isbn);
    }
  }

  @Region("Books")
  public static class Book implements Serializable {

    @Id
    private final ISBN isbn;

    private final String title;

    public Book(ISBN isbn, String title) {
      Assert.notNull(isbn, "The Book ISBN must not be null!");
      Assert.hasText(title, "The Book Title must be specified!");
      this.isbn = isbn;
      this.title = title;
    }

    public ISBN getIsbn() {
      return isbn;
    }

    public String getTitle() {
      return title;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }

      if (!(obj instanceof Book)) {
        return false;
      }

      Book that = (Book) obj;

      return this.getIsbn().equals(that.getIsbn());
    }

    @Override
    public int hashCode() {
      int hashValue = 17;
      hashValue = 37 * hashValue + getIsbn().hashCode();
      return hashValue;
    }

    @Override
    public String toString() {
      return String.format("{ @type = %1$s, @identityHashCode = %2$d, isbn = %3$s, title = %4$s }",
        getClass().getName(), System.identityHashCode(this), getIsbn(), getTitle());
    }
  }

  public static class ISBN implements Serializable {

    protected static final String TEN_DIGIT_FORMAT = "%4$s-%5$s%6$s%7$s-%8$s%9$s%10$s%11$s%12$s-%13$s";
    protected static final String THIRTEEN_DIGIT_FORMAT = "%1$s%2$s%3$s-" + TEN_DIGIT_FORMAT;

    private final String isbn;

    public ISBN(String isbn) {
      String isbnNumber = getDigitsOnly(isbn);
      Assert.isTrue(isbnNumber.length() == 13, "The ISBN number must be 13 digits");
      this.isbn = isbnNumber;
    }

    protected static String getDigitsOnly(String isbn) {
      StringBuilder buffer = new StringBuilder();

      if (isbn != null) {
        for (char digit : isbn.toCharArray()) {
          if (Character.isDigit(digit)) {
            buffer.append(digit);
          }
        }
      }

      return buffer.toString();
    }

    public static String[] toStringArray(String value) {
      List<String> stringArray = new ArrayList<>(value.length());

      for (char chr : value.toCharArray()) {
        stringArray.add(String.valueOf(chr));
      }

      return stringArray.toArray(new String[stringArray.size()]);
    }

    public static ISBN valueOf(String isbn) {
      return new ISBN(isbn);
    }

    public String getRawNumber() {
      return isbn;
    }

    public String getTenDigitNumber() {
      return String.format(TEN_DIGIT_FORMAT, toStringArray(getRawNumber()));
    }

    public String getThirteenDigitNumber() {
      return String.format(THIRTEEN_DIGIT_FORMAT, toStringArray(getRawNumber()));
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }

      if (!(obj instanceof ISBN)) {
        return false;
      }

      ISBN that = (ISBN) obj;

      return this.getRawNumber().equals(that.getRawNumber());
    }

    @Override
    public int hashCode() {
      int hashValue = 17;
      hashValue = 37 * hashValue + getRawNumber().hashCode();
      return hashValue;
    }

    @Override
    public String toString() {
      return getThirteenDigitNumber();
    }
  }

  public static class UPC implements Serializable {

    private final String number;

    public UPC(String number) {
      Assert.hasText(number, "The UPC number must be specified!");
      this.number = number;
    }

    public String getNumber() {
      return number;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }

      if (!(obj instanceof UPC)) {
        return false;
      }

      UPC that = (UPC) obj;

      return this.getNumber().equals(that.getNumber());
    }

    @Override
    public int hashCode() {
      int hashValue = 17;
      hashValue = 37 * hashValue + getNumber().hashCode();
      return hashValue;
    }

    @Override
    public String toString() {
      return getNumber();
    }
  }

  @Component("isbnToUpcConverter")
  public static class IsbnToUpcConverter implements Converter<ISBN, UPC> {

    public static final AtomicReference<IsbnToUpcConverter> INSTANCE = new AtomicReference<>(null);

    public IsbnToUpcConverter() {
      INSTANCE.compareAndSet(null, this);
    }

    public static IsbnToUpcConverter getInstance() {
      IsbnToUpcConverter instance = INSTANCE.get();
      Assert.notNull(instance, "The 'IsbnToUpcConverter' was not properly initialized!");
      return instance;
    }

    @Override
    public UPC convert(ISBN isbn) {
      int sum = 0;

      for (char digit : isbn.getTenDigitNumber().toCharArray()) {
        if (Character.isDigit(digit)) {
          sum += Integer.parseInt(String.valueOf(digit));
        }
      }

      return new UPC(isbn.getThirteenDigitNumber() + sum);
    }
  }
}
