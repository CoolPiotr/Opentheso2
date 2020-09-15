/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.condidat;

import fr.cnrs.opentheso.bdd.helper.NoteHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeLangTheso;
import fr.cnrs.opentheso.bdd.helper.nodes.notes.NodeNote;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
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
@Named(value = "noteBeanCandidat")
@SessionScoped
public class NoteBeanCandidat implements Serializable {

    @Inject
    private Connect connect;
    @Inject
    private LanguageBean languageBean;
    @Inject
    private SelectedTheso selectedTheso;
    @Inject CandidatBean candidatBean;

    private String selectedLang;
    private ArrayList<NoteHelper.NoteType> noteTypes;
    private ArrayList<NodeLangTheso> nodeLangs;

    private String selectedTypeNote;
    private String noteValue;

    private NodeNote selectedNodeNote;
    private String noteValueToChange;
    private boolean visible;

    public NoteBeanCandidat() {
    }

    public void reset() {
        visible = true;
        noteTypes = new NoteHelper().getNotesType(connect.getPoolConnexion());
        nodeLangs = selectedTheso.getNodeLangs();
        selectedLang = candidatBean.getCandidatSelected().getLang();
        noteValue = "";
        selectedTypeNote = null;

    }
    
    public void resetEditNode(NodeNote selectedNodeNote) {
        reset();
        noteValue = selectedNodeNote.getLexicalvalue();
        selectedTypeNote = selectedNodeNote.getLang();

    }

    public void initSelectedNode (NodeNote nodeNote){
        selectedNodeNote = nodeNote;
        noteValueToChange = nodeNote.getLexicalvalue();
    }
    
    public void infos() {
        FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "info !", " rediger une aide ici pour Add Concept !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    /**
     * permet d'ajouter un nouveau concept si le groupe = null, on ajoute un
     * concept sans groupe si l'id du concept est fourni, il faut controler s'il
     * est unique
     *
     * @param idUser
     */
    public void addNewNote(int idUser) {
        FacesMessage msg;
        if (noteValue == null || noteValue.isEmpty()) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " La note ne doit pas être vide !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;
        }

        switch (selectedTypeNote) {
            case "note":
                if (!addConceptNote(idUser)) {
                    printErreur();
                    return;
                }
                break;
            case "definition":
                if (!addtermNote(idUser)) {
                    printErreur();
                    return;
                }
                break;
            case "scopeNote":
                if (!addConceptNote(idUser)) {
                    printErreur();
                    return;
                }
                break;
            case "example":
                if (!addtermNote(idUser)) {
                    printErreur();
                    return;
                }
                break;
            case "historyNote":
                if (!addtermNote(idUser)) {
                    printErreur();
                    return;
                }
                break;
            case "editorialNote":
                if (!addtermNote(idUser)) {
                    printErreur();
                    return;
                }
                break;
            case "changeNote":
                if (!addtermNote(idUser)) {
                    printErreur();
                    return;
                }
                break;
            default:
                break;
        }
        reset();
        try {          
            candidatBean.showCandidatSelected(candidatBean.getCandidatSelected());
        } catch (IOException ex) {
            Logger.getLogger(NoteBeanCandidat.class.getName()).log(Level.SEVERE, null, ex);
        }

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("candidatForm:listTraductionForm");
            pf.ajax().update("candidatForm");
        }
        visible = false;
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "note ajoutée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);        
    }

    
    public void updateNote(int idUser){
        NoteHelper noteHelper = new NoteHelper();
        FacesMessage msg;        
        if (selectedNodeNote.getNotetypecode().equalsIgnoreCase("note") || selectedNodeNote.getNotetypecode().equalsIgnoreCase("scopeNote") || selectedNodeNote.getNotetypecode().equalsIgnoreCase("historyNote")) {
            if (!noteHelper.updateConceptNote(connect.getPoolConnexion(),
                    selectedNodeNote.getId_note(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                    selectedNodeNote.getId_concept(),
                    selectedNodeNote.getLang(),
                    selectedTheso.getCurrentIdTheso(),
                    noteValueToChange,
                    selectedNodeNote.getNotetypecode(),
                    idUser)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        } else {
            if (!noteHelper.updateTermNote(connect.getPoolConnexion(),
                    selectedNodeNote.getId_note(),
                    selectedNodeNote.getId_term(),
                    selectedNodeNote.getLang(),                    
                    selectedTheso.getCurrentIdTheso(),
                    noteValueToChange,
                    selectedNodeNote.getNotetypecode(),
                    idUser)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de modification !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        }
        reset();
        try {          
            candidatBean.showCandidatSelected(candidatBean.getCandidatSelected());
        } catch (IOException ex) {
            Logger.getLogger(NoteBeanCandidat.class.getName()).log(Level.SEVERE, null, ex);
        }

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("candidatForm:listTraductionForm");
            pf.ajax().update("candidatForm");
        }        

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "note modifiée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
      
    }
/*    
    public ArrayList<NodeNote> nodeToEdit() {
        if (selectedTypeNote == null) {
            return null;
        }
        switch (selectedTypeNote) {
            case "note":
                return conceptBean.getNotes();
            case "definition":
                return conceptBean.getDefinitions();
            case "scopeNote":
                return conceptBean.getScopeNotes();
            case "example":
                return conceptBean.getExamples();
            case "historyNote":
                return conceptBean.getHistoryNotes();
            case "editorialNote":
                return conceptBean.getEditorialNotes();
            case "changeNote":
                return conceptBean.getChangeNotes();
            default:
                return null;
        }
    }    
    */
    
    
    public void deleteNote(int idUser) {
        NoteHelper noteHelper = new NoteHelper();
        FacesMessage msg;

        if (selectedNodeNote.getNotetypecode().equalsIgnoreCase("note") || selectedNodeNote.getNotetypecode().equalsIgnoreCase("scopeNote") || selectedNodeNote.getNotetypecode().equalsIgnoreCase("historyNote")) {
            if (!noteHelper.deletethisNoteOfConcept(connect.getPoolConnexion(),
                    selectedNodeNote.getId_note(), /// c'est l'id qui va permettre de supprimer la note, les autres informations sont destinées pour l'historique  
                    selectedNodeNote.getId_concept(),
                    selectedNodeNote.getLang(),
                    selectedTheso.getCurrentIdTheso(),
                    selectedNodeNote.getNotetypecode(),
                    noteValueToChange, idUser)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        } else {
            if (!noteHelper.deleteThisNoteOfTerm(connect.getPoolConnexion(),
                    selectedNodeNote.getId_note(),
                    selectedNodeNote.getId_term(),
                    selectedNodeNote.getLang(),                    
                    selectedTheso.getCurrentIdTheso(),
                    selectedNodeNote.getNotetypecode(),
                    noteValueToChange, idUser)) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de suppression !");
                FacesContext.getCurrentInstance().addMessage(null, msg);
                return;
            }
        }

        reset();
        try {          
            candidatBean.showCandidatSelected(candidatBean.getCandidatSelected());
        } catch (IOException ex) {
            Logger.getLogger(NoteBeanCandidat.class.getName()).log(Level.SEVERE, null, ex);
        }

        PrimeFaces pf = PrimeFaces.current();
        if (pf.isAjaxRequest()) {
            pf.ajax().update("candidatForm:listTraductionForm");
            pf.ajax().update("candidatForm");
        }            

        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "note supprimée avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    /*
    public ArrayList<NodeNote> nodeToDelete() {
        if (selectedTypeNote == null) {
            return null;
        }
        switch (selectedTypeNote) {
            case "note":
                return conceptBean.getNotes();
            case "definition":
                return conceptBean.getDefinitions();
            case "scopeNote":
                return conceptBean.getScopeNotes();
            case "example":
                return conceptBean.getExamples();
            case "historyNote":
                return conceptBean.getHistoryNotes();
            case "editorialNote":
                return conceptBean.getEditorialNotes();
            case "changeNote":
                return conceptBean.getChangeNotes();
            default:
                return null;
        }
    }*/

    private boolean addConceptNote(int idUser) {
        NoteHelper noteHelper = new NoteHelper();
        return noteHelper.addConceptNote(
                connect.getPoolConnexion(),
                candidatBean.getCandidatSelected().getIdConcepte(),
                selectedLang,
                selectedTheso.getCurrentIdTheso(),
                noteValue,
                selectedTypeNote, idUser);
    }

    private boolean addtermNote(int idUser) {
        NoteHelper noteHelper = new NoteHelper();
        return noteHelper.addTermNote(
                connect.getPoolConnexion(),
                candidatBean.getCandidatSelected().getIdTerm(),
                selectedLang,
                selectedTheso.getCurrentIdTheso(),
                noteValue,
                selectedTypeNote, idUser);
    }

    private void printErreur() {
        FacesMessage msg;
        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " Erreur de création de note !");
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public String getSelectedLang() {
        return selectedLang;
    }

    public void setSelectedLang(String selectedLang) {
        this.selectedLang = selectedLang;
    }

    public ArrayList<NoteHelper.NoteType> getNoteTypes() {
        return noteTypes;
    }

    public void setNoteTypes(ArrayList<NoteHelper.NoteType> noteTypes) {
        this.noteTypes = noteTypes;
    }

    public String getSelectedTypeNote() {
        return selectedTypeNote;
    }

    public void setSelectedTypeNote(String selectedTypeNote) {
        this.selectedTypeNote = selectedTypeNote;
    }

    public String getNoteValue() {
        return noteValue;
    }

    public void setNoteValue(String noteValue) {
        this.noteValue = noteValue;
    }

    public ArrayList<NodeLangTheso> getNodeLangs() {
        return nodeLangs;
    }

    public void setNodeLangs(ArrayList<NodeLangTheso> nodeLangs) {
        this.nodeLangs = nodeLangs;
    }

    public NodeNote getSelectedNodeNote() {
        return selectedNodeNote;
    }

    public void setSelectedNodeNote(NodeNote selectedNodeNote) {
        this.selectedNodeNote = selectedNodeNote;
    }

    public String getNoteValueToChange() {
        return noteValueToChange;
    }

    public void setNoteValueToChange(String noteValueToChange) {
        this.noteValueToChange = noteValueToChange;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
}
