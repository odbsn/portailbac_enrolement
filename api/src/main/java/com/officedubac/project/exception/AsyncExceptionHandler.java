package com.officedubac.project.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Component
@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler{

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        final String msg = "Async Exception**************************************************************"
                + "\nmethod happen: " + method
                + "\nmethod params: " + Arrays.toString(params)
                + "\nException class: {}" + ex.getClass().getName()
                + "\nex.getMessage(): {}" + ex.getMessage()
                + "\n***************************************************************************************************";

        log.error(msg, ex);
    }

}
