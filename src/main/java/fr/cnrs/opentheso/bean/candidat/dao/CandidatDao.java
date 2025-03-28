package fr.cnrs.opentheso.bean.candidat.dao;

import com.zaxxer.hikari.HikariDataSource;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.candidat.NodeCandidateOld;
import fr.cnrs.opentheso.bdd.helper.nodes.candidat.NodeProposition;
import fr.cnrs.opentheso.bdd.helper.nodes.candidat.NodeTraductionCandidat;
import fr.cnrs.opentheso.bdd.tools.StringPlus;
import fr.cnrs.opentheso.bean.candidat.dto.CandidatDto;
import fr.cnrs.opentheso.bean.candidat.dto.VoteDto;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class CandidatDao {


    protected final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public List<CandidatDto> searchAllCondidats(HikariDataSource hikariDataSource, String idThesaurus,
            String lang) throws SQLException {

        List<CandidatDto> temps = new ArrayList<>();

        try (Connection conn = hikariDataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){
        
                stmt.executeQuery(new StringBuffer("SELECT term.lang, term.id_term,")
                    .append(" term.lexical_value, con.id_concept,")
                    .append(" con.id_thesaurus, con.created,")
                    .append(" users.username, term.contributor")

                    .append(" FROM preferred_term preTer, concept con, term, users")
                    .append(" WHERE")
                    .append(" con.id_concept = preTer.id_concept")
                    .append(" AND term.id_term = preTer.id_term")
                    .append(" AND con.id_thesaurus = preTer.id_thesaurus")
                    .append(" AND preTer.id_thesaurus = term.id_thesaurus")
                    .append(" AND con.status = 'CA'")
                    .append(" AND users.id_user = term.contributor")

                    .append(" AND term.lang = '").append(lang).append("' ")
                    .append(" AND con.id_thesaurus = '").append(idThesaurus).append("'")
                    .append(" ORDER BY term.lexical_value ASC").toString());

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        CandidatDto candidatDto = new CandidatDto();
                        candidatDto.setIdTerm(resultSet.getString("id_term"));
                        candidatDto.setNomPref(resultSet.getString("lexical_value"));
                        candidatDto.setIdConcepte(resultSet.getString("id_concept"));
                        candidatDto.setIdThesaurus(resultSet.getString("id_thesaurus"));
                        candidatDto.setCreationDate(resultSet.getDate("created"));
                        candidatDto.setUser(resultSet.getString("username"));
                        candidatDto.setUserId(resultSet.getInt("contributor"));
                        temps.add(candidatDto);
                    }
                }
            }
        }
        return temps;
    }
    
    /**
     * permet de récupérer la liste des candidats qui sont en attente
     * @param hikariDataSource
     * @param idThesaurus
     * @param lang
     * @param etat
     * @param statut
     * @return
     * @throws SQLException 
     */
    public List<CandidatDto> getCandidatsByStatus(HikariDataSource hikariDataSource, String idThesaurus,
            String lang, int etat, String statut) throws SQLException {

        List<CandidatDto> candidatDtos = new ArrayList<>();

        getAllIdOfCandidate(hikariDataSource, idThesaurus, etat, candidatDtos);
        ConceptHelper conceptHelper = new ConceptHelper();

        for (CandidatDto candidatDto : candidatDtos) {
            candidatDto.setNomPref(conceptHelper.getLexicalValueOfConcept(hikariDataSource, candidatDto.getIdConcepte(), idThesaurus, lang));
        }
        return candidatDtos;
    }
    
    private void getAllIdOfCandidate(HikariDataSource hikariDataSource,
            String idTheso, int etat, List<CandidatDto> candidatDtos) throws SQLException{

        try (Connection conn = hikariDataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery("SELECT concept.id_concept, concept.created, candidat_status.id_user" +
                        " FROM concept, candidat_status" +
                        " WHERE concept.id_concept = candidat_status.id_concept" +
                        " AND concept.id_thesaurus = candidat_status.id_thesaurus" +
                        " AND candidat_status.id_status = " + etat +
                        " AND concept.id_thesaurus = '" + idTheso + "'" +
                        " order by created DESC");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while(resultSet.next()) {
                        CandidatDto candidatDto = new CandidatDto();
                        candidatDto.setIdConcepte(resultSet.getString("id_concept"));
                        candidatDto.setCreationDate(resultSet.getDate("created"));
                        candidatDto.setStatut("" + etat);
                        candidatDto.setUserId(resultSet.getInt("id_user"));
                        candidatDto.setIdThesaurus(idTheso);
                        candidatDtos.add(candidatDto);
                    }
                }
            }
        }
    }
    
    /**
     * permet de récupérer la liste des candidats qui sont en attente
     * @param hikariDataSource
     * @param value
     * @param idThesaurus
     * @param lang
     * @param etat
     * @param statut
     * @return
     * @throws SQLException 
     */
    public List<CandidatDto> searchCandidatsByValue(HikariDataSource hikariDataSource, String value, String idThesaurus,
            String lang, int etat, String statut) throws SQLException {

        List<CandidatDto> temps = new ArrayList<>();

        try (Connection conn = hikariDataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery(createRequestSearchValue(lang, value, idThesaurus, etat, statut));

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        CandidatDto candidatDto = new CandidatDto();
                        candidatDto.setIdTerm(resultSet.getString("id_term"));
                        candidatDto.setNomPref(resultSet.getString("lexical_value"));
                        candidatDto.setIdConcepte(resultSet.getString("id_concept"));
                        candidatDto.setIdThesaurus(resultSet.getString("id_thesaurus"));
                        candidatDto.setCreationDate(resultSet.getDate("created"));
                        candidatDto.setUser(resultSet.getString("username"));
                        candidatDto.setUserId(resultSet.getInt("contributor"));
                        temps.add(candidatDto);
                    }
                }
            }
        }
        return temps;
    }

    private String createRequest(String lang, String idThesaurus, int etat, String statut) {

        StringBuffer request = new StringBuffer("SELECT DISTINCT term.lang, term.id_term,")
                .append(" term.lexical_value, con.id_concept,")
                .append(" con.id_thesaurus, con.created,")
                .append(" users.username, term.contributor")

                .append(" FROM preferred_term preTer, concept con, term, users, candidat_status")
                .append(" WHERE con.id_concept = candidat_status.id_concept")
                .append(" AND con.id_thesaurus = candidat_status.id_thesaurus")
                .append(" AND con.id_concept = preTer.id_concept")
                .append(" AND term.id_term = preTer.id_term")
                .append(" AND con.id_thesaurus = preTer.id_thesaurus")
                .append(" AND preTer.id_thesaurus = term.id_thesaurus");

        if ("CA".equals(statut)) {
            request.append(" AND con.status = 'CA'");
        } else {
            request.append(" AND con.status <> 'CA'");
        }

        request.append(" AND users.id_user = term.contributor")
                .append(" AND candidat_status.id_status = ").append(etat)
                .append(" AND term.lang = '").append(lang).append("' ")
                .append(" AND con.id_thesaurus = '").append(idThesaurus).append("'")
                .append(" ORDER BY term.lexical_value ASC");

        return request.toString();

    }    
    
    private String createRequestSearchValue(String lang, String value, String idThesaurus, int etat, String statut) {
        StringPlus stringPlus = new StringPlus();
        value = stringPlus.convertString(value);
        value = stringPlus.unaccentLowerString(value);
        
        StringBuffer request = new StringBuffer("SELECT DISTINCT term.lang, term.id_term,")
                .append(" term.lexical_value, con.id_concept,")
                .append(" con.id_thesaurus, con.created,")
                .append(" users.username, term.contributor")

                .append(" FROM preferred_term preTer, concept con, term, users, candidat_status")
                .append(" WHERE con.id_concept = candidat_status.id_concept")
                .append(" AND con.id_thesaurus = candidat_status.id_thesaurus")
                .append(" AND con.id_concept = preTer.id_concept")
                .append(" AND term.id_term = preTer.id_term")
                .append(" AND con.id_thesaurus = preTer.id_thesaurus")
                .append(" AND preTer.id_thesaurus = term.id_thesaurus");

        if ("CA".equals(statut)) {
            request.append(" AND con.status = 'CA'");
        } else {
            request.append(" AND con.status <> 'CA'");
        }

        request.append(" AND users.id_user = term.contributor")
                .append(" AND candidat_status.id_status = ").append(etat)
                .append(" AND term.lang = '").append(lang).append("' ")
                .append(" AND con.id_thesaurus = '").append(idThesaurus).append("'")
                .append(" AND f_unaccent(lower(term.lexical_value)) like '%").append(value).append("%'")
                .append(" ORDER BY term.lexical_value ASC");

        return request.toString();

    }

    public String searchCondidatStatus(HikariDataSource hikariDataSource, String idCouncepte, String idThesaurus)
            throws SQLException {

        String status = null;

        try (Connection conn = hikariDataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery(new StringBuffer("SELECT sta.value FROM candidat_status can_sta, status sta ")
                        .append("WHERE can_sta.id_status = sta.id_status AND can_sta.id_concept = '")
                        .append(idCouncepte).append("' AND can_sta.id_thesaurus = '")
                        .append(idThesaurus).append("'").toString());

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        status = resultSet.getString("value");
                    }
                }
            }
        }

        return status;
    }

    public int searchParticipantCount(HikariDataSource hikariDataSource, String idCouncepte, String idThesaurus) throws SQLException {
        int nbrParticipant = 0;

        try (Connection conn = hikariDataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){

                stmt.executeQuery(new StringBuffer("SELECT count(*) FROM candidat_messages WHERE id_concept = '")
                        .append(idCouncepte).append("' AND id_thesaurus = '")
                        .append(idThesaurus).append("'").toString());

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        nbrParticipant = resultSet.getInt("count");
                    }
                }
            }
        }

        return nbrParticipant;
    }

    public int searchDemandeCount(HikariDataSource hikariDataSource, String idCouncepte, String idThesaurus) throws SQLException {
        int nbrDemande = 0;

        try (Connection conn = hikariDataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){

                stmt.executeQuery(new StringBuffer("SELECT count(*) FROM proposition WHERE id_concept = '")
                        .append(idCouncepte).append("' AND id_thesaurus = '")
                        .append(idThesaurus).append("'").toString());

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        nbrDemande = resultSet.getInt("count");
                    }
                }
            }
        }

        return nbrDemande;
    }

    public void setStatutForCandidat(HikariDataSource hikariDataSource, int status, String idConcepte,
                                     String idThesaurus, String idUser, String date) {
        try (Connection conn = hikariDataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeUpdate("INSERT INTO candidat_status(id_concept, id_status, date, id_user, id_thesaurus) "
                        + "VALUES ('" + idConcepte + "', " + status + ", '"+date+"', " + idUser + ", '" + idThesaurus + "')");
            }
        } catch (Exception e) {

        }
    }

    public void setStatutForCandidat(HikariDataSource hikariDataSource, int status, String idConcepte,
            String idThesaurus, String idUser) {

        setStatutForCandidat(hikariDataSource, status, idConcepte, idThesaurus, idUser, sdf.format(new Date()));
    }

    public int getMaxCandidatId(HikariDataSource hikariDataSource) throws SQLException {
        int nbrDemande = 0;
        try (Connection conn = hikariDataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){

                stmt.executeQuery("SELECT COALESCE(max(id_concept), '0') max_id from concept where status = 'CA'");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        nbrDemande = resultSet.getInt("max_id");
                    }
                }
            }
        }
        return nbrDemande;
    }
    
/////// ajouté par Miled
    
    public int searchVoteCount(HikariDataSource hikariDataSource, String idCouncepte, String idThesaurus, String typeVote)
            throws SQLException {
        int nbrDemande = 0;

        try (Connection conn = hikariDataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery(new StringBuffer("SELECT count(*) FROM candidat_vote WHERE id_concept = '")
                    .append(idCouncepte).append("' AND id_thesaurus = '").append(idThesaurus)
                    .append("' AND type_vote = '").append(typeVote).append("'").toString());

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        nbrDemande = resultSet.getInt("count");
                    }
                }
            }
        }

        return nbrDemande;
    }    
    
    public void addVote(HikariDataSource hikariDataSource, String idThesaurus, String idConcept,
            int idUser, String idNote, String typeVote) {

        try (Connection conn = hikariDataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeUpdate(
                    "INSERT INTO candidat_vote(id_user, id_concept, id_thesaurus, id_note, type_vote) "
                            + "VALUES (" + idUser + ",'" + idConcept + "','" + idThesaurus + "', '"+idNote+"', '" + typeVote + "')");
            }
        } catch (Exception e) {
        }
    }
    
    public boolean removeVote(HikariDataSource hikariDataSource, String idThesaurus, String idConcept, 
            int idUser, String idNote, String typeVote) throws SQLException {

        try (Connection conn = hikariDataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                String requet = "delete from candidat_vote where id_user = " + idUser
                        + " and id_concept = '" + idConcept + "'"
                        + " and id_thesaurus = '" + idThesaurus + "'"
                        + " and type_vote = '" + typeVote + "'";

                if (idNote != null) {
                    requet += " and id_note = '" + idNote + "'";
                }

                stmt.executeUpdate(requet);
            }
        }

        return true;
    }    
    
    public boolean getVote(HikariDataSource hikariDataSource, int userId, String idConcept,
                    String idTheso, String idNote, String typeVote) throws SQLException {
        boolean voted = false;

        try (Connection conn = hikariDataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){

                String requet = "select id_concept from candidat_vote where" +
                        " id_user = " + userId +
                        " and id_concept = '" + idConcept + "'" +
                        " and id_thesaurus = '" + idTheso + "'"+
                        " and type_vote = '" + typeVote + "' ";
                if (idNote != null){
                    requet += " and id_note = '" + idNote + "' ";
                }

                stmt.executeQuery(requet);

                try (ResultSet resultSet = stmt.getResultSet()) {
                    if(resultSet.next()) {
                        voted = resultSet.getRow() != 0;
                    }
                }
            }
        }

        return voted;
    }



    public List<VoteDto> getAllVotesByCandidat(HikariDataSource hikariDataSource, String idConcept, String idTheso) {
        List<VoteDto> votes = new ArrayList<>();

        try (Connection conn = hikariDataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){

                stmt.executeQuery("SELECT id_user, id_concept, id_thesaurus, type_vote, id_note FROM candidat_vote " +
                        " WHERE id_concept = '"+idConcept+"' AND id_thesaurus = '"+idTheso+"'");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while(resultSet.next()) {
                        VoteDto voteDto = new VoteDto();
                        voteDto.setIdUser(resultSet.getInt("id_user"));
                        voteDto.setIdConcept(resultSet.getString("id_concept"));
                        voteDto.setIdThesaurus(resultSet.getString("id_thesaurus"));
                        voteDto.setTypeVote(resultSet.getString("type_vote"));
                        voteDto.setIdNote(resultSet.getString("id_note"));
                        votes.add(voteDto);
                    }
                }
            }
        } catch (Exception e) {

        }

        return votes;
    }

    public boolean insertCandidate(HikariDataSource hikariDataSource, CandidatDto candidatDto, String adminMessage, int idUser) {

        try (Connection conn = hikariDataSource.getConnection()) {
            conn.setAutoCommit(false);
            try (Statement stmt = conn.createStatement()){
                if(!updateCandidateStatus(candidatDto, stmt, adminMessage, 2, idUser)){ // 2 = insérée
                    conn.rollback();
                    return false;
                }

                if(!changeCandidateToConcept(candidatDto, stmt)){
                    conn.rollback();
                    return false;
                }
                if(candidatDto.getTermesGenerique().isEmpty()) {
                    if(!setTopTermToConcept(candidatDto, stmt)){
                        conn.rollback();
                        return false;
                    }
                }
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }     
    
    public boolean rejectCandidate(HikariDataSource hikariDataSource, CandidatDto candidatDto, String adminMessage, int idUser) {

        try (Connection conn = hikariDataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement()){
                if(!updateCandidateStatus(candidatDto, stmt, adminMessage, 3, idUser)){ // 3 = rejeté
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;            
        } catch (SQLException e) {
            return false;
        }
    }      
    
////////// import des candidats 
    
    public ArrayList<NodeCandidateOld> getCandidatesIdFromOldModule (
            HikariDataSource hikariDataSource, String idTheso) throws SQLException{
       
        ArrayList<NodeCandidateOld> nodeCandidateOlds = new ArrayList<>();

        try (Connection conn = hikariDataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery("select concept_candidat.id_concept, concept_candidat.status"
                        + " from concept_candidat"
                        + " where"
                        + " concept_candidat.id_thesaurus = '" + idTheso +"'"
                        + " and concept_candidat.status = 'a'");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeCandidateOld nodeCandidateOld = new NodeCandidateOld();
                        nodeCandidateOld.setIdCandidate(resultSet.getString("id_concept"));
                        nodeCandidateOld.setStatus(resultSet.getString("status"));
                        nodeCandidateOlds.add(nodeCandidateOld);
                    }
                }
            }
        }

        return nodeCandidateOlds;
    }
    
    public ArrayList<NodeTraductionCandidat> getCandidatesTraductionsFromOldModule(
            HikariDataSource hikariDataSource, String idOldCandidat, String idTheso) throws SQLException{
       
        ArrayList<NodeTraductionCandidat> nodeTraductionCandidats = new ArrayList<>();

        try (Connection conn = hikariDataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){
                stmt.executeQuery("select term_candidat.lexical_value, term_candidat.lang"
                        + " from concept_term_candidat, term_candidat"
                        + " where concept_term_candidat.id_term = term_candidat.id_term"
                        + " and concept_term_candidat.id_thesaurus = term_candidat.id_thesaurus"
                        + " and term_candidat.id_thesaurus = '" + idTheso + "'"
                        + " and concept_term_candidat.id_concept = '" + idOldCandidat + "'");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeTraductionCandidat nodeTraductionCandidat  = new NodeTraductionCandidat();
                        nodeTraductionCandidat.setIdLang(resultSet.getString("lang"));
                        nodeTraductionCandidat.setTitle(resultSet.getString("lexical_value"));
                        nodeTraductionCandidats.add(nodeTraductionCandidat);
                    }
                }
            }
        }

        return nodeTraductionCandidats;
    }
    
    public ArrayList<NodeProposition> getCandidatesMessagesFromOldModule(
            HikariDataSource hikariDataSource, String idOldCandidat, String idTheso) throws SQLException{
       
        ArrayList<NodeProposition> nodePropositions = new ArrayList<>();

        try (Connection conn = hikariDataSource.getConnection()) {
            try (Statement stmt = conn.createStatement()){

                stmt.executeQuery("select proposition.note, proposition.id_user "
                        + " from proposition where"
                        + " proposition.id_concept = '" + idOldCandidat + "'"
                        + " and proposition.id_thesaurus = '" + idTheso + "'"
                        + " ORDER BY created ASC");

                try (ResultSet resultSet = stmt.getResultSet()) {
                    while (resultSet.next()) {
                        NodeProposition nodeProposition  = new NodeProposition();
                        nodeProposition.setNote(resultSet.getString("note"));
                        nodeProposition.setId_user(resultSet.getInt("id_user"));
                        nodePropositions.add(nodeProposition);
                    }
                }
            }
        }

        return nodePropositions;
    }     
////////// fin import des candidats 
    
    
    
    private boolean updateCandidateStatus(CandidatDto candidatDto, Statement stmt,
            String adminMessage, int status, int idUser) throws SQLException{
        StringPlus stringPlus = new StringPlus();
        adminMessage = stringPlus.convertString(adminMessage);

        stmt.executeUpdate("update candidat_status set id_status = " + status +
                ", message = '" + adminMessage + "', id_user_admin = " + idUser +
                " where id_concept = '" + candidatDto.getIdConcepte() + "'" +
                " and id_thesaurus = '" + candidatDto.getIdThesaurus() + "'");
        return true;
    }
    
    private boolean changeCandidateToConcept(CandidatDto candidatDto, Statement stmt) throws SQLException{
        stmt.execute("update concept set status = 'D' where id_concept = '" + candidatDto.getIdConcepte()
                + "' and id_thesaurus = '" + candidatDto.getIdThesaurus() + "'");
        return true;
    }
    
    private boolean setTopTermToConcept(CandidatDto candidatDto, Statement stmt) throws SQLException{
        stmt.execute("update concept set top_concept = true where id_concept = '" + candidatDto.getIdConcepte()
                + "' and id_thesaurus = '" + candidatDto.getIdThesaurus() + "'");
        return true;
    }
            
}
