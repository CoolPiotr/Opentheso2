package fr.cnrs.opentheso.bean.condidat.dao;

import fr.cnrs.opentheso.bean.menu.connect.Connect;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class TermeDao extends BasicDao {
    
    
    public void saveIntitule(Connect connect, String intitule, String idThesaurus, 
            String lang, String idConcept, String idUser, String idTerm) throws SQLException {
        
        stmt = connect.getPoolConnexion().getConnection().createStatement();
        
        // insert in preferred_term
        executInsertRequest(stmt,
                "INSERT INTO preferred_term(id_concept, id_term, id_thesaurus) VALUES ('"
                        +idConcept+"', '"+idTerm+"', '"+idThesaurus+"')");
        
        // insert in non_preferred_term      
        executInsertRequest(stmt,
                new StringBuffer("INSERT INTO non_preferred_term(lexical_value, lang, id_thesaurus, hiden, id_term) VALUES ('"
                    +intitule+"', '"+lang+"', '"+idThesaurus+"', false, '"+idTerm+"')").toString());
        
        stmt.close();
    }

    public void updateIntitule(Connect connect, String intitule, String idThesaurus, String lang,
            String idConcept, String idTerm) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            String idTerme = getIdPreferredTerme(stmt, idConcept, idThesaurus);
            stmt.executeUpdate("UPDATE non_preferred_term SET lexical_value = '"+intitule+"', lang='"
                    +lang+"' WHERE id_term ='"+idTerme+"' AND id_term = '"+idTerm+"'");
            stmt.close();
        } catch (SQLException e) {
            LOG.error(e);
            System.out.println(">>>> Erreur : " + e);
        }
    }

    public String searchTGByConceptAndThesaurus(Connect connect, String idConceptSelected, String idThesaurus) {
        String idTerme = null;
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            stmt.executeQuery("SELECT id_concept2 FROM hierarchical_relationship WHERE id_concept1 = '"+idConceptSelected
                    +"' AND id_thesaurus= '"+idThesaurus+"' AND role = 'TG';");
            resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                idTerme = resultSet.getString("id_concept2");
            }
            resultSet.close();
            stmt.close();
        } catch (SQLException e) {
            LOG.error(e);
            System.out.println(">>>> Erreur : " + e);
        }
        return idTerme;
    }

    public List<String> searchTSByConceptAndThesaurus(Connect connect, String idConceptSelected, String idThesaurus) {
        List<String> termes = new ArrayList<>();
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            stmt.executeQuery("SELECT id_concept2 FROM hierarchical_relationship WHERE id_concept1 = '"+idConceptSelected
                    +"' AND id_thesaurus= '"+idThesaurus+"' AND role = 'TS';");
            resultSet = stmt.getResultSet();
            while (resultSet.next()) {
                termes.add(resultSet.getString("id_concept2"));
            }
            resultSet.close();
            stmt.close();
        } catch (SQLException e) {
            LOG.error(e);
            System.out.println(">>>> Erreur : " + e);
        }
        return termes;
    }

    public void saveNewTerme(Connect connect, String idConceptSelected, String idConceptdestination, String idThesaurus, String role) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            executInsertRequest(stmt, "INSERT INTO hierarchical_relationship(id_concept1, id_thesaurus, role, id_concept2) VALUES ('"
                    +idConceptSelected+"', '"+idThesaurus+"', '"+role+"', '"+idConceptdestination+"')");
            stmt.close();
        } catch (SQLException e) {
            LOG.error(e);
            System.out.println(">>>> Erreur : " + e);
        }
    }

    public void updateTerme(Connect connect, String idConceptSelected, String idConceptdestination, String idThesaurus, String role) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            executInsertRequest(stmt, "UPDATE hierarchical_relationship SET id_concept2 = '"+idConceptdestination
                    +"' WHERE id_concept1= '"+idConceptSelected+"' AND id_thesaurus= '"+idThesaurus+"' AND role='"+role+"'");
            stmt.close();
        } catch (SQLException e) {
            LOG.error(e);
            System.out.println(">>>> Erreur : " + e);
        }
    }

    public void deleteExistingTerme(Connect connect, String idConceptSelected, String idConceptdestination, String idThesaurus, String role) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            executDeleteRequest(stmt, "DELETE FROM hierarchical_relationship WHERE id_concept1 = '"+idConceptSelected
                    +"'AND id_concept2 = '"+idConceptdestination+"' AND id_thesaurus = '"+idThesaurus+"' AND role = '"+role+"'");
            stmt.close();
        } catch (SQLException e) {
            LOG.error(e);
            System.out.println(">>>> Erreur : " + e);
        }
    }

    public void deleteAllTermesByConcepteAndRole(Connect connect, String idConceptSelected, 
                    String idThesaurus, String role) {
        try {
            stmt = connect.getPoolConnexion().getConnection().createStatement();
            executDeleteRequest(stmt, "DELETE FROM hierarchical_relationship WHERE id_concept1 = '"+idConceptSelected
                    +"' AND id_thesaurus = '"+idThesaurus+"' AND role = '"+role+"'");
            stmt.close();
        } catch (SQLException e) {
            LOG.error(e);
            System.out.println(">>>> Erreur : " + e);
        }
    }

    private String getIdPreferredTerme(Statement stmt, String idConcept, String idThesaurus) throws SQLException {
        String idTerme = null;
        stmt.executeQuery("SELECT id_term FROM preferred_term WHERE id_concept = '"+idConcept
                +"' AND id_thesaurus = '"+idThesaurus+"';");
        resultSet = stmt.getResultSet();
        while (resultSet.next()) {
            idTerme = resultSet.getString("id_term");
        }
        resultSet.close();
        return idTerme;
    }
    
}
