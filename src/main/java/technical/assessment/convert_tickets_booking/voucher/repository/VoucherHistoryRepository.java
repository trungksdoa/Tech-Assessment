package technical.assessment.convert_tickets_booking.voucher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import technical.assessment.convert_tickets_booking.voucher.model.VoucherHistory;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoucherHistoryRepository extends JpaRepository<VoucherHistory, Integer> {
    List<VoucherHistory> findByUserId(Integer userId);
    Optional<VoucherHistory> findByUserIdAndVoucherId(Integer userId, Integer voucherId);
    
    @Query("SELECT COUNT(v.id) FROM VoucherHistory v WHERE v.userId = :userId AND v.voucher.id = :voucherId")
    int countByUserIdAndVoucherId(@Param("userId") Integer userId, @Param("voucherId") Integer voucherId);
}
