package com.abderrahmane.elearning.common.repositories;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.abderrahmane.elearning.common.annotations.ClearCache;
import com.abderrahmane.elearning.common.models.Account;
import com.abderrahmane.elearning.common.models.City;
import com.abderrahmane.elearning.common.models.RequestForConnection;
import com.abderrahmane.elearning.common.models.SchoolProfile;
import com.abderrahmane.elearning.common.models.StudentProfile;
import com.abderrahmane.elearning.common.models.StudentTeacherConnection;
import com.abderrahmane.elearning.common.models.TeacherProfile;
import com.abderrahmane.elearning.common.utils.RandomStringGenerator;
import com.abderrahmane.elearning.common.utils.SQLUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ProfileDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AccountDAO accountDAO;

    private RandomStringGenerator rndStringGenerator = new RandomStringGenerator();

    @Transactional
    public SchoolProfile createSchoolProfil (String name, String overview, String subtitle, Account account, City location) {
        SchoolProfile schoolProfil = new SchoolProfile();

        schoolProfil.setAccount(account);
        schoolProfil.setName(name);
        schoolProfil.setSubtitle(subtitle);
        schoolProfil.setOverview(overview);
        schoolProfil.setLocation(location);
        account.setSchoolProfile(schoolProfil);

        accountDAO.saveAccount(account);

        return schoolProfil;
    }

    @Transactional
    public TeacherProfile createTeacherProfil (String firstname, String lastname, String title, String bio, Account account, City location) {
        TeacherProfile teacherProfil = new TeacherProfile();

        teacherProfil.setAccount(account);
        teacherProfil.setFirstname(firstname);
        teacherProfil.setLastname(lastname);
        teacherProfil.setBio(bio);
        teacherProfil.setTitle(title);
        teacherProfil.setLocation(location);
        account.setTeacherProfile(teacherProfil);

        accountDAO.saveAccount(account);

        return teacherProfil;
    }

    @Transactional
    public StudentProfile creatStudentProfile(String firstname, String lastname, Calendar dayOfBirth, City location, Account account) {
        StudentProfile studentProfile = new StudentProfile();

        studentProfile.setAccount(account);
        studentProfile.setDayOfBirth(dayOfBirth);
        studentProfile.setFirstname(firstname);
        studentProfile.setLastname(lastname);
        studentProfile.setLocation(location);
        account.setStudentProfile(studentProfile);

        accountDAO.saveAccount(account);

        return studentProfile;
    }

    @Transactional
    public SchoolProfile saveSchoolProfil (SchoolProfile schoolProfil) {
        entityManager.persist(schoolProfil);

        return schoolProfil;
    }

    // TODO : Implement advance search for best result
    // TODO : Escape query

    @ClearCache
    @SuppressWarnings("unchecked")
    public List<SchoolProfile> searchSchool (String name, int limit) {
        Query query = entityManager.createNativeQuery("select * from school_profil where name ~* ? LIMIT ?", SchoolProfile.class);
        query.setParameter(1, ".*" + name + ".*");
        query.setParameter(2, limit);

        return (List<SchoolProfile>)query.getResultList();
    }

    @Transactional
    public boolean teacherJoinSchool(String teacherId, String schoolId, String title) {
        Query query = entityManager.createNativeQuery("INSERT INTO teacher_school (teacher_id, school_id, title) VALUES (?, ?, ?)");
        query.setParameter(1, teacherId);
        query.setParameter(2, schoolId);
        query.setParameter(3, title);

        return query.executeUpdate() > 0;
    }

    @Transactional
    public boolean deleteTeacherSchool (String teacherId, String schoolId) {
        Query query = entityManager.createNativeQuery("DELETE FROM teacher_school WHERE teacher_id = ? AND school_id = ?");
        query.setParameter(1, teacherId);
        query.setParameter(2, schoolId);

        return query.executeUpdate() > 0;
    }

    @Transactional
    public boolean endTeacherSchool (String teacherId, String schoolId) {
        String sqlString = "UPDATE teacher_school SET ended_date = NOW() WHERE teacher_id = ? AND school_id = ? AND verified = true";
        Query query = entityManager.createNativeQuery(sqlString);
        query.setParameter(1, teacherId);
        query.setParameter(2, schoolId);

        return query.executeUpdate() > 0;
    }

    @Transactional
    public boolean teacherRejoinSchool (String teacherId, String schoolId, String title) {
        String sqlString = "UPDATE teacher_school SET ended_date = NULL, verified = false, title = ? WHERE teacher_id = ? AND school_id = ?";
        Query query = entityManager.createNativeQuery(sqlString);
        query.setParameter(1, title);
        query.setParameter(2, teacherId);
        query.setParameter(3, schoolId);

        return query.executeUpdate() > 0;
    }

    @Transactional
    public boolean updateTeacherProfile (Map<String, Object> data, String teacherId) {
        return SQLUtils.updateTable(this.entityManager, "teacher_profil", "account_id", teacherId, data);
    }
    
    @Transactional
    public boolean updateSchoolProfile (Map<String, Object> data, String schoolId) {
        return SQLUtils.updateTable(this.entityManager, "school_profil", "account_id", schoolId, data);
    }
    
    @Transactional
    public boolean updateStudentProfile (Map<String, Object> data, String studentId) {
        return SQLUtils.updateTable(this.entityManager, "student_profil", "account_id", studentId, data);
    }

    public List<TeacherProfile> searchTeachers (String searchQuery) {
        return this.searchTeachers(searchQuery, 10);
    }

    // FIXME : Escape search query
    // FIXME : Make indexed query

    @SuppressWarnings("unchecked")
    public List<TeacherProfile> searchTeachers (String searchQuery, int limit) {
        String sqlString = "SELECT * FROM teacher_profil WHERE to_tsvector(first_name || ' ' || last_name) @@ to_tsquery(?) LIMIT ?";
        Query query = this.entityManager.createNativeQuery(sqlString, TeacherProfile.class);
        String nameValues = String.join(" | ", List.of(searchQuery.split(" ")).stream().map(name -> name + ":*").toList());

        query.setParameter(1, nameValues);
        query.setParameter(2, limit);

        return (List<TeacherProfile>)query.getResultList();
    }

    @ClearCache
    public RequestForConnection getRequestForConnection (String id, String teacherId) {
        String sqlString = "SELECT * FROM request_for_connection WHERE id = ? AND teacher_id = ?";
        Query query = this.entityManager.createNativeQuery(sqlString, RequestForConnection.class);

        query.setParameter(1, id);
        query.setParameter(2, teacherId);

        try {
            return (RequestForConnection)query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Transactional
    public boolean deleteRequestForConnection (String id, String teacherId) {
        Query query = this.entityManager.createNativeQuery("DELETE FROM request_for_connection WHERE id = ? and teacher_id = ?");
        query.setParameter(1, id);
        query.setParameter(2, teacherId);

        return query.executeUpdate() >= 1;
    }

    @Transactional
    public boolean deleteRequestForConnection (String id) {
        Query query = this.entityManager.createNativeQuery("DELETE FROM request_for_connection WHERE id = ?");
        query.setParameter(1, id);

        return query.executeUpdate() >= 1;
    }

    @Transactional
    public boolean deleteRequestForConnectionFromStudent (String id, String studentId) {
        Query query = this.entityManager.createNativeQuery("DELETE FROM request_for_connection WHERE (id = ? OR teacher_id = ?) AND student_id = ?");
        query.setParameter(1, id);
        query.setParameter(2, id);
        query.setParameter(3, studentId);

        return query.executeUpdate() >= 1;
    }

    @Transactional
    public boolean createTeacherStudentConnection (String teacherId, String studentId) {
        Query query = this.entityManager.createNativeQuery("INSERT INTO connection (teacher_id, student_id) VALUES (?,?)");
        query.setParameter(1, teacherId);
        query.setParameter(2, studentId);

        return query.executeUpdate() >= 1;
    }

    @Transactional
    public boolean deleteTeacherStudentConnection (String teacherId, String studentId) {
        Query query = this.entityManager.createNativeQuery("DELETE FROM connection WHERE teacher_id = ? AND student_id = ?");
        query.setParameter(1, teacherId);
        query.setParameter(2, studentId);

        return query.executeUpdate() >= 1;
    }

    @Transactional
    public boolean createRequestForConnection (String studentId, String teacherId) {
        Query query = this.entityManager.createNativeQuery("INSERT INTO request_for_connection (id, teacher_id, student_id) VALUES (?,?,?)");
        query.setParameter(1, rndStringGenerator.generateRandomStr(25));
        query.setParameter(2, teacherId);
        query.setParameter(3, studentId);

        return query.executeUpdate() >= 1;
    }

    @ClearCache
    @SuppressWarnings("unchecked")
    public List<StudentTeacherConnection> getConnectionsList (String id, int limit, int offset) {
        CriteriaBuilder criteriaBuilder = this.entityManager.getCriteriaBuilder();
        CriteriaQuery<StudentTeacherConnection> cq = criteriaBuilder.createQuery(StudentTeacherConnection.class);
        Root<StudentTeacherConnection> root = cq.from(StudentTeacherConnection.class);

        Query query = this.entityManager.createQuery(cq.select(root).where(
            criteriaBuilder.or(
                criteriaBuilder.equal(root.get("teacherId"), id), 
                criteriaBuilder.equal(root.get("studentId"), id)
            )
        ));

        query.setFirstResult(offset);
        query.setMaxResults(limit);

        return (List<StudentTeacherConnection>)query.getResultList();
    }
}
