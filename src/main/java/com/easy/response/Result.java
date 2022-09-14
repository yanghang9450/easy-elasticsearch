package com.easy.response;

import lombok.ToString;
import lombok.Value;

import java.io.Serializable;
/**
 * @author yanghang
 */
@ToString
@Value(staticConstructor = "of")
public class Result<T>  implements Serializable {
    private static final long serialVersionUID = 74551368L;
    T data;
    int code;
    String message;
}
