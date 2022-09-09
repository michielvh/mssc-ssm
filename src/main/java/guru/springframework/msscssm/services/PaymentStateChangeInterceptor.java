package guru.springframework.msscssm.services;

import java.util.Optional;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import guru.springframework.msscssm.domain.Payment;
import guru.springframework.msscssm.domain.PaymentEvent;
import guru.springframework.msscssm.domain.PaymentState;
import guru.springframework.msscssm.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {

    private final PaymentRepository paymentRepository;

    @Override
    public void preStateChange(State<PaymentState, PaymentEvent> state, Message<PaymentEvent> message,
            Transition<PaymentState, PaymentEvent> transition, StateMachine<PaymentState, PaymentEvent> stateMachine) {


        // before state change , if message is present, get the header which contains messageId
        // (we set this in .sendEvent() in PaymentserviceImpl
        // if paymentId is present, get the payment from the repo
        // set the state of the payment == stateId (stateId is class Paymentstate)
        // save the payment with the new state
        Optional.ofNullable(message).ifPresent(msg -> {
            Optional.ofNullable(Long.class.cast(msg.getHeaders().getOrDefault(
                    PaymentServiceImpl.PAYMENT_ID_HEADER,-1)))
                    .ifPresent(paymentId -> {
                        Payment payment = paymentRepository.getOne(paymentId);
                        System.out.println("PreStateChange : payment from DB PaymentState = " + payment.getState());
                        payment.setState(state.getId());
                        System.out.println("PreStateChange : stateMachine state = " + stateMachine.getState());
                        System.out.println("PreStateChange : transition source = " + transition.getSource());
                        System.out.println("PreStateChange : transition target = " + transition.getTarget());
                        System.out.println("PreStateChange : STATE_ID of state : " +state.getId());
                        System.out.println("PreStateChange : message : " +message.toString());
                        System.out.println("PreStateChange : Because of payload of message pre_auth_approved, transition source will go to transition target, defined in StateMachineConfig how the flow must go");
                        paymentRepository.save(payment);
                    });
        });

    }
}
