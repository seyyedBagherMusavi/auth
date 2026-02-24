package com.example.auth.book.service;

import com.example.auth.book.dto.BookDto;
import com.example.auth.book.entity.Book;
import com.example.auth.book.repository.BookRepository;
import com.example.auth.category.service.CategoryService;
import com.example.auth.common.exception.NotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryService categoryService;

    public BookService(BookRepository bookRepository, CategoryService categoryService) {
        this.bookRepository = bookRepository;
        this.categoryService = categoryService;
    }

    public BookDto.Response create(BookDto.CreateRequest request) {
        categoryService.findEntityById(request.categoryId());

        Book book = new Book(request.title(), request.author(), request.categoryId());
        Book savedBook = bookRepository.save(book);
        Book loadedBook = findEntityById(savedBook.getId());
        return BookDto.Response.from(loadedBook);
    }

    @Transactional(readOnly = true)
    public List<BookDto.Response> findAll() {
        return bookRepository.findAll().stream()
                .map(BookDto.Response::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public Book findEntityById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found with id=" + id));
    }

    @Transactional(readOnly = true)
    public BookDto.Response findById(Long id) {
        return BookDto.Response.from(findEntityById(id));
    }

    public BookDto.Response update(Long id, BookDto.UpdateRequest request) {
        Book book = findEntityById(id);
        categoryService.findEntityById(request.categoryId());

        book.update(request.title(), request.author(), request.categoryId());
        return BookDto.Response.from(book);
    }

    public void delete(Long id) {
        Book book = findEntityById(id);
        bookRepository.delete(book);
    }
}
