package ru.krizhanovskiy.p2ptransfers.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimingAspect {

    private static final Logger log = LoggerFactory.getLogger(TimingAspect.class);

    @Around("@annotation(ru.krizhanovskiy.p2ptransfers.annotations.Timed)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object proceed = joinPoint.proceed();

        long duration = System.currentTimeMillis() - start;

        String methodName = joinPoint.getSignature().toShortString();
        log.info("{} executed in {} ms", methodName, duration);

        return proceed;
    }
}
