package net.diaowen.common.plugs.httpclient;

import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

/**
 * PageResult
 *
 * @author diaowen
 * @since 2023/3/9 23:47
 */
@Data
public class PageResult<T> {

  private Integer total;
  private Integer current = 1;
  private boolean success;
  private Integer pageSize;

  private List<T> data;

  public static <T> PageResult<T> convert(Page<T> page) {
    PageResult<T> result = new PageResult<>();
    result.setSuccess(true);
    result.setCurrent(page.getPageable().getPageNumber());
    result.setPageSize(page.getPageable().getPageSize());
    result.setTotal((int) page.getTotalElements());
    result.setData(page.getContent());
    return result;
  }

  public PageRequest to() {
    return PageRequest.of(this.getCurrent() - 1, this.getPageSize());
  }

  public Integer getCurrent() {
    if (current == null || current <= 0) {
      current = 1;
    }
    return current;
  }

  public Integer getPageSize() {
    if (pageSize == null) {
      pageSize = 20;
    }
    return pageSize;
  }
}
