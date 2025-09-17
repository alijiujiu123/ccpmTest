package com.cvagent.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "项目搜索请求", title = "项目搜索")
public class ProjectSearchRequest {

    @Schema(description = "搜索关键词", example = "电商")
    private String query;

    @Schema(description = "项目状态", example = "planning")
    private String status;

    @Schema(description = "项目标签列表")
    private List<String> tags;

    @Schema(description = "排序字段", example = "updatedAt", defaultValue = "updatedAt")
    private String sortBy = "updatedAt";

    @Schema(description = "排序方向", example = "desc", defaultValue = "desc")
    private String sortOrder = "desc";

    @Schema(description = "页码", example = "1", defaultValue = "1")
    private int page = 1;

    @Schema(description = "每页大小", example = "10", defaultValue = "10")
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