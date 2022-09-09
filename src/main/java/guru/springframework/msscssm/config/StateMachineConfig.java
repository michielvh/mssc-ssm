package guru.springframework.msscssm.config;

import java.util.EnumSet;
import java.util.Random;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.services.PaymentServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableStateMachineFactory
@Configuration
@RequiredArgsConstructor
public class StateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    // Beans by bean-name
    private final Action<PaymentState, PaymentEvent> preAuthAction;
    private final Action<PaymentState, PaymentEvent> authAction;
    private final Guard<PaymentState, PaymentEvent> paymentIdGuard;
    private final Action<PaymentState, PaymentEvent> preAuthApprovedAction;
    private final Action<PaymentState, PaymentEvent> preAuthDeclinedAction;
    private final Action<PaymentState, PaymentEvent> authApprovedAction;
    private final Action<PaymentState, PaymentEvent> authDeclinedAction;

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states.withStates()
                .initial(PaymentState.NEW)
                .states(EnumSet.allOf(PaymentState.class))
                .end(PaymentState.AUTH)
                .end(PaymentState.PRE_AUTH_ERROR)
                .end(PaymentState.AUTH_ERROR);
    }


    // Describes how States change from source to target based on which event happens
    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions.withExternal()
                .source(PaymentState.NEW)
                .target(PaymentState.NEW)   // because state doesn't neccesairaly changes, target can be NEW
                .event(PaymentEvent.PRE_AUTHORIZE)
                .action(preAuthAction)
                .guard(paymentIdGuard)
                //source
                //to target
                //when event happens
                .and()
                .withExternal()
                .source(PaymentState.NEW).target(PaymentState.PRE_AUTH).event(PaymentEvent.PRE_AUTH_APPROVED).action(preAuthApprovedAction)
                .and()
                .withExternal()
                .source(PaymentState.NEW).target(PaymentState.PRE_AUTH_ERROR).event(PaymentEvent.PRE_AUTH_DECLINED).action(preAuthDeclinedAction)
                .and()
                .withExternal()
                .source(PaymentState.PRE_AUTH).target(PaymentState.PRE_AUTH).event(PaymentEvent.AUTHORIZE).action(authAction)
                .and()
                .withExternal()
                .source(PaymentState.PRE_AUTH).target(PaymentState.AUTH).event(PaymentEvent.AUTH_APPROVED).action(authApprovedAction)
                .and()
                .withExternal()
                .source(PaymentState.PRE_AUTH).target(PaymentState.AUTH_ERROR).event(PaymentEvent.AUTH_DECLINED).action(authDeclinedAction);
    }



    @Override
    public void configure(StateMachineConfigurationConfigurer<PaymentState, PaymentEvent> config) throws Exception {
        StateMachineListenerAdapter<PaymentState,PaymentEvent> adapter = new StateMachineListenerAdapter<>(){

            @Override
            public void stateChanged(State<PaymentState, PaymentEvent> from, State<PaymentState, PaymentEvent> to) {
                log.info(String.format("stateChanged(from: %s, to:%s)", from, to));
            }
        };

        config.withConfiguration().listener(adapter);
    }
/*
    public Guard<PaymentState,PaymentEvent> paymentIdGuard(){

        //if null give back a false, if true, return true
        return context -> {
            return context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER) != null;
        };
    }



    // goes first from new to new because of event Pre_authorize
    // than does this action  which sends event of pre_auth_approved
    // when source==NEW && target == Pre_AUTH && event == PRE_AUTH_APPROVED
    // state == PRE_AUTH
    // when source==NEW && target == Pre_AUTH && event == PRE_AUTH_DECLINED
    // state == PRE_AUTH_ERROR
    public Action<PaymentState,PaymentEvent> preAuthAction(){

        return context -> {
            System.out.println("PreAuth was called!!!");

            // random testcase for 80% of the time approved
            if(new Random().nextInt(10)<8){
                System.out.println("Approved");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_APPROVED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());

            }
            else {
                System.out.println("Declined! no credit!");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.PRE_AUTH_DECLINED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());
            }
        };
    }
/*
    private Action<PaymentState, PaymentEvent> authorize() {

        return context -> {
            System.out.println("Auth was called!!!");

            // random testcase for 80% of the time approved
            if(new Random().nextInt(10)<8){
                System.out.println("Approved");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.AUTH_APPROVED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());

            }
            else {
                System.out.println("Declined! Auth got declined!");
                context.getStateMachine().sendEvent(MessageBuilder.withPayload(PaymentEvent.AUTH_DECLINED)
                        .setHeader(PaymentServiceImpl.PAYMENT_ID_HEADER,context.getMessageHeader(PaymentServiceImpl.PAYMENT_ID_HEADER))
                        .build());
            }
        };

    }
*/
}
