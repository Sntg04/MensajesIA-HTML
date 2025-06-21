package com.ia.mensajes.agentemensajesia.dao;

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

    public void guardar(Mensaje mensaje) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(mensaje);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public List<Mensaje> buscarTodos() {
        EntityManager em = getEntityManager();
        try {
            return em.createQuery("SELECT m FROM Mensaje m ORDER BY m.fechaProcesamiento DESC", Mensaje.class).getResultList();
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    // --- MÉTODO AÑADIDO QUE SOLUCIONA EL ERROR ---
    /**
     * Calcula las estadísticas agregadas sobre los mensajes en la base de datos.
     * @return Un objeto EstadisticaMensaje con los resultados.
     */
    public EstadisticaMensaje getEstadisticas() {
        EntityManager em = getEntityManager();
        try {
            Long totalMensajes = em.createQuery("SELECT count(m) FROM Mensaje m", Long.class)
                                   .getSingleResult();

            Double confianzaSpam = em.createQuery("SELECT avg(m.confianza) FROM Mensaje m WHERE m.clasificacion = 'Alerta'", Double.class)
                                     .getSingleResult();
            
            Double confianzaNoSpam = em.createQuery("SELECT avg(m.confianza) FROM Mensaje m WHERE m.clasificacion = 'Bueno'", Double.class)
                                       .getSingleResult();

            EstadisticaMensaje estadisticas = new EstadisticaMensaje();
            estadisticas.setTotalMensajes(totalMensajes != null ? totalMensajes : 0L);
            estadisticas.setConfianzaPromedioSpam(confianzaSpam != null ? confianzaSpam : 0.0);
            estadisticas.setConfianzaPromedioNoSpam(confianzaNoSpam != null ? confianzaNoSpam : 0.0);
            
            return estadisticas;

        } catch (NoResultException e) {
            return new EstadisticaMensaje(0L, 0.0, 0.0);
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}