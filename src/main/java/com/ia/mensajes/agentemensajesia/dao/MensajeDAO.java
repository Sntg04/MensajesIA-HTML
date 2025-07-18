package com.ia.mensajes.agentemensajesia.dao;

import com.ia.mensajes.agentemensajesia.model.AsesorStats;
import com.ia.mensajes.agentemensajesia.model.EstadisticaMensaje;
import com.ia.mensajes.agentemensajesia.model.Mensaje;
import com.ia.mensajes.agentemensajesia.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class MensajeDAO {

    private EntityManager getEntityManager() {
        return JPAUtil.getEntityManagerFactory().createEntityManager();
    }

    public void guardarVarios(List<Mensaje> mensajes) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            for (Mensaje mensaje : mensajes) {
                em.persist(mensaje);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction() != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
    
    // --- NUEVO MÉTODO ---
    public Mensaje buscarPorId(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Mensaje.class, id);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
    
    // --- NUEVO MÉTODO ---
    public Mensaje actualizar(Mensaje mensaje) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Mensaje mensajeActualizado = em.merge(mensaje);
            em.getTransaction().commit();
            return mensajeActualizado;
        } catch (Exception e) {
            if (em.getTransaction() != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error al actualizar el mensaje", e);
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Mensaje> buscarTodos() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT m FROM Mensaje m ORDER BY m.id DESC", Mensaje.class).getResultList();
        } finally {
            if (em != null) em.close();
        }
    }
    
    public EstadisticaMensaje getEstadisticas() {
        EntityManager em = getEntityManager();
        try {
            Long total = em.createQuery("SELECT count(m) FROM Mensaje m", Long.class).getSingleResult();
            return new EstadisticaMensaje(total != null ? total : 0L, 0.0, 0.0);
        } catch (NoResultException e) {
            return new EstadisticaMensaje(0L, 0.0, 0.0);
        } finally {
            if (em != null) em.close();
        }
    }

    public long contarTotalMensajes(String asesorFiltro) {
        EntityManager em = getEntityManager();
        try {
            String queryString = "SELECT COUNT(m) FROM Mensaje m";
            if (asesorFiltro != null && !asesorFiltro.isEmpty()) {
                queryString += " WHERE m.nombreAsesor = :asesor";
            }
            TypedQuery<Long> query = em.createQuery(queryString, Long.class);
            if (asesorFiltro != null && !asesorFiltro.isEmpty()) {
                query.setParameter("asesor", asesorFiltro);
            }
            return query.getSingleResult();
        } finally {
            if (em != null) em.close();
        }
    }

    public List<Mensaje> buscarPaginado(String asesorFiltro, int numeroPagina, int tamanoPagina) {
        EntityManager em = getEntityManager();
        try {
            String queryString = "SELECT m FROM Mensaje m";
            if (asesorFiltro != null && !asesorFiltro.isEmpty()) {
                queryString += " WHERE m.nombreAsesor = :asesor";
            }
            queryString += " ORDER BY m.fechaProcesamiento DESC";
            TypedQuery<Mensaje> query = em.createQuery(queryString, Mensaje.class);
            if (asesorFiltro != null && !asesorFiltro.isEmpty()) {
                query.setParameter("asesor", asesorFiltro);
            }
            query.setFirstResult(numeroPagina * tamanoPagina);
            query.setMaxResults(tamanoPagina);
            return query.getResultList();
        } finally {
            if (em != null) em.close();
        }
    }

    public long contarTotalMensajesPorLote(String loteId) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(m) FROM Mensaje m WHERE m.lote = :loteId", Long.class);
            query.setParameter("loteId", loteId);
            return query.getSingleResult();
        } finally {
            if (em != null) em.close();
        }
    }

    public List<Mensaje> buscarPaginadoPorLote(String loteId, int numeroPagina, int tamanoPagina) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Mensaje> query = em.createQuery("SELECT m FROM Mensaje m WHERE m.lote = :loteId ORDER BY m.id DESC", Mensaje.class);
            query.setParameter("loteId", loteId);
            query.setFirstResult(numeroPagina * tamanoPagina);
            query.setMaxResults(tamanoPagina);
            return query.getResultList();
        } finally {
            if (em != null) em.close();
        }
    }

    public void borrarTodos() {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Mensaje").executeUpdate();
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction() != null && em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al borrar todos los mensajes en DAO", e);
        } finally {
            if (em != null) em.close();
        }
    }

    public List<String> obtenerNombresDeAsesores() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<String> query = em.createQuery("SELECT DISTINCT m.nombreAsesor FROM Mensaje m WHERE m.nombreAsesor IS NOT NULL AND m.nombreAsesor != '' ORDER BY m.nombreAsesor", String.class);
            return query.getResultList();
        } finally {
            if (em != null) em.close();
        }
    }
    
    public List<Object[]> contarMensajesPorAsesor() {
        EntityManager em = getEntityManager();
        try {
            String qlString = "SELECT m.nombreAsesor, COUNT(m), " +
                              "SUM(CASE WHEN m.clasificacion = 'Bueno' THEN 1 ELSE 0 END), " +
                              "SUM(CASE WHEN m.clasificacion = 'Alerta' THEN 1 ELSE 0 END) " +
                              "FROM Mensaje m WHERE m.nombreAsesor IS NOT NULL " +
                              "GROUP BY m.nombreAsesor " +
                              "ORDER BY COUNT(m) DESC";
            TypedQuery<Object[]> query = em.createQuery(qlString, Object[].class);
            return query.getResultList();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
}