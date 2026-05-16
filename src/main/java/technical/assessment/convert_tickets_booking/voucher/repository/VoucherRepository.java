package technical.assessment.convert_tickets_booking.voucher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import technical.assessment.convert_tickets_booking.voucher.model.Voucher;

import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    Optional<Voucher> findByCode(String code);

    @Modifying
    @Query("UPDATE Voucher v SET v.currentUsage = v.currentUsage + 1 " +
            "WHERE v.id = :id AND (v.maxUsage IS NULL OR v.currentUsage < v.maxUsage)")
    int incrementUsage(@Param("id") Integer id);
}
