package com.spartaecommerce.refund.domain.port.out;

import com.spartaecommerce.common.exception.BusinessException;
import com.spartaecommerce.common.exception.ErrorCode;
import com.spartaecommerce.refund.application.dto.query.RefundSearchQuery;
import com.spartaecommerce.refund.domain.entity.Refund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class RefundFakeRepository implements LoadRefundPort, SaveRefundPort {

    private final Map<Long, Refund> repository = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    @Override
    public Long save(Refund refund) {
        if (refund.getRefundId() == null) {
            long refundId = idGenerator.getAndIncrement();
            Refund newRefund = Refund.builder()
                .refundId(refundId)
                .userId(refund.getUserId())
                .orderId(refund.getOrderId())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
            repository.put(refundId, newRefund);
            return refundId;
        } else {
            Refund updatedRefund = Refund.builder()
                .refundId(refund.getRefundId())
                .userId(refund.getUserId())
                .orderId(refund.getOrderId())
                .reason(refund.getReason())
                .status(refund.getStatus())
                .createdAt(refund.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
            repository.put(refund.getRefundId(), updatedRefund);
            return refund.getRefundId();
        }
    }

    @Override
    public Optional<Refund> findById(Long refundId) {
        return Optional.ofNullable(repository.get(refundId));
    }

    @Override
    public Refund getById(Long refundId) {
        return findById(refundId)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND, "refundId: " + refundId));
    }

    @Override
    public Optional<Refund> findByOrderId(Long orderId) {
        return repository.values().stream()
            .filter(refund -> refund.getOrderId().equals(orderId))
            .findFirst();
    }

    @Override
    public Page<Refund> search(RefundSearchQuery searchQuery) {
        Stream<Refund> stream = repository.values().stream();

        // userId 필터
        if (searchQuery.userId() != null) {
            stream = stream.filter(refund -> refund.getUserId().equals(searchQuery.userId()));
        }

        // status 필터
        if (searchQuery.refundStatus() != null) {
            stream = stream.filter(refund -> refund.getStatus().equals(searchQuery.refundStatus()));
        }

        List<Refund> filteredRefunds = stream
            .sorted((r1, r2) -> r2.getRefundId().compareTo(r1.getRefundId())) // ID 내림차순
            .toList();

        PageRequest pageRequest = PageRequest.of(
            searchQuery.pageable().page(),
            searchQuery.pageable().size()
        );

        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), filteredRefunds.size());

        List<Refund> pageContent = filteredRefunds.subList(start, end);
        return new PageImpl<>(pageContent, pageRequest, filteredRefunds.size());
    }

    public void clear() {
        repository.clear();
        idGenerator.set(1L);
    }
}
