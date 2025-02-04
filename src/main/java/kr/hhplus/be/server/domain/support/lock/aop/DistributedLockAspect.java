package kr.hhplus.be.server.domain.support.lock.aop;

import kr.hhplus.be.server.domain.support.lock.DistributedLockManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {
    private static final String REDISSON_LOCK_PREFIX = "LOCK:";

    private final DistributedLockManager lockManager;

    @Around("@annotation(kr.hhplus.be.server.domain.support.lock.aop.DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable { // Throwable 도 필수인가?
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);


        // template으로 가 있는 구조.
        // advice 로직에 필요한 게 결국 구체 클라이언트라면.? 이러면 어드바이스가 너무 구체에 의존하는 꼴.
            // 결국 어드바이스 내부에 특정 분산 락 클라이언트 로직에 의존하고 있는 구조..?
        // 일단 근데 성급하게 구조화 하지 말고 일단 실제 로직에 고민을 더 하는 게 맞아보임.!
        // 근데 그럼에도 결합도늘 낮추고 싶긴 함..! >> 꼭 필요 없다면,

        // 우리는 어노테이션을 통해 락 전략을 받음 > 락 전략을 통해 클라이언트를 구체화 할 수 있음 > 클라이언트 내부에서 특정 로직들을 드러낼 수 있음. (단, 어드바이스에 드러나게 하고 싶지 않음)

        // 락을 획득하지 못했을 때 어떻게 할지 어떤 전략들이 있는거지?
        String key = REDISSON_LOCK_PREFIX + getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributedLock.key());
        try {
            boolean available = lockManager.tryLock(key, distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            if (!available) {
                return false;
            }
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            // 락을 획득하지 못했을 때 어떤 에러를?
            throw new InterruptedException();
        } finally {
            lockManager.unlock(key);
        }
    }

    private Object getDynamicValue(String[] parameterNames, Object[] args, String key) {
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();

        for (int i = 0; i < parameterNames.length; i++) {
            context.setVariable(parameterNames[i], args[i]);
        }

        return parser.parseExpression(key).getValue(context, Object.class);
    }
}