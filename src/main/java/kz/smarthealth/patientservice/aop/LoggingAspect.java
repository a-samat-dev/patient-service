package kz.smarthealth.patientservice.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("@annotation(Log)")
    public void logPointcut() {
    }

    @Before("logPointcut() && args(obj)")
    public void beforeAdvice(JoinPoint joinPoint, Object obj) {
        log.info("Method call: {}, args: {}", joinPoint.getSignature().toShortString(), obj);
    }

    @AfterThrowing(value = "logPointcut()", throwing = "exception")
    public void afterThrowingAdvice(JoinPoint joinPoint, Throwable exception) {
        log.error("Exception thrown: {}, exception message: {}", joinPoint.getSignature().toShortString(),
                exception.getMessage());
    }
}
