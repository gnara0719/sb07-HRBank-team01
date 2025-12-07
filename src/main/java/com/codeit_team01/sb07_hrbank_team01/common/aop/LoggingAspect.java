package com.codeit_team01.sb07_hrbank_team01.common.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    private static final ObjectMapper prettyMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Pointcut("execution(* com.codeit_team01.sb07_hrbank_team01..controller..*(..))")
    public void allControllers() {}

    @Before("allControllers()")
    public void logBefore(JoinPoint joinPoint) {
        logger.info("[Time][Request]: {}.{}: Args = {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                getParametersAsString(joinPoint, false)
        );
    }

    @AfterReturning(pointcut = "allControllers()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        logger.info("[Time][Result]: {}.{}: Result = {}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                serializeResult(result, false)
        );
    }

    @AfterThrowing(pointcut = "allControllers()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        logger.error("[Time][Exception]: {}.{}: Exception = {}\n{}",
                joinPoint.getSignature().getDeclaringTypeName(),
                joinPoint.getSignature().getName(),
                ex.toString(),
                Arrays.toString(ex.getStackTrace())
        );
    }

    @Around("allControllers()")
    public Object logExecutionTime(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = null;
        Throwable thrown = null;
        try {
            result = pjp.proceed();
            return result;
        } catch (Throwable ex) {
            thrown = ex;
            throw ex;
        } finally {
            long time = System.currentTimeMillis() - start;
            if (thrown == null) {
                logger.info("\n[Time][Request]: {}.{}: Args = {}", pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName(), getParametersAsString(pjp, false));
                logger.info("[Time][Result]: {}.{}: Result = {} ({}ms)\n", pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName(), serializeResult(result, false), time);
            } else {
                logger.error("\n[Time][Request]: {}.{}: Args = {}", pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName(), getParametersAsString(pjp, false));
                logger.error("[Time][Exception]: {}.{}: Exception = {} ({}ms)\n{}\n", pjp.getSignature().getDeclaringTypeName(), pjp.getSignature().getName(), thrown.toString(), time, Arrays.toString(thrown.getStackTrace()));
            }
        }
    }

    private String getParametersAsString(JoinPoint joinPoint, boolean pretty) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args == null || args.length == 0) return "";
            if (pretty) return prettyMapper.writeValueAsString(args);
            return new ObjectMapper().writeValueAsString(args);
        } catch (Exception e) {
            return Arrays.toString(joinPoint.getArgs());
        }
    }

    private String serializeResult(Object result, boolean pretty) {
        try {
            if (result == null) return "null";
            if (result instanceof ResponseEntity && ((ResponseEntity) result).getBody() instanceof Resource) {
                return "FILE_DOWNLOAD_SKIP_LOGGING";
            }
            if (pretty) return prettyMapper.writeValueAsString(result);
            return new ObjectMapper().writeValueAsString(result);
        } catch (Exception e) {
            return result != null ? result.toString() : "null";
        }
    }
}

