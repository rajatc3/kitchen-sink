package org.johndoe.kitchensink.advices;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    private static final List<String> SENSITIVE_KEYS = List.of("email", "phoneNumber", "token", "password", "secret", "authorization");

    @Around("execution(* org.johndoe.kitchensink..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String methodName = getSimpleMethodName(joinPoint);

        Object[] sanitizedArgs = maskSensitiveData(joinPoint.getArgs());

        logger.info("ENTER: {} | Args: {}", methodName, Arrays.toString(sanitizedArgs));

        Object result = joinPoint.proceed();
        long elapsedTime = System.currentTimeMillis() - start;

        Object sanitizedResult = maskSensitiveData(result);
        logger.info("EXECUTED: {} | Time: {} ms", methodName, elapsedTime);
        logger.info("EXIT: {} | Result: {}", methodName, sanitizedResult);

        return result;
    }

    private String getSimpleMethodName(JoinPoint joinPoint) {
        return joinPoint.getSignature().getDeclaringType().getSimpleName() + "." +
                joinPoint.getSignature().getName() + getMethodParams(joinPoint);
    }

    private String getMethodParams(JoinPoint joinPoint) {
        return Arrays.stream(joinPoint.getSignature().getDeclaringType().getDeclaredMethods())
                .filter(m -> m.getName().equals(joinPoint.getSignature().getName()))
                .findFirst()
                .map(m -> Arrays.toString(Arrays.stream(m.getParameterTypes())
                        .map(Class::getSimpleName)
                        .toArray(String[]::new)))
                .orElse("()");
    }

    private Object[] maskSensitiveData(Object[] args) {
        return Arrays.stream(args)
                .map(this::maskSensitiveData)
                .toArray();
    }

    private Object maskSensitiveData(Object obj) {
        if (obj == null) return null;

        String objString = obj.toString();
        if (containsSensitiveData(objString)) {
            return maskSensitiveValues(objString);
        }

        if (obj instanceof Map<?, ?> map) {
            return maskSensitiveMap(map);
        } else if (obj instanceof List<?> list) {
            return list.stream().map(this::maskSensitiveData).toList();
        }

        return obj;
    }

    private String maskSensitiveValues(String str) {
        for (String key : SENSITIVE_KEYS) {
            str = str.replaceAll("(?i)(" + key + "\\s*[:=]\\s*)([^,\\s]+)", "$1***");
        }
        return str;
    }

    private boolean containsSensitiveData(String str) {
        String lowerStr = str.toLowerCase();
        return SENSITIVE_KEYS.stream().anyMatch(lowerStr::contains);
    }

    private Map<?, ?> maskSensitiveMap(Map<?, ?> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> containsSensitiveKey(entry.getKey().toString()) ? "***" : maskSensitiveData(entry.getValue())
                ));
    }

    private boolean containsSensitiveKey(String key) {
        String lowerKey = key.toLowerCase();
        return SENSITIVE_KEYS.stream().anyMatch(lowerKey::contains);
    }
}