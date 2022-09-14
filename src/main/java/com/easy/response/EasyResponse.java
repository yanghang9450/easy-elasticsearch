package com.easy.response;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
/**
 * @author yanghang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EasyResponse {
    public static <T>Result<T> build(final T data){
        return Result.of(data,0,"success");
    }
    public static <T>Result<T> fail(int code , String message){
        return Result.of(null,code,message);
    }
}
