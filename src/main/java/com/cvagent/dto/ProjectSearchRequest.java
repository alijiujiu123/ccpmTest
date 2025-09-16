package com.cvagent.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class ProjectSearchRequest {

    private String query;
    private String status;
    private List<String> tags;
    private String sortBy = "updatedAt";
    private String sortOrder = "desc";
    private int page = 1;
    private int size = 10;

    // Getters and Setters
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Min(value = 1, message = "页码必须大于0")
    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    @Min(value = 1, message = "每页大小必须大于0")
    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}