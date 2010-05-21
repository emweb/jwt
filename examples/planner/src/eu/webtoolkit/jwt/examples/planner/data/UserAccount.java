package eu.webtoolkit.jwt.examples.planner.data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Query;


/**
 *  A user of the planner application
 */
@Entity
public class UserAccount implements Serializable {
	@Id @GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	
	@Column (nullable = false)
	private String name;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	@SuppressWarnings("unchecked")
	public List<Entry> getEntriesInRange(EntityManager em, Date from, Date untill) {
		Query q = em.createQuery("from Entry entry where entry.start >= :from and entry.start < :untill and entry.userAccount = :user");
	
		q.setParameter("from", from);
		q.setParameter("untill", untill);
		q.setParameter("user", this);
		
		return q.getResultList();
	}
	
	public static UserAccount login(EntityManager em, String user) {
		EntityTransaction et = em.getTransaction();
		try {
			et.begin();
			Query q = em.createQuery("from UserAccount ua where ua.name = :user");
			q.setParameter("user", user);
	
			UserAccount ua = null;
			List<UserAccount> result = q.getResultList();
			if (result.size() == 0) {
				ua = new UserAccount();
				ua.setName(user);
				em.persist(ua);
			} else {
				ua = result.get(0);
			}
			et.commit();
			
			return ua;
		} finally {
			if (et.isActive())
				et.rollback();
		}
	}

	public void addEntry(EntityManager em, Entry e) {
		EntityTransaction et = em.getTransaction();
		try {
			et.begin();
			e.setUser(this);
			em.persist(e);
			et.commit();
		} finally {
			if (et.isActive())
				et.rollback();
		}	
	}
}
