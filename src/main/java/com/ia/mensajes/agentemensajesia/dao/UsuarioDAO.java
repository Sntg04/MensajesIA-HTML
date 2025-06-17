package com.ia.mensajes.agentemensajesia.dao;

import com.ia.mensajes.agentemensajesia.model.Usuario;
import com.ia.mensajes.agentemensajesia.util.JPAUtil; // ¡NUEVA IMPORTACIÓN!
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class UsuarioDAO {

    // ¡YA NO CREAMOS EL EMF AQUÍ! Lo obtenemos de JPAUtil.
    // private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("AgenteMensajesIAPU");

    public EntityManager getEntityManager() {
        // Obtenemos la instancia singleton del factory desde nuestra clase de utilidad
        return JPAUtil.getEntityManagerFactory().createEntityManager();
    }

    // ... EL RESTO DE TUS MÉTODOS (crear, buscarPorId, etc.) NO NECESITA CAMBIOS ...
    // Te dejo el método crear como ejemplo de que el resto del código no cambia.

    public Usuario crear(Usuario usuario) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(usuario);
            em.getTransaction().commit();
            return usuario;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace(); 
            throw new RuntimeException("Error al crear el usuario en DAO", e); 
        } finally {
            em.close();
        }
    }

    // (Asegúrate de que todos tus otros métodos DAO sigan aquí: 
    // buscarPorId, buscarPorUsername, actualizar, eliminar, listarTodos)
    public Usuario buscarPorId(Integer id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Usuario.class, id);
        } finally {
            em.close();
        }
    }

    public Usuario buscarPorUsername(String username) {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Usuario> query = em.createQuery(
                "SELECT u FROM Usuario u WHERE u.username = :username", Usuario.class);
            query.setParameter("username", username);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null; 
        } finally {
            em.close();
        }
    }

    public Usuario actualizar(Usuario usuario) {
        EntityManager em = getEntityManager();
        Usuario usuarioActualizado = null;
        try {
            em.getTransaction().begin();
            usuarioActualizado = em.merge(usuario);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
        return usuarioActualizado;
    }

    public void eliminar(Integer id) {
        EntityManager em = getEntityManager();
        try {
            em.getTransaction().begin();
            Usuario usuario = em.find(Usuario.class, id);
            if (usuario != null) {
                em.remove(usuario);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }

    public List<Usuario> listarTodos() {
        EntityManager em = getEntityManager();
        try {
            TypedQuery<Usuario> query = em.createQuery("SELECT u FROM Usuario u", Usuario.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
}