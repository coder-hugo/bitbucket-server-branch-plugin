package org.jenkinsci.plugins.bitbucket.server.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.jenkinsci.plugins.bitbucket.server.BitbucketPojoBuilder;

import java.util.List;

/**
 * @author Robin Müller
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@BitbucketPojoBuilder
public class Page<T> {

    private Integer size;
    private Integer limit;
    private Integer start;
    private Boolean isLastPage;
    private List<T> values;

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getStart() {
        return start;
    }

    public void setStart(Integer start) {
        this.start = start;
    }

    public Boolean getIsLastPage() {
        return isLastPage;
    }

    public void setIsLastPage(Boolean last) {
        isLastPage = last;
    }

    public List<T> getValues() {
        return values;
    }

    public void setValues(List<T> values) {
        this.values = values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Page<?> page = (Page<?>) o;
        return new EqualsBuilder()
                .append(size, page.size)
                .append(limit, page.limit)
                .append(start, page.start)
                .append(isLastPage, page.isLastPage)
                .append(values, page.values)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(size)
                .append(limit)
                .append(start)
                .append(isLastPage)
                .append(values)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("size", size)
                .append("limit", limit)
                .append("start", start)
                .append("isLast", isLastPage)
                .append("values", values)
                .toString();
    }
}
