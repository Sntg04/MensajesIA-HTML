package com.ia.mensajes.agentemensajesia.dao;

import com.ia.mensajes.agentemensajesia.model.Mensaje;
import com.ia.mensajes.agentemensajesia.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class MensajeDAO {

    private EntityManager getEntityManager() {
        return JPAUtil.getEntityManagerFactory().createEntityManager();
    }

    public void guardarVarios(List<Mensaje> mensajes) {
        if (mensajes == null || mensajes.isEmpty()) {
            return;
        }
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            for (Mensaje mensaje : mensajes) {
                em.persist(mensaje);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Error al guardar la lista de mensajes en el DAO.", e);
        } finally {
            em.close();
        }
    }

    public List<Mensaje> listarAlertasPorLote(String loteId) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT m FROM Mensaje m " +
                          "WHERE m.loteCarga = :loteId AND m.clasificacion = :tipoClasificacion " +
                          "ORDER BY m.fechaCargaDb DESC";
            TypedQuery<Mensaje> query = em.createQuery(jpql, Mensaje.class);
            query.setParameter("loteId", loteId);
            query.setParameter("tipoClasificacion", "Alerta");
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    // --- MÉTODO AÑADIDO PARA OBTENER TODOS LOS MENSAJES DE UNA CARGA ---
    public List<Mensaje> listarPorLote(String loteCarga) {
        EntityManager em = getEntityManager();
        try {
            String jpql = "SELECT m FROM Mensaje m WHERE m.loteCarga = :lote ORDER BY m.id";
            TypedQuery<Mensaje> query = em.createQuery(jpql, Mensaje.class);
            query.setParameter("lote", loteCarga);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}