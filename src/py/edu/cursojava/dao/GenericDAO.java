package py.edu.cursojava.dao;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.query.Query;

import py.edu.cursojava.utilidades.ConnectionHelper;


public abstract class GenericDAO <T>{
	
	protected Class<T> clase;
	
	public GenericDAO(Class<T> clase) {
		this.clase = clase;
	}

	protected Session getSession() {
		return ConnectionHelper.getSessionFactory().getCurrentSession();
	}
	
	protected void iniciarTransaccion() {
		if ( ! getSession().getTransaction().isActive()) {
			getSession().beginTransaction();	
		}
	}
	
	public void commit() {
		getSession().flush();
		getSession().getTransaction().commit();
	}
	
	public void rollback() {
		getSession().getTransaction().rollback();
	}
	
	public void guardar(T entity) throws Exception{
		iniciarTransaccion();
		getSession().saveOrUpdate(entity);
	}
	
	public void eliminar(T entity) throws Exception{
		iniciarTransaccion();
		getSession().delete(entity);
	}
	
	public T recuperarPorId(Serializable id) {
		iniciarTransaccion();
		T result = getSession().get(clase, id);
		commit();
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public List<T> recuperarTodo() {
		iniciarTransaccion();
		String hql = "from "+clase.getName()+" order by id";
		Query<T> query = getSession().createQuery(hql);
		List<T> lista = query.getResultList();
		commit();
		return lista;
	}

}
