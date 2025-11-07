package com.wang.common.result;

import com.wang.common.enums.BizCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
public class Result {
    private Integer code; //编码：1成功，0和其它数字为失败
    private String msg; //错误信息
    private Object data; //数据

    public static  Result success() {
        Result result = new Result();
        result.code = 1;
        return result;
    }

    public static  Result success(Object object) {
        Result result = new Result();
        result.data = object;
        result.code = 1;
        return result;
    }

    public static  Result error(String msg) {
        Result result = new Result();
        result.msg = msg;
        result.code = 0;
        return result;
    }

    /**
     * 自定义状态码和错误信息
     * @param code
     * @param msg
     * @return
     */
    public static Result buildCodeAndMsg(int code, String msg) {
        return new Result(code, msg, null);
    }

    /**
     * 传入枚举，返回信息
     * @param codeEnum
     * @return
     */
    public static Result buildResult(BizCodeEnum codeEnum){
        return Result.buildCodeAndMsg(codeEnum.getCode(),codeEnum.getMessage());
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Result() {
    }

    public Result(Integer code, String msg, Object data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}
