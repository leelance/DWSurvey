package net.diaowen.common.base.controller;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.diaowen.common.plugs.httpclient.HttpResult;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 自定义默认异常统一拦截处理
 *
 * @author lance
 * @since 2023/3/11 11:56
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalWebAdviceController {
  private final HttpServletRequest request;

  /**
   * 拦截controller参数错误提示, 统一处理返回
   *
   * @param ex MethodArgumentNotValidException
   * @return R
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public HttpResult<String> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
    if (log.isInfoEnabled()) {
      log.info("Controller params fail[{}]: [{}]", request.getRequestURI(), ex.getMessage());
    }

    if (ex.getBindingResult().getFieldError() != null) {
      FieldError fieldError = ex.getBindingResult().getFieldError();
      String message = "";
      if (Objects.nonNull(fieldError)) {
        message = fieldError.getDefaultMessage();
      }
      return HttpResult.fail(message);
    }
    return HttpResult.fail("参数验证错误");
  }

  /**
   * 唯一索引重复异常
   */
  @ExceptionHandler({DuplicateKeyException.class})
  public HttpResult<String> duplicateKeyException(DuplicateKeyException ex) {
    log.info("Database duplicate Key fail[{}]: ", request.getRequestURI(), ex);
    return HttpResult.fail("数据重复保存");
  }

  /**
   * 资源未授权
   *
   * @param ex ex
   * @return R
   */
  @ExceptionHandler({ResourceAccessException.class})
  public HttpResult<String> handlerResourceAccessException(ResourceAccessException ex) {
    log.info("Resource access fail[{}]: ", request.getRequestURI(), ex);
    return HttpResult.fail("资源未授权");
  }

  /**
   * 处理GET请求中参数转换中的类型错误
   * 当应用程序试图将字符串转换成一种数值类型，但该字符串不能转换为适当格式时，抛出该异常
   */
  @ExceptionHandler(NumberFormatException.class)
  public HttpResult<String> handleNumberFormatException(NumberFormatException ex) {
    log.info("Number format fail[{}]: ", request.getRequestURI(), ex);
    return HttpResult.fail("数据类型转换错误");
  }

  /**
   * 参数未进行json序列化
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public HttpResult<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
    log.info("Http message not readable fail[{}]: ", request.getRequestURI(), ex);
    return HttpResult.fail("参数序列化失败");
  }

  /**
   * 方法参数类型不匹配异常
   *
   * @param ex ex
   * @return R<String>
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public HttpResult<String> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
    log.info("Method argument type mismatch fail[{}]: ", request.getRequestURI(), ex);
    return HttpResult.fail("方法参数类型不匹配");
  }

  /**
   * 请求参数确实异常
   *
   * @param ex ex
   * @return R<String>
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public HttpResult<String> handleConstraintViolationException(MissingServletRequestParameterException ex) {
    log.info("Missing servlet request parameter fail[{}]: ", request.getRequestURI(), ex);
    return HttpResult.fail("请求参数缺失");
  }

  /**
   * 处理POST请求中 @RequestBody 注解进行json转换中的类型错误异常,json反序列化失败
   *
   * @param ex ex
   * @return R<String>
   */
  @ExceptionHandler(InvalidFormatException.class)
  public HttpResult<String> handleInvalidFormatException(InvalidFormatException ex) {
    log.info("Invalid format fail[{}]: ", request.getRequestURI(), ex);
    return HttpResult.fail("参数格式错误");
  }

  /**
   * http 请求不支持类型
   *
   * @param ex HttpRequestMethodNotSupportedException
   * @return R
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public HttpResult<String> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
    log.info("Not supported[{}]: {}", request.getRequestURI(), ex.getMessage());
    return HttpResult.fail("http请求类型不支持");
  }

  /**
   * 自定义业务错误, 类型
   *
   * @param ex ServiceException
   * @return 返回异常
   */
  @ExceptionHandler(Exception.class)
  public HttpResult<String> handleException(Exception ex) {
    log.error("unknown exception fail[{}]: ", request.getRequestURI(), ex);
    return HttpResult.fail("未知错误");
  }
}
