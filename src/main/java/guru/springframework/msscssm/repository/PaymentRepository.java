package guru.springframework.msscssm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import guru.springframework.msscssm.domain.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

}
