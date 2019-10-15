package br.com.zecon;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import br.com.zecon.exceptions.NonexistentEntityException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Pierre
 */
public class SensoresTesteJpaController implements Serializable {

    public SensoresTesteJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(SensoresTeste sensoresTeste) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(sensoresTeste);
            em.getTransaction().commit();
        } finally {
            if (em != null)
                em.close();
        }
    }

    public void edit(SensoresTeste sensoresTeste) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            sensoresTeste = em.merge(sensoresTeste);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Integer id = sensoresTeste.getId();
                if (findSensoresTeste(id) == null)
                    throw new NonexistentEntityException("The sensoresTeste with id " + id + " no longer exists.");
            }
            throw ex;
        } finally {
            if (em != null)
                em.close();
        }
    }

    public void destroy(Integer id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            SensoresTeste sensoresTeste;
            try {
                sensoresTeste = em.getReference(SensoresTeste.class, id);
                sensoresTeste.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The sensoresTeste with id " + id + " no longer exists.", enfe);
            }
            em.remove(sensoresTeste);
            em.getTransaction().commit();
        } finally {
            if (em != null)
                em.close();
        }
    }

    public List<SensoresTeste> findSensoresTesteEntities() {
        return findSensoresTesteEntities(true, -1, -1);
    }

    public List<SensoresTeste> findSensoresTesteEntities(int maxResults, int firstResult) {
        return findSensoresTesteEntities(false, maxResults, firstResult);
    }

    private List<SensoresTeste> findSensoresTesteEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(SensoresTeste.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public SensoresTeste findSensoresTeste(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(SensoresTeste.class, id);
        } finally {
            em.close();
        }
    }

    public int getSensoresTesteCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<SensoresTeste> rt = cq.from(SensoresTeste.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

}
