/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.cnrs.opentheso.bean.rightbody.viewhome;

import fr.cnrs.opentheso.bdd.helper.ConceptHelper;
import fr.cnrs.opentheso.bdd.helper.HtmlPageHelper;
import fr.cnrs.opentheso.bdd.helper.StatisticHelper;
import fr.cnrs.opentheso.bdd.helper.nodes.NodeIdValue;
import fr.cnrs.opentheso.bean.language.LanguageBean;
import fr.cnrs.opentheso.bean.menu.connect.Connect;
import fr.cnrs.opentheso.bean.menu.theso.SelectedTheso;
import java.io.Serializable;
import java.sql.Date;
import java.util.ArrayList;
import javax.annotation.PreDestroy;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;

/**
 *
 * @author miledrousset
 */
@Named(value = "viewEditorThesoHomeBean")
@SessionScoped
public class ViewEditorThesoHomeBean implements Serializable {
    @Inject private Connect connect;
    @Inject private SelectedTheso selectedTheso;
    @Inject private LanguageBean languageBean; 

    private boolean isViewPlainText;
    private boolean isInEditing;

    private String text;
    private String colorOfHtmlButton;
    private String colorOfTextButton;    

    @PreDestroy
    public void destroy(){
        clear();
    }  
    public void clear(){
        text = null;
        colorOfHtmlButton = null;
        colorOfTextButton = null;        
    }     

    public void reset(){
        isInEditing = false;
        isViewPlainText = false;
        text = null;
    }
    
    public void initText() {
        String lang = languageBean.getIdLangue().toLowerCase();
        if(lang == null || lang.isEmpty()) {
            lang = connect.getWorkLanguage();
        } 
        HtmlPageHelper copyrightHelper = new HtmlPageHelper();
        text = copyrightHelper.getThesoHomePage(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                lang);
        isInEditing = true;
        isViewPlainText = false;
        
        colorOfHtmlButton = "#F49F66;";
        colorOfTextButton = "#8C8C8C;";          
    }

    public String getThesoHomePage(){
        String lang = languageBean.getIdLangue().toLowerCase();
        if(lang == null || lang.isEmpty()) {
            lang = connect.getWorkLanguage();
        }         
        HtmlPageHelper copyrightHelper = new HtmlPageHelper();
        String homePage = copyrightHelper.getThesoHomePage(
                connect.getPoolConnexion(),
                selectedTheso.getCurrentIdTheso(),
                lang);
        return homePage;
    }
    /**
     * permet d'ajouter un copyright, s'il n'existe pas, on le créé,sinon, on applique une mise à jour 
     */
    public void updateThesoHomePage() {
        FacesMessage msg;

        String lang = languageBean.getIdLangue().toLowerCase();
        if(lang == null || lang.isEmpty()) {
            lang = connect.getWorkLanguage();
        }         
        HtmlPageHelper htmlPageHelper = new HtmlPageHelper();
        if (!htmlPageHelper.setThesoHomePage(
                connect.getPoolConnexion(),
                text,
                selectedTheso.getCurrentIdTheso(),
                lang)){
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur !", " l'ajout a échoué !");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            return;               
        }
        msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "info", "texte ajouté avec succès");
        FacesContext.getCurrentInstance().addMessage(null, msg);
        isInEditing = false;
        isViewPlainText = false;
    }
    
    public String getTotalConceptOfTheso(){
        StatisticHelper statisticHelper = new StatisticHelper();
        int count = statisticHelper.getNbCpt(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
        return "" + count;
    }    
    
    public String getLastModifiedDate(){
        ConceptHelper conceptHelper = new ConceptHelper();
        Date date = conceptHelper.getLastModification(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso());
        if(date != null)
            return date.toString();
        return "";
    }
    
    public ArrayList<NodeIdValue> getLastModifiedConcepts(){
        ConceptHelper conceptHelper = new ConceptHelper();
        return conceptHelper.getLastModifiedConcept(connect.getPoolConnexion(), selectedTheso.getCurrentIdTheso(), selectedTheso.getCurrentLang());
    }        
    
    public void setViewPlainTextTo(boolean status){
        if(status) {
            colorOfHtmlButton = "#8C8C8C;";
            colorOfTextButton = "#F49F66;";
        } else {
            colorOfHtmlButton = "#F49F66;";
            colorOfTextButton = "#8C8C8C;";            
        } 
        isViewPlainText = status;
    }

    public boolean isIsViewPlainText() {
        return isViewPlainText;
    }

    public void setIsViewPlainText(boolean isViewPlainText) {
        this.isViewPlainText = isViewPlainText;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isIsInEditing() {
        return isInEditing;
    }

    public void setIsInEditing(boolean isInEditing) {
        this.isInEditing = isInEditing;
    }

    public String getColorOfHtmlButton() {
        return colorOfHtmlButton;
    }

    public void setColorOfHtmlButton(String colorOfHtmlButton) {
        this.colorOfHtmlButton = colorOfHtmlButton;
    }

    public String getColorOfTextButton() {
        return colorOfTextButton;
    }

    public void setColorOfTextButton(String colorOfTextButton) {
        this.colorOfTextButton = colorOfTextButton;
    }


    
}
