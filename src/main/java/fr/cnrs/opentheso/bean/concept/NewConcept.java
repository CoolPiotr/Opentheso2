/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.concept;

import fr.cnrs.opentheso.bdd.datas.Concept;
import fr.cnrs.opentheso.bdd.datas.Term;
import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.FacetHelper;
import fr.cnrs.opentheso.bdd.helper.GroupHelper;
import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.RelationsHelper;
import fr.cnrs.opentheso.bdd.helper.SearchHelper;
import fr.cnrs.opentheso.bdd.helper.TermHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeFacet;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeTypeRelation;
import fr.cnrs.opentheso.bdd.helper.nodes.group.NodeGroup;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bdd.helper.nodes.search.NodeSearchMini;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.leftbody.TreeNodeData;
import fr.cnrs.opentheso.bean.leftbody.viewtree.Tree;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.RoleOnThesoBean;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import fr.cnrs.opentheso.bean.rightbody.viewconcept.ConceptView;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.primefaces.PrimeFaces;

/**
 *
 * @author miledrousset
 */
@Named(value = "newConcept")
@SessionScoped
public class NewConcept implements Serializable {
    @Inject private Connect connect;
    @Inject private RoleOnThesoBean roleOnThesoBean;
    @Inject private LanguageBean languageBean;
    @Inject private ConceptView conceptBean;
    @Inject private SelectedTheso selectedTheso;
    @Inject private Tree tree;

    private String prefLabel;
    private String notation;
    private String idNewConcept; // l'utilisateur peut choisir un identifiant à la création.
    private String source;
    private ArrayList<NodeTypeRelation> typesRelationsNT;
    private String relationType;
    private ArrayList<NodeGroup> nodeGroups;
    private String idGroup; // facultatif    
    private boolean isCreated;
    private boolean duplicate;
    private List<NodeSearchMini> nodeSearchMinis;
   
    /// partie pour les concepts à créer sous une Facette 
    private String idBTfacet;
    private String idFacet;
    private boolean isConceptUnderFacet;
    
    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        if(typesRelationsNT != null){
            typesRelationsNT.clear();
            typesRelationsNT = null;
        }
        if(nodeGroups != null){
            nodeGroups.clear();
            nodeGroups = null;
        }
        if(nodeSearchMinis != null){
            nodeSearchMinis.clear();
            nodeSearchMinis = null;
        }        
        prefLabel = null;
        notation = null;
        idNewConcept = null;
        source = null;
        relationType = null;
        idGroup = null;
        idBTfacet = null;
        idFacet = null;        
    }      
    
    public NewConcept() {
    }

    public void reset() {
        isCreated = false;
        duplicate = false;
        prefLabel = null;
        idNewConcept = null;
        notation = null;
        isConceptUnderFacet = false;
        if(conceptBean.getNodeConcept() != null) {
            for (NodeGroup nodeGroup : conceptBean.getNodeConcept().getNodeConceptGroup()) {
                idGroup = nodeGroup.getConceptGroup().getIdgroup();
            }
        }
        RelationsHelper relationsHelper = new RelationsHelper();
        typesRelationsNT = relationsHelper.getTypesRelationsNT(connect.getPoolConnexion());
        GroupHelper groupHelper = new GroupHelper();
        nodeGroups = groupHelper.getListConceptGroup(connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                selectedTheso.getCurrentLang());
    }
    
    public void resetForFacet(NodeFacet nodeFacet){
        reset();
        isConceptUnderFacet = true;
        // le concept BT
        idBTfacet = nodeFacet.getIdConceptParent();
        // id de la facette
        idFacet = nodeFacet.getIdFacet();

    }

    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void infosTopConcept() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Top Concept!");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet d'ajouter un nouveau top concept si le groupe = null, on ajoute un
     * TopConcept sans groupe si l'id du concept est fourni, il faut controler
     * s'il est unique
     *
     * @param idLang
     * @param status // descripteur=D cancdidat = CA
     * @param idTheso
     * @param idUser
     */
    public void addTopConcept(
            String idLang,
            String status, // CA ou D
            String idTheso,
            int idUser) {
        isCreated = false;
        duplicate = false;

        if (prefLabel == null || prefLabel.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "le label est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            //     msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Loggin Error", "Invalid credentials");
            //        FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        ConceptHelper conceptHelper = new ConceptHelper();
        if (roleOnThesoBean.getNodePreference() == null) {
            // erreur de préférences de thésaurusa
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "le thésaurus n'a pas de préférences !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());

        TermHelper termHelper = new TermHelper();
        
        // vérification si le term à ajouter existe déjà 
        
        // verification dans les prefLabels
        if (termHelper.isPrefLabelExist(connect.getPoolConnexion(),
                prefLabel.trim(),
                idTheso,
                idLang)) {
            duplicate = true;
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "un TopTerme existe déjà avec ce nom !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        // verification dans les altLabels
        if (termHelper.isAltLabelExist(connect.getPoolConnexion(),
                prefLabel.trim(),
                idTheso,
                idLang)) {
            duplicate = true;
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "un synonyme existe déjà avec ce nom !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        } 

        if ((idNewConcept != null) && (!idNewConcept.isEmpty())) {
            if (conceptHelper.isIdExiste(connect.getPoolConnexion(), idNewConcept, idTheso)) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Attention!", "Identifiant déjà attribué, veuillez choisir un autre ou laisser vide !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        } else {
            idNewConcept = null;
        }

        Concept concept = new Concept();

        // si le group est à null ou vide, on créé le concept sans l'ajouter à aucun groupe 
        // c'est dans ConceptHelper que ca se passe.
        concept.setIdGroup(idGroup);

        concept.setIdThesaurus(idTheso);
        concept.setStatus(status);
        concept.setNotation(notation);
        concept.setTopConcept(true);

        concept.setIdConcept(idNewConcept); // si l'id est null, un nouvel identifiant sera attribué

        Term terme = new Term();
        terme.setId_thesaurus(idTheso);
        terme.setLang(idLang);
        terme.setLexical_value(prefLabel.trim());
        if (source == null) {
            source = "";
        }
        terme.setSource(source);

        terme.setStatus(status);
        concept.setTopConcept(false);
        idNewConcept = conceptHelper.addConcept(
                connect.getPoolConnexion(),
                null, null,
                concept,
                terme,
                idUser);

        if (idNewConcept == null) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", conceptHelper.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        conceptBean.getConcept(idTheso, idNewConcept, idLang);
        isCreated = true;
        duplicate = false;
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le top concept a bien été ajouté");
        FacesContext.getCurrentInstance().addMessage(null, msg);


        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("messageIndex");
            pf.ajax().update("formRightTab:viewTabConcept:conceptView");
    //        pf.ajax().update("formLeftTab:tabTree:idAddTopConcept");
    //        pf.ajax().update("formLeftTab:tabTree:idGlobalActionsTree");
        }

        PrimeFaces.current().executeScript("PF('addTopConcept').hide();");        
        //   if(tree.getSelectedNode() == null) return;
        // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
        //    if( !((TreeNodeData) tree.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(idConceptParent)){

        //   }

        tree.addNewChild(tree.getRoot(), idNewConcept, idTheso, idLang);

        tree.expandTreeToPath(idNewConcept, idTheso, idLang);
        if (pf.isAjaxRequest()) {
            pf.ajax().update("formLeftTab:tabTree:tree");
        }
        init();
        // cas où l'arbre est déjà déplié ou c'est un concept sans fils
        /*    if (tree.getSelectedNode().isExpanded() || tree.getSelectedNode().getChildCount() == 0) {
            tree.addNewChild(tree.getSelectedNode(), idNewConcept, idTheso, idLang);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("formLeftTab");
            }
        }*/

        // sinon, on ne fait rien, l'arbre sera déplié automatiquement
        //    PrimeFaces.current().executeScript("$('.addNT1').modal('hide');"); 
    }

    /**
     * permet d'ajouter un nouveau concept si le groupe = null, on ajoute un
     * concept sans groupe si l'id du concept est fourni, il faut controler s'il
     * est unique
     *
     * @param idConceptParent
     * @param idLang
     * @param status // descripteur=D cancdidat = CA
     * @param idTheso
     * @param idUser
     */
    public void addNewConcept(
            String idConceptParent,
            String idLang,
            String status, // CA ou D
            String idTheso,
            int idUser) {
        isCreated = false;
        duplicate = false;

        if (prefLabel == null || prefLabel.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "le label est obligatoire !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            //     msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Loggin Error", "Invalid credentials");
            //        FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        TermHelper termHelper = new TermHelper();
      
        // vérification si le term à ajouter existe déjà 
        
        // verification dans les prefLabels
        if (termHelper.isPrefLabelExist(connect.getPoolConnexion(),
                prefLabel.trim(),
                idTheso,
                idLang)) {
            duplicate = true;
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "un prefLabel existe déjà avec ce nom !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        // verification dans les altLabels
        if (termHelper.isAltLabelExist(connect.getPoolConnexion(),
                prefLabel.trim(),
                idTheso,
                idLang)) {
            duplicate = true;
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Attention!", "un synonyme existe déjà avec ce nom !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }        

        addNewConceptForced(idConceptParent, idLang, status, idTheso, idUser);
    }
    
    /**
     * permet d'ajouter un nouveau concept en doublon après validation de l'utilisateur 
     * pour créer un doublon
     *
     * @param idConceptParent
     * @param idLang
     * @param status // descripteur=D cancdidat = CA
     * @param idTheso
     * @param idUser
     */
    public void addNewConceptForced(
            String idConceptParent,
            String idLang,
            String status, // CA ou D
            String idTheso,
            int idUser) {
        isCreated = false;
        duplicate = false;
        FacesMessage msg;
        
        ConceptHelper conceptHelper = new ConceptHelper();
        if (roleOnThesoBean.getNodePreference() == null) {
            // erreur de préférences de thésaurusa
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "le thésaurus n'a pas de préférences !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        conceptHelper.setNodePreference(roleOnThesoBean.getNodePreference());

        if ((idNewConcept != null) && (!idNewConcept.isEmpty())) {
            if (conceptHelper.isIdExiste(connect.getPoolConnexion(), idNewConcept, idTheso)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Attention!", "Identifiant déjà attribué, veuillez choisir un autre ou laisser vide !!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        } else {
            idNewConcept = null;
        }

        Concept concept = new Concept();

        // si le group est à null ou vide, on créé le concept sans l'ajouter à aucun groupe 
        // c'est dans ConceptHelper que ca se passe.
        concept.setIdGroup(idGroup);

        concept.setIdThesaurus(idTheso);
        concept.setStatus(status);
        concept.setNotation(notation);

        concept.setIdConcept(idNewConcept); // si l'id est null, un nouvel identifiant sera attribué

        Term terme = new Term();
        terme.setId_thesaurus(idTheso);
        terme.setLang(idLang);
        terme.setLexical_value(prefLabel.trim());
        if (source == null) {
            source = "";
        }
        terme.setSource(source);

        terme.setStatus(status);
        concept.setTopConcept(false);
        
        // Si le concept est sous une Facette, le BT est celui du parent de la facette 
        if(isConceptUnderFacet) {
            idConceptParent = idBTfacet;
        }
        
        idNewConcept = conceptHelper.addConcept(
                connect.getPoolConnexion(),
                idConceptParent, relationType,
                concept,
                terme,
                idUser);

        if (idNewConcept == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", conceptHelper.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }
        if(isConceptUnderFacet) {
            FacetHelper facetHelper = new FacetHelper();
            if(!facetHelper.addConceptToFacet(connect.getPoolConnexion(), idFacet, idTheso, idNewConcept)){
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur!", "le concept n'a pas été ajouté à la facette");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
            TreeNodeData data = new TreeNodeData(idNewConcept, prefLabel, "", false,
                    false, true, false, "term");
            data.setIdFacetParent(idFacet);
            tree.getDataService().addNodeWithoutChild("file", data, tree.getSelectedNode());
            
            tree.initialise(selectedTheso.getCurrentIdTheso(), selectedTheso.getSelectedLang());
            tree.expandTreeToPath2(idBTfacet,
                    selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getSelectedLang(),
                    idFacet);

            PrimeFaces pf = PrimeFaces.current();
            if (pf.isAjaxRequest()) {
                pf.ajax().update("formLeftTab:tabTree:tree");
            }
            FacesMessage msg2 = new FacesMessage(FacesMessage.SEVERITY_INFO, "", "Concept ajouté avec succès !");
            FacesContext.getCurrentInstance().addMessage(null, msg2);
            init();
            return;
        }        
        
        PrimeFaces pf = PrimeFaces.current();
        if (tree.getSelectedNode() == null) {
            return;
        }
        // si le concept en cours n'est pas celui sélectionné dans l'arbre, on se positionne sur le concept en cours dans l'arbre
        if (!((TreeNodeData) tree.getSelectedNode().getData()).getNodeId().equalsIgnoreCase(idConceptParent)) {
            tree.expandTreeToPath(idConceptParent, idTheso, idLang);
        }

        // cas où l'arbre est déjà déplié ou c'est un concept sans fils
        
        /// attention, cette condition permet d'éviter une erreur dans l'arbre si : 
        // un concept est sélectionné dans l'arbre mais non déployé, puis, on ajoute un TS, alors ca produit une erreur
        if(tree.getSelectedNode().getChildCount() == 0) {
            tree.getSelectedNode().setType("concept");
        }
        if (tree.getSelectedNode().isExpanded() || tree.getSelectedNode().getChildCount() == 0) {
            tree.addNewChild(tree.getSelectedNode(), idNewConcept, idTheso, idLang);
            if (pf.isAjaxRequest()) {
                pf.ajax().update("formLeftTab:tabTree:tree");
                pf.executeScript("srollToSelected()");
            }
            tree.getSelectedNode().setExpanded(true);
        }
        conceptBean.getConcept(idTheso, idConceptParent, idLang);
        isCreated = true;

        if (pf.isAjaxRequest()) {
        //    pf.ajax().update("messageIndex");
            pf.ajax().update("formRightTab:viewTabConcept:idConceptNarrower");     
        }        
        
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "Le concept a bien été ajouté");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        if(!conceptHelper.getMessage().isEmpty()) {
            FacesMessage msg2 = new FacesMessage(FacesMessage.SEVERITY_WARN, "", conceptHelper.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg2);                 
        }        
        init();
 //       PrimeFaces.current().executeScript("PF('addNT').hide();");
    }    

    private void init(){
        duplicate = false;
        idNewConcept = null;
        prefLabel = "";
    }
    
    public void cancel() {
        duplicate = false;
    }
    

    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    // à activer s'il faut faire un controle en temps réel de l'existance d'un terme avant la création
    public List<NodeSearchMini> completExactTerm(String value) {

        SearchHelper searchHelper = new SearchHelper();

        if (selectedTheso.getCurrentIdTheso() == null) {
            return null;
        }
        if (selectedTheso.getCurrentIdTheso().isEmpty()) {
            return null;
        }

        List<NodeSearchMini> liste = searchHelper.searchExactTermForAutocompletion(connect.getPoolConnexion(),
                value,
                selectedTheso.getCurrentLang(),
                selectedTheso.getCurrentIdTheso());
        /*    list.clear();
        for (NodeSearchMini nodeSearchMini : liste) {
            list.add(nodeSearchMini.getPrefLabel());
        }*/
        nodeSearchMinis = liste;
        return liste;
    }

    /**
     * permet de retourner les infos en temps réel pour un concept pour afficher
     * les groupes et les définitions
     *
     * @param idConcept
     * @param idTerm
     * @return
     */
    public String getInfosConcepts(String idConcept, String idTerm) {
        String infos = "";
        GroupHelper groupHelper = new GroupHelper();
        NoteHelper noteHelper = new NoteHelper();
        ConceptHelper conceptHelper = new ConceptHelper();
        ArrayList<String> idGroups = conceptHelper.getListGroupIdOfConcept(
                connect.getPoolConnexion(), idConcept, selectedTheso.getCurrentIdTheso());
        for (String idGroup1 : idGroups) {
            infos = groupHelper.getLexicalValueOfGroup(
                    connect.getPoolConnexion(), idGroup1,
                    selectedTheso.getCurrentIdTheso(),
                    selectedTheso.getCurrentLang());
        }

        ArrayList<NodeNote> nodeNotes = noteHelper.getListNotesTerm(connect.getPoolConnexion(), idTerm,
                selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());
        for (NodeNote nodeNote : nodeNotes) {
            infos = infos + " \n" + nodeNote.getLexicalvalue();
        }
        return infos;
    }

    public String getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(String prefLabel) {
        this.prefLabel = prefLabel;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public String getNotation() {
        return notation;
    }

    public void setNotation(String notation) {
        this.notation = notation;
    }

    public String getIdNewConcept() {
        return idNewConcept;
    }

    public void setIdNewConcept(String idNewConcept) {
        this.idNewConcept = idNewConcept;
    }

    public String getIdGroup() {
        return idGroup;
    }

    public void setIdGroup(String idGroup) {
        this.idGroup = idGroup;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isIsCreated() {
        return isCreated;
    }

    public void setIsCreated(boolean isCreated) {
        this.isCreated = isCreated;
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public void setDuplicate(boolean duplicate) {
        this.duplicate = duplicate;
    }

    public ArrayList<NodeTypeRelation> getTypesRelationsNT() {
        return typesRelationsNT;
    }

    public void setTypesRelationsNT(ArrayList<NodeTypeRelation> typesRelationsNT) {
        this.typesRelationsNT = typesRelationsNT;
    }

    public ArrayList<NodeGroup> getNodeGroups() {
        return nodeGroups;
    }

    public void setNodeGroups(ArrayList<NodeGroup> nodeGroups) {
        this.nodeGroups = nodeGroups;
    }

    public List<NodeSearchMini> getNodeSearchMinis() {
        return nodeSearchMinis;
    }

    public void setNodeSearchMinis(List<NodeSearchMini> nodeSearchMinis) {
        this.nodeSearchMinis = nodeSearchMinis;
    }

    public boolean isIsConceptUnderFacet() {
        return isConceptUnderFacet;
    }

    public void setIsConceptUnderFacet(boolean isConceptUnderFacet) {
        this.isConceptUnderFacet = isConceptUnderFacet;
    }

}
