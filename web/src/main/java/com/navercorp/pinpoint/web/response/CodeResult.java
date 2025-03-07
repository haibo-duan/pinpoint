/*
 * Copyright 2017 NAVER Corp.
 *
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

package com.navercorp.pinpoint.web.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

/**
 * @author Taejin Koo
 */
public class CodeResult {

    public static final int CODE_SUCCESS = 0;

    @Deprecated // instead use `throw new ResponseStatusException`
    public static final int CODE_FAIL = -1;

    private final int code;
    private final Object message;

    public static ResponseEntity<CodeResult> ok(Object message) {
        return ResponseEntity.ok(new CodeResult(CODE_SUCCESS, message));
    }

    public CodeResult(int code) {
        this(code, null);
    }

    public CodeResult(int code, Object message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public Object getMessage() {
        return message;
    }

}
