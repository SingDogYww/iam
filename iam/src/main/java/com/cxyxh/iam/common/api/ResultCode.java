/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cxyxh.iam.common.api;

/**
 * 常用API返回码
 *
 * @author IAM
 */
public enum ResultCode implements IErrorCode {
    /**
     * 操作成功
     */
    SUCCESS(200, "操作成功"),
    /**
     * 操作失败
     */
    FAILED(500, "操作失败"),
    /**
     * 参数检验失败
     */
    VALIDATE_FAILED(400, "参数检验失败"),
    /**
     * 暂未登录或token已过期
     */
    UNAUTHORIZED(401, "暂未登录或token已过期"),
    /**
     * 没有相关权限
     */
    FORBIDDEN(403, "没有相关权限");
    
    private final long code;
    private final String message;
    
    ResultCode(long code, String message) {
        this.code = code;
        this.message = message;
    }
    
    @Override
    public long getCode() {
        return code;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
} 