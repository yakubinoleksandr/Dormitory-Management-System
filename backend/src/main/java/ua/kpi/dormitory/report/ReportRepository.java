package ua.kpi.dormitory.report;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

/** Лише читання: окремих таблиць звітності немає (лаб. 2, розділ 2). */
@Repository
public class ReportRepository {
    private final EntityManager em;

    public ReportRepository(EntityManager em) { this.em = em; }

    public List<ResidentRow> activeResidents(Long dormitoryId) {
        return em.createQuery("""
                select new ua.kpi.dormitory.report.ResidentRow(s.contractNumber, p.fullName, r.roomNumber, b.bedNumber)
                from Settlement s
                  join s.bed b
                  join b.room r
                  join ua.kpi.dormitory.profile.ResidentProfile p on p.account = s.residentAccount
                where r.dormitory.id = :dormitoryId
                  and s.status = ua.kpi.dormitory.settlement.SettlementStatus.ACTIVE
                order by r.floor, r.roomNumber, b.bedNumber
                """, ResidentRow.class)
                .setParameter("dormitoryId", dormitoryId)
                .getResultList();
    }

    public List<Object[]> freeBeds(Long dormitoryId) {
        return em.createQuery("""
                select r.roomNumber, b.bedNumber from Bed b join b.room r
                where r.dormitory.id = :dormitoryId and b.occupied = false
                  and r.status <> ua.kpi.dormitory.inventory.RoomStatus.REPAIR
                order by r.floor, r.roomNumber, b.bedNumber
                """, Object[].class)
                .setParameter("dormitoryId", dormitoryId)
                .getResultList();
    }
}
