package com.example.auth.book.controller;

import com.example.auth.book.dto.BookDto;
import com.example.auth.book.service.BookService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDto.Response create(@Valid @RequestBody BookDto.CreateRequest request) {
        return bookService.create(request);
    }

    @GetMapping
    public List<BookDto.Response> findAll() {
        return bookService.findAll();
    }

    @GetMapping("/{id}")
    public BookDto.Response findById(@PathVariable Long id) {
        return bookService.findById(id);
    }

    @PutMapping("/{id}")
    public BookDto.Response update(@PathVariable Long id, @Valid @RequestBody BookDto.UpdateRequest request) {
        return bookService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        bookService.delete(id);
    }
}
