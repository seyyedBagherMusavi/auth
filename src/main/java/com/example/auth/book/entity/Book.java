package com.example.auth.book.entity;

import com.example.auth.category.entity.Category;
import com.example.auth.common.entity.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "books")
public class Book extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false, insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_book_category"))
    private Category category;

    protected Book() {
    }

    public Book(String title, String author, Long categoryId) {
        this.title = title;
        this.author = author;
        this.categoryId = categoryId;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public Category getCategory() {
        return category;
    }

    public void update(String title, String author, Long categoryId) {
        this.title = title;
        this.author = author;
        this.categoryId = categoryId;
    }
}
