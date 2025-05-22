package org.ouanu.manager.dto;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class ResponseResult<T> {
    private final int code;
    private final String message;
    private final T data;
    private final long timestamp;

    public ResponseResult(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        timestamp = System.currentTimeMillis();
    }

    public static <T> ResponseEntity<ResponseResult<T>> success() {
        return success(null);
    }

    public static <T> ResponseEntity<ResponseResult<T>> success(T data) {
        return ResponseEntity.ok(
                new ResponseResult<>(
                        HttpStatus.OK.value(),
                        "操作成功",
                        data
                )
        );
    }

    public static <T> ResponseEntity<ResponseResult<T>> created() {
        return created(null);
    }

    public static <T> ResponseEntity<ResponseResult<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseResult<>(
                        HttpStatus.CREATED.value(),
                        "资源创建成功",
                        data
                ));
    }

    public static <T> ResponseEntity<ResponseResult<T>> fail(HttpStatus status, String message) {
        return ResponseEntity
                .status(status)
                .body(new ResponseResult<>(
                        status.value(),
                        message,
                        null
                ));
    }
}
