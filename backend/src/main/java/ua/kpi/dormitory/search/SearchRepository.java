package ua.kpi.dormitory.search;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

/** Лише читання. Пошук обмежено переданим набором дозволених гуртожитків. */
@Repository
public class SearchRepository {
    private static final int LIMIT = 50;
    private final EntityManager em;

    public SearchRepository(EntityManager em) { this.em = em; }

    public List<ResidentHit> residents(String like, Collection<Long> dormitoryIds) {
        return em.createQuery("""
                select new ua.kpi.dormitory.search.ResidentHit(p.account.id, p.fullName, s.contractNumber,
                       r.roomNumber, d.nameOrNumber, s.status)
                from Settlement s
                  join s.bed b join b.room r join r.dormitory d
                  join ua.kpi.dormitory.profile.ResidentProfile p on p.account = s.residentAccount
                where d.id in :ids
                  and (lower(p.fullName) like :q or lower(s.contractNumber) like :q)
                order by p.fullName""", ResidentHit.class)
                .setParameter("ids", dormitoryIds).setParameter("q", like).setMaxResults(LIMIT).getResultList();
    }

    public List<RoomHit> rooms(String like, Collection<Long> dormitoryIds) {
        return em.createQuery("""
                select new ua.kpi.dormitory.search.RoomHit(r.id, r.roomNumber, d.id, d.nameOrNumber)
                from Room r join r.dormitory d
                where d.id in :ids and lower(r.roomNumber) like :q
                order by d.nameOrNumber, r.floor, r.roomNumber""", RoomHit.class)
                .setParameter("ids", dormitoryIds).setParameter("q", like).setMaxResults(LIMIT).getResultList();
    }

    public List<DormitoryHit> dormitories(String like, Collection<Long> dormitoryIds) {
        return em.createQuery("""
                select new ua.kpi.dormitory.search.DormitoryHit(d.id, d.nameOrNumber, d.address)
                from Dormitory d
                where d.id in :ids and (lower(d.nameOrNumber) like :q or lower(d.address) like :q)
                order by d.nameOrNumber""", DormitoryHit.class)
                .setParameter("ids", dormitoryIds).setParameter("q", like).setMaxResults(LIMIT).getResultList();
    }
}
