package egovframework.common.config;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import javax.sql.DataSource;
import java.util.Collections;

/**
 * @ClassName : TransactionConfig.java
 * @Description : TransactionConfig Java Config 설정
 *
 * @author : tspark
 * @since : 2025. 11. 06
 * @version : 1.0
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfig {
	private static final int TX_METHOD_TIMEOUT = 300;
	private static final String AOP_POINTCUT_EXPRESSION = "execution(* egovframework..service..*Service.*(..))"
                                                        + "&& !execution(* egovframework.common.audit.service..*Service.*(..))"; // audit 제외
	private static final String AUDIT_POINTCUT_EXPRESSION = "execution(* egovframework.common.audit.service..*Service.*(..))"; // audit 전용
	private static final String OPERATION_TX_MANAGER = "operationTxManager";

    @Bean(name = OPERATION_TX_MANAGER)
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public TransactionInterceptor transactionAdvice(PlatformTransactionManager transactionManager) {
        NameMatchTransactionAttributeSource txAttributeSource = new NameMatchTransactionAttributeSource();

        // read-only 트랜잭션 (조회용)
        RuleBasedTransactionAttribute readOnlyTx = new RuleBasedTransactionAttribute();
        readOnlyTx.setReadOnly(true);
        readOnlyTx.setTimeout(TX_METHOD_TIMEOUT);
        readOnlyTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        // 일반 트랜잭션 (CUD용)
        RuleBasedTransactionAttribute writeTx = new RuleBasedTransactionAttribute();
        writeTx.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));
        writeTx.setTimeout(TX_METHOD_TIMEOUT);
        writeTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        // 메서드 패턴별 트랜잭션 적용
        txAttributeSource.addTransactionalMethod("get*", readOnlyTx);
        txAttributeSource.addTransactionalMethod("select*", readOnlyTx);
        txAttributeSource.addTransactionalMethod("find*", readOnlyTx);
        txAttributeSource.addTransactionalMethod("list*", readOnlyTx);
        txAttributeSource.addTransactionalMethod("search*", readOnlyTx);
        txAttributeSource.addTransactionalMethod("count*", readOnlyTx);

        txAttributeSource.addTransactionalMethod("insert*", writeTx);
        txAttributeSource.addTransactionalMethod("update*", writeTx);
        txAttributeSource.addTransactionalMethod("delete*", writeTx);
        txAttributeSource.addTransactionalMethod("upsert*", writeTx);
        txAttributeSource.addTransactionalMethod("save*", writeTx);
        txAttributeSource.addTransactionalMethod("create*", writeTx);
        txAttributeSource.addTransactionalMethod("remove*", writeTx);
        txAttributeSource.addTransactionalMethod("modify*", writeTx);
        txAttributeSource.addTransactionalMethod("reset*", writeTx);
        txAttributeSource.addTransactionalMethod("*", writeTx);
        
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(transactionManager);
        interceptor.setTransactionAttributeSource(txAttributeSource);

        return interceptor;
    }

    /**
     * 일반 Service 트랜잭션 AOP Advisor 설정 (audit 제외)
     */
    @Bean
    @Order(2)
    public Advisor transactionAdvisor(@Qualifier("transactionAdvice") TransactionInterceptor transactionAdvice) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(AOP_POINTCUT_EXPRESSION);

        return new DefaultPointcutAdvisor(pointcut, transactionAdvice);
    }

    /**
     * Audit Service 전용 트랜잭션 Interceptor
     * 항상 새로운 트랜잭션을 생성 (PROPAGATION_REQUIRES_NEW)
     */
    @Bean
    public TransactionInterceptor auditTransactionAdvice(PlatformTransactionManager transactionManager) {
        NameMatchTransactionAttributeSource txAttributeSource = new NameMatchTransactionAttributeSource();

        // Audit용 트랜잭션 속성 - 항상 새 트랜잭션 생성
        RuleBasedTransactionAttribute auditTx = new RuleBasedTransactionAttribute();
        auditTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        auditTx.setRollbackRules(Collections.singletonList(new RollbackRuleAttribute(Exception.class)));
        auditTx.setTimeout(TX_METHOD_TIMEOUT);

        // 모든 메서드에 동일한 트랜잭션 속성 적용
        txAttributeSource.addTransactionalMethod("*", auditTx);

        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(transactionManager);
        interceptor.setTransactionAttributeSource(txAttributeSource);

        return interceptor;
    }

    /**
     * Audit Service 전용 AOP Advisor 설정
     */
    @Bean
    @Order(1)
    public Advisor auditTransactionAdvisor(@Qualifier("auditTransactionAdvice") TransactionInterceptor auditTransactionAdvice) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(AUDIT_POINTCUT_EXPRESSION);

        return new DefaultPointcutAdvisor(pointcut, auditTransactionAdvice);
    }
}
